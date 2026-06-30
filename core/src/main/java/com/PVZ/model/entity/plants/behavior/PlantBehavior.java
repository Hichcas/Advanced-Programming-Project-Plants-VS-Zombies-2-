package com.PVZ.model.entity.plants.behavior;

import com.PVZ.model.entity.plants.PlantInstance;

public interface PlantBehavior {

    void onUpdate(
            PlantInstance plant,
            BehaviorContext context,
            double deltaTime
    );

}
