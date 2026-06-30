package com.PVZ.model.game;

import java.util.List;

public class Wave {
    private final List<WaveEntry> entries;
    private final float startDelay;

    public  Wave(List<WaveEntry>entries, float startDelay) {
        this.entries = entries;
        this.startDelay = startDelay;
    }

    public static class WaveEntry{
        private final String zomeAlias;
        private final int count;
        private final float spawnDelay ;

        public WaveEntry(String zomeAtlias, int count, float spawnDelay) {
            this.zomeAlias = zomeAtlias;
            this.count = count;
            this.spawnDelay = spawnDelay;
        }

        public String getZomeAlias() {
            return zomeAlias;
        }
        public int getCount() {
            return count;
        }
        public float getSpawnDelay() {
            return spawnDelay;
        }

    }

    public List<WaveEntry> getEntries() { return entries; }
    public float getStartDelay() { return startDelay; }
}


//how to make wave in game:
/*
private List<Wave> createWaves() {
    List<Wave> waves = new ArrayList<>();
    List<Wave.WaveEntry> e2 = new ArrayList<>();
    e2.add(new Wave.WaveEntry("ZombieTutorialDefault", 5, 1.5f));
    e2.add(new Wave.WaveEntry("ZombieTutorialArmor1Default", 2, 3f));
    waves.add(new Wave(e2, 10f));
    return waves;
}
 */
