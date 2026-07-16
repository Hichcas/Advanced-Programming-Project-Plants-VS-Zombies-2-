package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;


public class CompositeBehavior implements PlantBehavior {
    private final PlantBehavior[] delegates;

    public CompositeBehavior(PlantBehavior... delegates) {
        this.delegates = delegates;
    }


    @Override
    public void onDamaged(PlantInstance plant, BehaviorContext context, Zombie attacker, int damageAmount, boolean destroyed) {
        for (PlantBehavior delegate : delegates) {
            if (delegate != null) {
                delegate.onDamaged(plant, context, attacker, damageAmount, destroyed);
            }
        }
    }

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        for (PlantBehavior delegate : delegates) {
            if (delegate != null) {
                delegate.onUpdate(plant, context, deltaTime);
            }
        }
    }
}
