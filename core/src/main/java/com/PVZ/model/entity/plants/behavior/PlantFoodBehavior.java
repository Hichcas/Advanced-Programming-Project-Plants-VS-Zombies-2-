package com.PVZ.model.entity.plants.behavior;

import com.PVZ.model.entity.plants.PlantInstance;

public interface PlantFoodBehavior {

    void onPlantFood(
            PlantInstance plant,
            BehaviorContext context
    );
}
