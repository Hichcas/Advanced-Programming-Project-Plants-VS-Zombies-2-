package com.PVZ.model.game;

import com.PVZ.model.enums.PlantType;

public interface SeedBarEngine {
    int getSunCount();
    boolean isOnCooldown(PlantType type);
    double getRechargeRemainingSeconds(PlantType type);
    default boolean isConveyorBeltMode() {
        return false;
    }
}
