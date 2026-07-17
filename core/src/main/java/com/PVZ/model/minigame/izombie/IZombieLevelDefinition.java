package com.PVZ.model.minigame.izombie;

import java.util.List;

public class IZombieLevelDefinition {

    private int id;
    private int rows = 5;
    private int cols = 9;
    private int redLineCol = 3;
    private int startingSun = 150;
    private List<ZombieOption> zombieRoster;
    private String sunZombieAlias = "ZombieTutorialArmor2Default";
    private double sunProductionBase = 5.0;
    private double sunProductionGrowthPerTick = 0.4;
    private double sunProductionIntervalSeconds = 5.0;
    private double sunProductionCap = 25.0;
    private double plantDensity = 0.55;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }

    public int getRedLineCol() { return redLineCol; }
    public void setRedLineCol(int redLineCol) { this.redLineCol = redLineCol; }

    public int getStartingSun() { return startingSun; }
    public void setStartingSun(int startingSun) { this.startingSun = startingSun; }

    public List<ZombieOption> getZombieRoster() { return zombieRoster; }
    public void setZombieRoster(List<ZombieOption> zombieRoster) { this.zombieRoster = zombieRoster; }

    public String getSunZombieAlias() { return sunZombieAlias; }
    public void setSunZombieAlias(String sunZombieAlias) { this.sunZombieAlias = sunZombieAlias; }

    public double getSunProductionBase() { return sunProductionBase; }
    public void setSunProductionBase(double sunProductionBase) { this.sunProductionBase = sunProductionBase; }

    public double getSunProductionGrowthPerTick() { return sunProductionGrowthPerTick; }
    public void setSunProductionGrowthPerTick(double v) { this.sunProductionGrowthPerTick = v; }

    public double getSunProductionIntervalSeconds() { return sunProductionIntervalSeconds; }
    public void setSunProductionIntervalSeconds(double v) { this.sunProductionIntervalSeconds = v; }

    public double getSunProductionCap() { return sunProductionCap; }
    public void setSunProductionCap(double sunProductionCap) { this.sunProductionCap = sunProductionCap; }

    public double getPlantDensity() { return plantDensity; }
    public void setPlantDensity(double plantDensity) { this.plantDensity = plantDensity; }
}
