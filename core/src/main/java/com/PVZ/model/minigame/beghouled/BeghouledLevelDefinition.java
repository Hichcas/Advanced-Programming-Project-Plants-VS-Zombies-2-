package com.PVZ.model.minigame.beghouled;

import java.util.List;

public class BeghouledLevelDefinition {

    private int id;
    private int rows = 5;
    private int cols = 9;
    private int targetMatches = 20;
    private int startingSun = 200;
    private double zombieSpawnIntervalSeconds = 8.0;

    private List<String> plantTypes;

    private List<String> zombieAliases;

    private List<BeghouledUpgrade> upgrades;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }

    public int getTargetMatches() { return targetMatches; }
    public void setTargetMatches(int targetMatches) { this.targetMatches = targetMatches; }

    public int getStartingSun() { return startingSun; }
    public void setStartingSun(int startingSun) { this.startingSun = startingSun; }

    public double getZombieSpawnIntervalSeconds() { return zombieSpawnIntervalSeconds; }
    public void setZombieSpawnIntervalSeconds(double v) { this.zombieSpawnIntervalSeconds = v; }

    public List<String> getPlantTypes() { return plantTypes; }
    public void setPlantTypes(List<String> plantTypes) { this.plantTypes = plantTypes; }

    public List<String> getZombieAliases() { return zombieAliases; }
    public void setZombieAliases(List<String> zombieAliases) { this.zombieAliases = zombieAliases; }

    public List<BeghouledUpgrade> getUpgrades() { return upgrades; }
    public void setUpgrades(List<BeghouledUpgrade> upgrades) { this.upgrades = upgrades; }
}
