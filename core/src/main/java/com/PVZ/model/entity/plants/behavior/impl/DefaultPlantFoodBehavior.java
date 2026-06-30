package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantFoodBehavior;

public class DefaultPlantFoodBehavior implements PlantFoodBehavior {

    @Override
    public void onPlantFood(
            PlantInstance plant,
            BehaviorContext context
    ) {
        if (plant == null) {
            return;
        }

        plant.setPlantFoodActive(true);
        plant.setPlantFoodTicksRemaining(5);
    }
}
