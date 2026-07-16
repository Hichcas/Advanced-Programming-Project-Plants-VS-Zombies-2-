package com.PVZ.model.minigame.vasebreaker;

import com.PVZ.model.enums.PlantType;

public class DroppedSeedPacket {

    private final PlantType plantType;
    private final int row;
    private final int col;
    private float remainingSeconds;

    public DroppedSeedPacket(PlantType plantType, int row, int col, float lifetimeSeconds) {
        this.plantType = plantType;
        this.row = row;
        this.col = col;
        this.remainingSeconds = lifetimeSeconds;
    }

    public PlantType getPlantType() { return plantType; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public float getRemainingSeconds() { return remainingSeconds; }

    public void tick(float delta) {
        remainingSeconds -= delta;
    }

    public boolean isExpired() {
        return remainingSeconds <= 0f;
    }
}
