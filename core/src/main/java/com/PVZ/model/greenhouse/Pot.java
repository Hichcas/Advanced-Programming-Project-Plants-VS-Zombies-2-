package com.PVZ.model.greenhouse;

import com.PVZ.model.enums.PlantType;

public class Pot {
    private boolean unlocked;
    private PlantType plantType;          // null if empty or marigold
    private long plantedTimeMillis;       // 0 if empty
    private boolean marigold;             // true if this pot has a marigold (common flower)

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

    // ---------- getters/setters ----------
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public PlantType getPlantType() { return plantType; }
    public void setPlantType(PlantType plantType) { this.plantType = plantType; }

    public long getPlantedTimeMillis() { return plantedTimeMillis; }
    public void setPlantedTimeMillis(long plantedTimeMillis) { this.plantedTimeMillis = plantedTimeMillis; }

    public boolean isMarigold() { return marigold; }
    public void setMarigold(boolean marigold) { this.marigold = marigold; }

    // ---------- status checks ----------
    /** گلدان کاملاً خالی (نه گیاه معمولی، نه آنلاک‌شده) */
    public boolean isEmpty() {
        return plantType == null && !marigold;
    }

    /** آیا گلدان باز و خالی است (آمادهٔ کاشت) */
    public boolean isReadyForPlanting() {
        return unlocked && isEmpty();
    }

    /** آیا گلدان گیاه دارد (از هر نوع) */
    public boolean hasPlant() {
        return !isEmpty();
    }
}
