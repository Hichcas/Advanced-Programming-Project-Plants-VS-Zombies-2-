package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;

/**
 * Runs several PlantBehaviors on the same plant instance every tick. Used for plants
 * whose base kit is genuinely two behaviors at once (e.g. garlic both blocks zombies
 * like a wall AND periodically pushes them to an adjacent lane).
 */
public class CompositeBehavior implements PlantBehavior {
    private final PlantBehavior[] delegates;

    public CompositeBehavior(PlantBehavior... delegates) {
        this.delegates = delegates;
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
