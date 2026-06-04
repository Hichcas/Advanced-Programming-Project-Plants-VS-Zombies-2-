package model.greenhouse;

import model.enums.PlantType;

public class Pot {
    private boolean unlocked;
    private PlantType plantType; // null if empty
    private long plantedTimeMillis; // 0 if empty

    public Pot() {
        this.unlocked = false;
        this.plantType = null;
        this.plantedTimeMillis = 0;
    }

    public Pot(boolean unlocked) {
        this.unlocked = unlocked;
        this.plantType = null;
        this.plantedTimeMillis = 0;
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

    /** آیا گلدان خالی است (گیاهی در آن کاشته نشده) */
    public boolean isEmpty() {
        return plantType == null;
    }

    /** آیا گلدان باز و خالی است (آمادهٔ کاشت) */
    public boolean isReadyForPlanting() {
        return unlocked && isEmpty();
    }
}
