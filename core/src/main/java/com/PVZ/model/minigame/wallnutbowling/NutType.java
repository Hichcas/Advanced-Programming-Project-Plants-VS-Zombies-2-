package com.PVZ.model.minigame.wallnutbowling;

import com.PVZ.model.enums.PlantType;

public enum NutType {
    NORMAL(PlantType.WALLNUT_BOWLING),
    EXPLOSIVE(PlantType.EXPLODE_O_NUT),
    GIANT(PlantType.GIANT_WALLNUT);

    private final PlantType plantType;

    NutType(PlantType plantType) {
        this.plantType = plantType;
    }

    public PlantType getPlantType() {
        return plantType;
    }
}
