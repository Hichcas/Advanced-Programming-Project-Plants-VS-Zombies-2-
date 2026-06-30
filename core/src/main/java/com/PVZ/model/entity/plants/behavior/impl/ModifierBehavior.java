package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;

public class ModifierBehavior implements PlantBehavior {
    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        Object behaviorOverride = plant.getStats().getExtra("behaviorOverride");
        if (behaviorOverride != null) {
            plant.putRuntimeState("activeBehaviorOverride", behaviorOverride);
        }
    }
}
