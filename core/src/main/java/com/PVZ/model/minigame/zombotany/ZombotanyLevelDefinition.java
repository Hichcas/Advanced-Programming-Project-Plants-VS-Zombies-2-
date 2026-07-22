package com.PVZ.model.minigame.zombotany;

import java.util.List;

public class ZombotanyLevelDefinition {

    private int id;
    private int rows = 5;
    private int cols = 9;
    private int startingSun = 150;
    private List<String> plantPool;
    private List<WaveDef> waves;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }

    public int getStartingSun() { return startingSun; }
    public void setStartingSun(int startingSun) { this.startingSun = startingSun; }

    public List<String> getPlantPool() { return plantPool; }
    public void setPlantPool(List<String> plantPool) { this.plantPool = plantPool; }

    public List<WaveDef> getWaves() { return waves; }
    public void setWaves(List<WaveDef> waves) { this.waves = waves; }

    public static class WaveDef {
        private float startDelay;
        private List<WaveEntryDef> entries;

        public float getStartDelay() { return startDelay; }
        public void setStartDelay(float startDelay) { this.startDelay = startDelay; }

        public List<WaveEntryDef> getEntries() { return entries; }
        public void setEntries(List<WaveEntryDef> entries) { this.entries = entries; }
    }

    public static class WaveEntryDef {
        private String zombie;
        private int count = 1;
        private float spawnDelay = 2.0f;

        public String getZombie() { return zombie; }
        public void setZombie(String zombie) { this.zombie = zombie; }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public float getSpawnDelay() { return spawnDelay; }
        public void setSpawnDelay(float spawnDelay) { this.spawnDelay = spawnDelay; }
    }
}
