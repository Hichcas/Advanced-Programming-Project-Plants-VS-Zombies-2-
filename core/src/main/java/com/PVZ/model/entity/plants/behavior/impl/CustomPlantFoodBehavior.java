package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantFoodBehavior;
import com.PVZ.model.game.PlantFoodManager;

public class CustomPlantFoodBehavior implements PlantFoodBehavior {
    private final PlantDefinition definition;

    public CustomPlantFoodBehavior(PlantDefinition definition) {
        this.definition = definition;
    }

    @Override
    public void onPlantFood(PlantInstance plant, BehaviorContext context) {
        PlantFoodManager.applyCustomPlantFood(definition, plant, context);
    }
}
