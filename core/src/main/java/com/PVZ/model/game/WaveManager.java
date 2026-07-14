package com.PVZ.model.game;

import com.PVZ.model.entity.zombies.base.Zombie;

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

    public WaveManager(List<Wave> waves) {
        this.waves = waves;
    }

    public void start() { started = true; }

    public void update(float delta, ZombieEngine engine) {
        if (!started || currentWave >= waves.size()) return;

        if (!spawning && !waitingForHP) {
            if (currentWave == 0) {
                setupTimer += delta;
                if (setupTimer < waves.get(0).getStartDelay()) {
                    return;
                }
            }
            beginWave();
        }

        if (spawning) {
            spawnTimer += delta;
            if (spawnTimer >= currentEntry.getSpawnDelay() && spawned < currentEntry.getCount()) {
                Zombie z = engine.spawnZombie(currentEntry.getZombieAlias(), randomRow(), 8);
                if (z != null) {
                    waveZombies.add(z);
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

        if (waitingForHP) {
            double remainingHP = 0;
            for (Zombie z : waveZombies) {
                if (!z.isDead()) {
                    remainingHP += z.getEffectiveHitpoints();
                }
            }
            if (!hpConditionMet) {
                if (totalWaveHP == 0 || remainingHP <= totalWaveHP * 0.25) {
                    hpConditionMet = true;
                }
            }
            if (hpConditionMet) {
                hpWaitTimer += delta;
                if (hpWaitTimer >= waves.get(currentWave).getStartDelay()) {
                    advanceWave();
                }
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
    }

    private void advanceWave() {
        currentWave++;
        waitingForHP = false;
        hpConditionMet = false;
        waveZombies.clear();
        setupTimer = 0;
        hpWaitTimer = 0;
    }

    private int randomRow() {
        return (int) (Math.random() * 5);
    }

    public int getCurrentWave() { return currentWave; }
    public int getTotalWaves() { return waves.size(); }
    public boolean isFinished() { return currentWave >= waves.size(); }
}
