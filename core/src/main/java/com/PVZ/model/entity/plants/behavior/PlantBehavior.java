package com.PVZ.model.entity.plants.behavior;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.zombies.base.Zombie;

public interface PlantBehavior {

    void onUpdate(
            PlantInstance plant,
            BehaviorContext context,
            double deltaTime
    );

    default void onDamaged(
            PlantInstance plant,
            BehaviorContext context,
            Zombie attacker,
            int damageAmount,
            boolean destroyed
    ) {
    }

}
