package model.entity.plants.behavior.impl;

import model.entity.plants.PlantInstance;
import model.entity.plants.behavior.BehaviorContext;
import model.entity.plants.behavior.PlantFoodBehavior;

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