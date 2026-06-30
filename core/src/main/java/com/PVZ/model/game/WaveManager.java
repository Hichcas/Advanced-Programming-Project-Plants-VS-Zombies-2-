package com.PVZ.model.game;

import java.util.Iterator;
import java.util.List;

public class WaveManager {
    private List<Wave> waves;
    private int currentWave = 0;
    private float timer = 0;
    private boolean active = false;

    private Iterator<Wave.WaveEntry> entryIterator;
    private Wave.WaveEntry currentEntry;
    private int spawned = 0;
    private float spawnTimer = 0;

    public WaveManager(List<Wave> waves) {
        this.waves = waves;
    }

    public void update(float delta, ZombieEngine engine) {
        if (currentWave >= waves.size()) return;

        Wave wave = waves.get(currentWave);

        if (!active) {
            timer += delta;
            if (timer >= wave.getStartDelay()) {
                active = true;
                entryIterator = wave.getEntries().iterator();
                nextEntry();
            }
            return;
        }

        spawnTimer += delta;
        if (spawnTimer >= currentEntry.getSpawnDelay() && spawned < currentEntry.getCount()) {
            engine.spawnZombie(currentEntry.getZombieAlias(), randomRow(), 950);
            spawned++;
            spawnTimer = 0;
        }

        if (spawned >= currentEntry.getCount()) {
            if (entryIterator.hasNext()) {
                nextEntry();
            } else {
                currentWave++;
                active = false;
                timer = 0;
            }
        }
    }

    private void nextEntry() {
        currentEntry = entryIterator.next();
        spawned = 0;
        spawnTimer = 0;
    }

    private int randomRow() {
        return (int) (Math.random() * 5);
    }

    public int getCurrentWave() { return currentWave; }
    public int getTotalWaves() { return waves.size(); }
    public boolean isFinished() { return currentWave >= waves.size(); }
}
