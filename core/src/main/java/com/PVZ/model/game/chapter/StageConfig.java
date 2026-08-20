package com.PVZ.model.game.chapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StageConfig {
    private int stageNumber;
    private String type;
    private int plantLimit;
    private int rows;
    private int cols;
    private String mapTexture;
    private boolean disableFallingSun;
    private int initialSun;
    private boolean manualWaveStart;
    private double conveyorInterval;
    private List<String> lockedPlants;
    private List<FamilyLockEntry> lockedFamilies;
    private String specialLevel;
    private int maxPlantDeaths;
    private int deadlineCol;
    private double timedWarSeconds;
    private int timedWarZombieKills;
    private int timedWarSunTarget;
    private List<TombstoneEntry> tombstones;
    private List<TileEntry> tiles;
    private List<WaveEntry> waves;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TileEntry {
        private int row;
        private int col;
        private String type;

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getCol() {
            return col;
        }

        public void setCol(int col) {
            this.col = col;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FamilyLockEntry {
        private String family;

        public String getFamily() {
            return family;
        }

        public void setFamily(String family) {
            this.family = family;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TombstoneEntry {
        private int row;
        private int col;
        private int hp;

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getCol() {
            return col;
        }

        public void setCol(int col) {
            this.col = col;
        }

        public int getHp() {
            return hp;
        }

        public void setHp(int hp) {
            this.hp = hp;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WaveEntry {
        private double startDelay;
        private boolean isFinal;
        private List<ZombieSpawn> entries;

        public double getStartDelay() {
            return startDelay;
        }

        public void setStartDelay(double startDelay) {
            this.startDelay = startDelay;
        }

        public boolean isFinal() {
            return isFinal;
        }

        public void setFinal(boolean isFinal) {
            this.isFinal = isFinal;
        }

        public List<ZombieSpawn> getEntries() {
            return entries;
        }

        public void setEntries(List<ZombieSpawn> entries) {
            this.entries = entries;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ZombieSpawn {
        private String zombie;
        private int count;
        private double spawnDelay;

        public String getZombie() {
            return zombie;
        }

        public void setZombie(String zombie) {
            this.zombie = zombie;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public double getSpawnDelay() {
            return spawnDelay;
        }

        public void setSpawnDelay(double spawnDelay) {
            this.spawnDelay = spawnDelay;
        }
    }

    public int getStageNumber() {
        return stageNumber;
    }

    public void setStageNumber(int stageNumber) {
        this.stageNumber = stageNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPlantLimit() {
        return plantLimit;
    }

    public void setPlantLimit(int plantLimit) {
        this.plantLimit = plantLimit;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public String getMapTexture() {
        return mapTexture;
    }

    public void setMapTexture(String mapTexture) {
        this.mapTexture = mapTexture;
    }

    public boolean isDisableFallingSun() {
        return disableFallingSun;
    }

    public void setDisableFallingSun(boolean disableFallingSun) {
        this.disableFallingSun = disableFallingSun;
    }

    public int getInitialSun() {
        return initialSun;
    }

    public void setInitialSun(int initialSun) {
        this.initialSun = initialSun;
    }

    public boolean isManualWaveStart() {
        return manualWaveStart;
    }

    public void setManualWaveStart(boolean manualWaveStart) {
        this.manualWaveStart = manualWaveStart;
    }

    public double getConveyorInterval() {
        return conveyorInterval;
    }

    public void setConveyorInterval(double conveyorInterval) {
        this.conveyorInterval = conveyorInterval;
    }

    public List<String> getLockedPlants() {
        return lockedPlants;
    }

    public void setLockedPlants(List<String> lockedPlants) {
        this.lockedPlants = lockedPlants;
    }

    public List<FamilyLockEntry> getLockedFamilies() {
        return lockedFamilies;
    }

    public void setLockedFamilies(List<FamilyLockEntry> lockedFamilies) {
        this.lockedFamilies = lockedFamilies;
    }

    public String getSpecialLevel() {
        return specialLevel;
    }

    public void setSpecialLevel(String specialLevel) {
        this.specialLevel = specialLevel;
    }

    public int getMaxPlantDeaths() {
        return maxPlantDeaths;
    }

    public void setMaxPlantDeaths(int maxPlantDeaths) {
        this.maxPlantDeaths = maxPlantDeaths;
    }

    public int getDeadlineCol() {
        return deadlineCol;
    }

    public void setDeadlineCol(int deadlineCol) {
        this.deadlineCol = deadlineCol;
    }

    public double getTimedWarSeconds() {
        return timedWarSeconds;
    }

    public void setTimedWarSeconds(double timedWarSeconds) {
        this.timedWarSeconds = timedWarSeconds;
    }

    public int getTimedWarZombieKills() {
        return timedWarZombieKills;
    }

    public void setTimedWarZombieKills(int timedWarZombieKills) {
        this.timedWarZombieKills = timedWarZombieKills;
    }

    public int getTimedWarSunTarget() {
        return timedWarSunTarget;
    }

    public void setTimedWarSunTarget(int timedWarSunTarget) {
        this.timedWarSunTarget = timedWarSunTarget;
    }

    public List<TombstoneEntry> getTombstones() {
        return tombstones;
    }

    public void setTombstones(List<TombstoneEntry> tombstones) {
        this.tombstones = tombstones;
    }

    public List<TileEntry> getTiles() {
        return tiles;
    }

    public void setTiles(List<TileEntry> tiles) {
        this.tiles = tiles;
    }

    public List<WaveEntry> getWaves() {
        return waves;
    }

    public void setWaves(List<WaveEntry> waves) {
        this.waves = waves;
    }
}
