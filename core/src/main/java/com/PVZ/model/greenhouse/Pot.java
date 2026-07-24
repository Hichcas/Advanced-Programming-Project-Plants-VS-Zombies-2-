package com.PVZ.model.greenhouse;

import com.PVZ.model.enums.PlantType;

public class Pot {
    private boolean unlocked;
    private PlantType plantType;
    private long plantedTimeMillis;
    private boolean marigold;

    public Pot() {
        this.unlocked = false;
        this.plantType = null;
        this.plantedTimeMillis = 0;
        this.marigold = false;
    }

    public Pot(boolean unlocked) {
        this.unlocked = unlocked;
        this.plantType = null;
        this.plantedTimeMillis = 0;
        this.marigold = false;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public void setPlantType(PlantType plantType) {
        this.plantType = plantType;
    }

    public long getPlantedTimeMillis() {
        return plantedTimeMillis;
    }

    public void setPlantedTimeMillis(long plantedTimeMillis) {
        this.plantedTimeMillis = plantedTimeMillis;
    }

    public boolean isMarigold() {
        return marigold;
    }

    public void setMarigold(boolean marigold) {
        this.marigold = marigold;
    }

    public boolean isEmpty() {
        return plantType == null && !marigold;
    }

    public boolean isReadyForPlanting() {
        return unlocked && isEmpty();
    }

    public boolean hasPlant() {
        return !isEmpty();
    }
}
