package model.entity.plants.behavior;

import model.entity.plants.PlantInstance;

public interface PlantFoodBehavior {

    void onPlantFood(
            PlantInstance plant,
            BehaviorContext context
    );
}