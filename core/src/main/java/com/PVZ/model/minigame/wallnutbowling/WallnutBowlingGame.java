package com.PVZ.model.minigame.wallnutbowling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class WallnutBowlingGame {

    private final int rows;
    private final int cols;
    private final int redLineCol;
    private final double nutSpeed;
    private final double nutDamage;
    private final double explosionDamage;
    private final double launchCooldownSeconds;
    private double cooldownRemaining = 0.0;

    private final List<String> zombiePool;
    private final List<NutType> weightedNutPool = new ArrayList<>();
    private final Random random = new Random();

    private final List<BowlingNut> nuts = new ArrayList<>();

    private NutType heldNut;

    private final int totalZombies;
    private int zombiesSpawned = 0;
    private double waveTimer;
    private double currentWaveInterval;
    private final double minWaveInterval;
    private final double waveIntervalDecrease;

    private boolean won = false;
    private boolean lost = false;

    public WallnutBowlingGame(WallnutBowlingLevelDefinition level) {
        this.rows = level.getRows();
        this.cols = level.getCols();
        this.redLineCol = Math.max(0, Math.min(level.getRedLineCol(), cols - 1));
        this.nutSpeed = level.getNutSpeed();
        this.nutDamage = level.getNutDamage() != null ? level.getNutDamage() : 190.0;
        this.explosionDamage = level.getExplosionDamage() != null ? level.getExplosionDamage() : 1800.0;
        this.launchCooldownSeconds = level.getLaunchCooldownSeconds() != null ? level.getLaunchCooldownSeconds() : 2.5;
        this.totalZombies = level.getTotalZombies();
        this.currentWaveInterval = level.getWaveIntervalSeconds();
        this.minWaveInterval = level.getMinWaveIntervalSeconds();
        this.waveIntervalDecrease = level.getWaveIntervalDecreasePerWave();
        this.waveTimer = currentWaveInterval;
        this.zombiePool = resolveZombiePool(level.getZombiePool());
        buildWeightedNutPool(level.getNutPool());
        drawNextNut();
    }

    private List<String> resolveZombiePool(List<String> names) {
        List<String> pool = new ArrayList<>();
        if (names != null && !names.isEmpty()) {
            pool.addAll(names);
        }
        if (pool.isEmpty()) {
            for (com.PVZ.model.enums.ZombieType type : com.PVZ.model.enums.ZombieType.values()) {
                pool.add(type.alias);
            }
        }
        return pool;
    }

    private void buildWeightedNutPool(List<Map<String, Object>> nutPoolConfig) {
        if (nutPoolConfig != null && !nutPoolConfig.isEmpty()) {
            for (Map<String, Object> entry : nutPoolConfig) {
                Object typeObj = entry.get("type");
                Object weightObj = entry.get("weight");
                if (typeObj == null) continue;
                NutType type;
                try {
                    type = NutType.valueOf(typeObj.toString().trim().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                int weight = weightObj instanceof Number ? ((Number) weightObj).intValue() : 1;
                for (int i = 0; i < Math.max(1, weight); i++) {
                    weightedNutPool.add(type);
                }
            }
        }
        if (weightedNutPool.isEmpty()) {
            weightedNutPool.add(NutType.NORMAL);
            weightedNutPool.add(NutType.NORMAL);
            weightedNutPool.add(NutType.NORMAL);
            weightedNutPool.add(NutType.EXPLOSIVE);
            weightedNutPool.add(NutType.GIANT);
        }
    }


    public NutType getHeldNut() {
        return heldNut;
    }

    public NutType drawNextNut() {
        heldNut = weightedNutPool.get(random.nextInt(weightedNutPool.size()));
        return heldNut;
    }

    public boolean canLaunchAt(int row, int col) {
        return heldNut != null && cooldownRemaining <= 0.0
                && row >= 0 && row < rows && col >= 0 && col <= redLineCol;
    }

    public NutType consumeHeldNut() {
        NutType type = heldNut;
        heldNut = null;
        cooldownRemaining = launchCooldownSeconds;
        return type;
    }

    public void tickCooldown(float delta) {
        if (cooldownRemaining > 0.0) {
            cooldownRemaining = Math.max(0.0, cooldownRemaining - delta);
        }
    }

    public double getCooldownRemaining() { return cooldownRemaining; }
    public double getLaunchCooldownSeconds() { return launchCooldownSeconds; }

    public List<BowlingNut> getNuts() {
        return nuts;
    }

    public void addNut(BowlingNut nut) {
        nuts.add(nut);
    }

    public String randomZombieAlias() {
        return zombiePool.get(random.nextInt(zombiePool.size()));
    }

    public int nextWaveRow() {
        return random.nextInt(rows);
    }

    public List<Integer> nextWaveRows(int count) {
        List<Integer> pool = new ArrayList<>();
        for (int r = 0; r < rows; r++) pool.add(r);
        java.util.Collections.shuffle(pool, random);
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(pool.get(i % pool.size()));
        }
        return result;
    }

    private static final int MIN_BURST_SIZE = 5;
    private static final int MAX_BURST_SIZE = 6;

    public int tickWave(float delta) {
        if (zombiesSpawned >= totalZombies) return 0;
        waveTimer -= delta;
        if (waveTimer <= 0) {
            waveTimer = currentWaveInterval;
            currentWaveInterval = Math.max(minWaveInterval, currentWaveInterval - waveIntervalDecrease);
            int remaining = totalZombies - zombiesSpawned;
            int burst = MIN_BURST_SIZE + random.nextInt(MAX_BURST_SIZE - MIN_BURST_SIZE + 1);
            burst = Math.min(burst, remaining);
            zombiesSpawned += burst;
            return burst;
        }
        return 0;
    }

    public boolean allZombiesSpawned() {
        return zombiesSpawned >= totalZombies;
    }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getRedLineCol() { return redLineCol; }
    public double getNutSpeed() { return nutSpeed; }
    public double getNutDamage() { return nutDamage; }
    public double getExplosionDamage() { return explosionDamage; }
    public int getTotalZombies() { return totalZombies; }
    public int getZombiesSpawned() { return zombiesSpawned; }
    public boolean isWon() { return won; }
    public boolean isLost() { return lost; }
    public boolean isFinished() { return won || lost; }
    public void markWon() { won = true; }
    public void markLost() { lost = true; }
}
