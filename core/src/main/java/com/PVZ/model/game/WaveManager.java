package com.PVZ.model.game;

import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.status.AppStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WaveManager {
    private final List<Wave> waves;
    private int currentWave = 0;

    private float setupTimer = 0;

    private boolean spawning = false;
    private Iterator<Wave.WaveEntry> entryIterator;
    private Wave.WaveEntry currentEntry;
    private int spawned = 0;
    private float spawnTimer = 0;

    private final List<Zombie> waveZombies = new ArrayList<>();
    private double totalWaveHP = 0;
    private boolean waitingForHP = false;
    private boolean hpConditionMet = false;
    private float hpWaitTimer = 0;

    private boolean started = false;
    private int totalZombieCount = 0;
    private final List<Zombie> allSpawnedWaveZombies = new ArrayList<>();

    public WaveManager(List<Wave> waves) {
        this.waves = waves;
        for (Wave w : waves) {
            for (Wave.WaveEntry e : w.getEntries()) {
                totalZombieCount += e.getCount();
            }
        }
    }

    public void start() {
        started = true;
        if (AppStatus.currentUser != null && AppStatus.currentUser.questState != null) {
            AppStatus.currentUser.questState.getQuestManager().onFirstWaveStarted();
        }
    }

    public boolean isStarted() {
        return started;
    }


    public void update(float delta, ZombieEngine engine) {
        if (!started || currentWave >= waves.size()) {
            return;
        }

        if (!spawning && !waitingForHP) {
            checkAndBeginWave(delta);
        }

        if (spawning) {
            updateSpawning(delta, engine);
        }

        if (waitingForHP) {
            updateWaitingForHP(delta);
        }
    }

    private void checkAndBeginWave(float delta) {
        if (currentWave == 0) {
            setupTimer += delta;
            if (setupTimer < waves.get(0).getStartDelay()) {
                return;
            }
        }
        beginWave();
    }

    private void updateSpawning(float delta, ZombieEngine engine) {
        spawnTimer += delta;
        if (spawnTimer >= currentEntry.getSpawnDelay() && spawned < currentEntry.getCount()) {
            Zombie z = engine.spawnZombie(currentEntry.getZombieAlias(), randomRow(), 8);
            if (z != null) {
                waveZombies.add(z);
                allSpawnedWaveZombies.add(z);
            } else {
                System.out.println("[WaveManager] WARNING: failed to spawn " + currentEntry.getZombieAlias());
            }
            spawned++;
            spawnTimer = 0;
        }

        if (spawned >= currentEntry.getCount()) {
            if (entryIterator.hasNext()) {
                nextEntry();
            } else {
                finishSpawning();
            }
        }
    }

    private void updateWaitingForHP(float delta) {
        double remainingHP = 0;
        for (Zombie z : waveZombies) {
            if (!z.isDead()) {
                remainingHP += z.getEffectiveHitpoints();
            }
        }

        if (!hpConditionMet) {
            if (totalWaveHP == 0 || remainingHP <= totalWaveHP * 0.25) {
                hpConditionMet = true;
                System.out.println("[WaveManager] HP condition met: remainingHP=" + String.format("%.0f",
                    remainingHP) + " / totalWaveHP=" + String.format("%.0f", totalWaveHP) + " (75% threshold=" +
                    String.format("%.0f", totalWaveHP * 0.25) + ")");
                if (currentWave + 1 < waves.size()) {
                    System.out.println("[WaveManager] The next wave is almost ready...");
                }
            }
        }

        if (hpConditionMet) {
            hpWaitTimer += delta;
            if (hpWaitTimer >= waves.get(currentWave).getStartDelay()) {
                advanceWave();
            }
        }
    }

    private void beginWave() {
        Wave wave = waves.get(currentWave);
        entryIterator = wave.getEntries().iterator();
        spawning = true;
        waveZombies.clear();
        spawned = 0;
        spawnTimer = 0;
        System.out.println("[WaveManager] *** A huge wave of zombies is approaching! (Wave " + (currentWave + 1) + "/" +
            waves.size() + ") ***");
        nextEntry();
    }

    private void nextEntry() {
        currentEntry = entryIterator.next();
        spawned = 0;
        spawnTimer = 0;
    }

    private void finishSpawning() {
        spawning = false;
        totalWaveHP = 0;
        for (Zombie z : waveZombies) {
            totalWaveHP += z.getEffectiveHitpoints();
        }
        waitingForHP = true;
        hpConditionMet = false;
        hpWaitTimer = 0;
        System.out.println("[WaveManager] Finished spawning wave " + (currentWave + 1) + ". waveZombies=" + waveZombies
            .size() + ", totalWaveHP=" + String.format("%.0f", totalWaveHP));
    }

    private void advanceWave() {
        currentWave++;
        waitingForHP = false;
        hpConditionMet = false;
        waveZombies.clear();
        setupTimer = 0;
        hpWaitTimer = 0;
        System.out.println("[WaveManager] Advanced to wave " + (currentWave + 1) + "/" + waves.size());
    }

    private int randomRow() {
        return (int) (Math.random() * 5);
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getTotalWaves() {
        return waves.size();
    }

    public boolean isFinished() {
        return currentWave >= waves.size();
    }

    public int getProgressPercent() {
        if (totalZombieCount == 0) return 0;
        int killed = 0;
        for (Zombie z : allSpawnedWaveZombies) {
            if (z.isDead()) killed++;
        }
        return Math.min(100, killed * 100 / totalZombieCount);
    }

    /**
     * برای هر موج، نسبت (بین ۰ تا ۱) جایگاهش روی نوار پیشروی را برمی‌گرداند — یعنی چند درصد
     * از کل زامبی‌های مرحله باید کشته شوند تا آن موج تمام شود. HUD از این لیست برای رسم
     * پرچم/نشانه‌ی هر موج روی نوار استفاده می‌کند (طبق تصویر ۱۹ سند: «جایگاه موج‌ها روی نوار
     * باید مشخص باشد»). محاسبه بر همان مبنای تجمعیِ تعداد زامبی هر موج است که getProgressPercent
     * هم استفاده می‌کند، برای هم‌خوانی کامل بین عدد پیشروی و جای پرچم‌ها.
     */
    public java.util.List<Float> getWaveMarkerRatios() {
        java.util.List<Float> ratios = new ArrayList<>();
        if (totalZombieCount == 0) {
            return ratios;
        }
        int cumulative = 0;
        for (Wave w : waves) {
            int waveCount = 0;
            for (Wave.WaveEntry e : w.getEntries()) {
                waveCount += e.getCount();
            }
            cumulative += waveCount;
            ratios.add(Math.min(1f, cumulative / (float) totalZombieCount));
        }
        return ratios;
    }
}
