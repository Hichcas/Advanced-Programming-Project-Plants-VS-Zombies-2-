package model.entity.plants.behavior;

import model.entity.plants.PlantInstance;

public interface PlantBehavior {

    void onUpdate(
            PlantInstance plant,
            BehaviorContext context,
            double deltaTime
    );

}