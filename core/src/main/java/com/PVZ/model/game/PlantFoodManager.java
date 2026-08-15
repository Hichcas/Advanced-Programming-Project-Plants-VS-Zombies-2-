package com.PVZ.model.game;

import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.impl.ManualPlantFoodBehavior;

public class PlantFoodManager {
    private int plantFoodCount;

    public static void applyCustomPlantFood(PlantDefinition definition, PlantInstance plant, BehaviorContext context) {
        if (plant == null) {
            return;
        }
        new ManualPlantFoodBehavior(definition,
            definition == null ? null : definition.getPlantFoodEffect())
            .onPlantFood(plant, context);
    }

    public int getPlantFoodCount() {
        return plantFoodCount;
    }

    public void setPlantFoodCount(int plantFoodCount) {
        this.plantFoodCount = Math.max(0, plantFoodCount);
    }

    public void addPlantFood(int amount) {
        if (amount > 0) {
            plantFoodCount += amount;
        }
    }

    public boolean consumePlantFood() {
        if (plantFoodCount <= 0) {
            return false;
        }
        plantFoodCount--;
        return true;
    }

    public void reset() {
        plantFoodCount = 0;
    }
}
