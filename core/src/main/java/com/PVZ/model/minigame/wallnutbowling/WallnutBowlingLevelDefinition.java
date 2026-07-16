package com.PVZ.model.minigame.wallnutbowling;

import java.util.List;
import java.util.Map;

public class WallnutBowlingLevelDefinition {

    private int id;
    private int rows = 5;
    private int cols = 9;
    private int redLineCol = 3;
    private List<Map<String, Object>> nutPool;
    private List<String> zombiePool;
    private int totalZombies = 15;
    private double waveIntervalSeconds = 6.0;
    private double minWaveIntervalSeconds = 2.0;
    private double waveIntervalDecreasePerWave = 0.3;
    private double nutSpeed = 450.0;
    private Double nutDamage;
    private Double explosionDamage;
    private Double launchCooldownSeconds;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }

    public int getRedLineCol() { return redLineCol; }
    public void setRedLineCol(int redLineCol) { this.redLineCol = redLineCol; }

    public List<Map<String, Object>> getNutPool() { return nutPool; }
    public void setNutPool(List<Map<String, Object>> nutPool) { this.nutPool = nutPool; }

    public List<String> getZombiePool() { return zombiePool; }
    public void setZombiePool(List<String> zombiePool) { this.zombiePool = zombiePool; }

    public int getTotalZombies() { return totalZombies; }
    public void setTotalZombies(int totalZombies) { this.totalZombies = totalZombies; }

    public double getWaveIntervalSeconds() { return waveIntervalSeconds; }
    public void setWaveIntervalSeconds(double waveIntervalSeconds) { this.waveIntervalSeconds = waveIntervalSeconds; }

    public double getMinWaveIntervalSeconds() { return minWaveIntervalSeconds; }
    public void setMinWaveIntervalSeconds(double minWaveIntervalSeconds) { this.minWaveIntervalSeconds = minWaveIntervalSeconds; }

    public double getWaveIntervalDecreasePerWave() { return waveIntervalDecreasePerWave; }
    public void setWaveIntervalDecreasePerWave(double waveIntervalDecreasePerWave) { this.waveIntervalDecreasePerWave = waveIntervalDecreasePerWave; }

    public double getNutSpeed() { return nutSpeed; }
    public void setNutSpeed(double nutSpeed) { this.nutSpeed = nutSpeed; }

    public Double getNutDamage() { return nutDamage; }
    public void setNutDamage(Double nutDamage) { this.nutDamage = nutDamage; }

    public Double getExplosionDamage() { return explosionDamage; }
    public void setExplosionDamage(Double explosionDamage) { this.explosionDamage = explosionDamage; }

    public Double getLaunchCooldownSeconds() { return launchCooldownSeconds; }
    public void setLaunchCooldownSeconds(Double launchCooldownSeconds) { this.launchCooldownSeconds = launchCooldownSeconds; }
}
