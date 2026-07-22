package com.PVZ.model.minigame.zombotany;

import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.Wave;

import java.util.ArrayList;
import java.util.List;

public class ZombotanyGame {

    private final int rows;
    private final int cols;
    private final int startingSun;

    private int sun;
    private final List<PlantType> plantPool;
    private final List<Wave> waves;

    private boolean won = false;
    private boolean lost = false;

    public ZombotanyGame(ZombotanyLevelDefinition level) {
        this.rows = level.getRows();
        this.cols = level.getCols();
        this.startingSun = level.getStartingSun();
        this.sun = level.getStartingSun();
        this.plantPool = resolvePlantPool(level.getPlantPool());
        this.waves = buildWaves(level.getWaves());
    }

    private static List<PlantType> resolvePlantPool(List<String> rawNames) {
        List<PlantType> pool = new ArrayList<>();
        if (rawNames == null) {
            return pool;
        }
        for (String name : rawNames) {
            if (name == null || name.isBlank()) {
                continue;
            }
            try {
                PlantType type = PlantType.fromName(name);
                if (type != null && !pool.contains(type)) {
                    pool.add(type);
                }
            } catch (RuntimeException ex) {
                System.err.println("[ZombotanyGame] unknown plant in pool: " + name);
            }
        }
        return pool;
    }

    private static List<Wave> buildWaves(List<ZombotanyLevelDefinition.WaveDef> defs) {
        List<Wave> waves = new ArrayList<>();
        if (defs == null) {
            return waves;
        }
        for (ZombotanyLevelDefinition.WaveDef waveDef : defs) {
            if (waveDef == null) {
                continue;
            }
            List<Wave.WaveEntry> entries = new ArrayList<>();
            if (waveDef.getEntries() != null) {
                for (ZombotanyLevelDefinition.WaveEntryDef entryDef : waveDef.getEntries()) {
                    if (entryDef == null || entryDef.getZombie() == null) {
                        continue;
                    }
                    entries.add(new Wave.WaveEntry(
                            entryDef.getZombie(),
                            entryDef.getCount(),
                            entryDef.getSpawnDelay()));
                }
            }
            waves.add(new Wave(entries, waveDef.getStartDelay()));
        }
        return waves;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public int getStartingSun() { return startingSun; }
    public int getSun() { return sun; }
    public void addSun(int amount) { sun = Math.max(0, sun + amount); }
    public void setSun(int sun) { this.sun = Math.max(0, sun); }

    public List<PlantType> getPlantPool() { return plantPool; }
    public boolean isPlantAllowed(PlantType type) { return type != null && plantPool.contains(type); }

    public List<Wave> getWaves() { return waves; }

    public boolean isWon() { return won; }
    public boolean isLost() { return lost; }
    public boolean isFinished() { return won || lost; }
    public void markWon() { won = true; }
    public void markLost() { lost = true; }
}
