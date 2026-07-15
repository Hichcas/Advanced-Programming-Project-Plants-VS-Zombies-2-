package com.PVZ.model.minigame.vasebreaker;

import java.util.List;

public class VasebreakerLevelDefinition {

    private int id;
    private int rows;
    private int cols;
    private List<String> vases;
    private List<String> zombiePool;
    private List<String> plantPool;
    private float seedPacketLifetimeSeconds = 8f;

    // ── Randomization controls ──
    // When true (or when no explicit `vases` list is supplied) the engine builds
    // a fresh random board every time the level is started: which cells hold a
    // vase and what each vase contains (plant / zombie / gargantuar) is decided
    // randomly, so no two runs look the same.
    private Boolean random;
    private Integer vaseCount;
    private Double plantVaseChance = 0.20;
    private Double gargantuarVaseChance = 0.08;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }

    public List<String> getVases() { return vases; }
    public void setVases(List<String> vases) { this.vases = vases; }

    public List<String> getZombiePool() { return zombiePool; }
    public void setZombiePool(List<String> zombiePool) { this.zombiePool = zombiePool; }

    public List<String> getPlantPool() { return plantPool; }
    public void setPlantPool(List<String> plantPool) { this.plantPool = plantPool; }

    public float getSeedPacketLifetimeSeconds() { return seedPacketLifetimeSeconds; }
    public void setSeedPacketLifetimeSeconds(float seedPacketLifetimeSeconds) { this.seedPacketLifetimeSeconds = seedPacketLifetimeSeconds; }

    public Boolean getRandom() { return random; }
    public void setRandom(Boolean random) { this.random = random; }

    public Integer getVaseCount() { return vaseCount; }
    public void setVaseCount(Integer vaseCount) { this.vaseCount = vaseCount; }

    public Double getPlantVaseChance() { return plantVaseChance; }
    public void setPlantVaseChance(Double plantVaseChance) { this.plantVaseChance = plantVaseChance; }

    public Double getGargantuarVaseChance() { return gargantuarVaseChance; }
    public void setGargantuarVaseChance(Double gargantuarVaseChance) { this.gargantuarVaseChance = gargantuarVaseChance; }
}
