package com.PVZ.model.minigame.izombie;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class IZombieGame {

    private final int rows;
    private final int cols;
    private final int redLineCol;

    private int sun;
    private final List<ZombieOption> roster;

    private final double sunProductionBase;
    private final double sunProductionGrowthPerTick;
    private final double sunProductionIntervalSeconds;
    private final double sunProductionCap;
    private double sunProductionTimer;
    private double currentSunRate;
    private final double plantDensity;
    private final boolean[] sunZombieAlive;
    private final String sunZombieAlias;
    private final Set<Integer> brainsEaten = new HashSet<>();

    private boolean won = false;
    private boolean lost = false;

    public IZombieGame(IZombieLevelDefinition level) {
        this.rows = level.getRows();
        this.cols = level.getCols();
        this.redLineCol = Math.max(0, Math.min(level.getRedLineCol(), cols - 1));
        this.sun = level.getStartingSun();
        this.roster = level.getZombieRoster() != null ? new ArrayList<>(level.getZombieRoster()) : new ArrayList<>();
        this.sunZombieAlias = level.getSunZombieAlias();
        this.sunProductionBase = level.getSunProductionBase();
        this.sunProductionGrowthPerTick = level.getSunProductionGrowthPerTick();
        this.sunProductionIntervalSeconds = level.getSunProductionIntervalSeconds();
        this.sunProductionCap = level.getSunProductionCap();
        this.currentSunRate = sunProductionBase;
        this.sunProductionTimer = sunProductionIntervalSeconds;
        this.plantDensity = level.getPlantDensity();
        this.sunZombieAlive = new boolean[rows];
        java.util.Arrays.fill(sunZombieAlive, true);
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getRedLineCol() { return redLineCol; }

    public boolean isInDeployZone(int col) { return col > redLineCol && col < cols; }

    public int getSun() { return sun; }
    public void addSun(int amount) { sun = Math.max(0, sun + amount); }

    public List<ZombieOption> getRoster() { return roster; }

    public ZombieOption findOption(String alias) {
        for (ZombieOption o : roster) {
            if (o.getAlias().equalsIgnoreCase(alias)) return o;
        }
        return null;
    }

    public boolean trySpend(ZombieOption option) {
        if (option == null || sun < option.getCost()) return false;
        sun -= option.getCost();
        return true;
    }

    public String getSunZombieAlias() { return sunZombieAlias; }

    public boolean isSunZombieAlive(int row) {
        return row >= 0 && row < rows && sunZombieAlive[row];
    }

    public void markSunZombieDead(int row) {
        if (row >= 0 && row < rows) sunZombieAlive[row] = false;
    }

    public int tickSunProduction(float delta) {
        sunProductionTimer -= delta;
        if (sunProductionTimer > 0) return 0;
        sunProductionTimer = sunProductionIntervalSeconds;
        currentSunRate = Math.min(sunProductionCap, currentSunRate + sunProductionGrowthPerTick);

        int produced = 0;
        for (int r = 0; r < rows; r++) {
            if (sunZombieAlive[r]) {
                produced += (int) Math.round(currentSunRate);
            }
        }
        if (produced > 0) addSun(produced);
        return produced;
    }

    public double getCurrentSunRate() { return currentSunRate; }

    public double getPlantDensity() { return plantDensity; }

    public boolean isBrainEaten(int row) { return brainsEaten.contains(row); }

    public boolean eatBrain(int row) {
        if (row < 0 || row >= rows || brainsEaten.contains(row)) return false;
        brainsEaten.add(row);
        if (brainsEaten.size() >= rows) {
            won = true;
        }
        return true;
    }

    public int getBrainsRemaining() { return rows - brainsEaten.size(); }

    public boolean isWon() { return won; }
    public boolean isLost() { return lost; }
    public boolean isFinished() { return won || lost; }
    public void markLost() { lost = true; }
    public void markWon() { won = true; }
}
