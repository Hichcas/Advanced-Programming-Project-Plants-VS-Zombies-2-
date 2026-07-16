package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;


public class TorchwoodBehavior implements PlantBehavior {

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);

        applyAura(plant, context, row, col - 1);
        applyAura(plant, context, row, col + 1);
    }

    private void applyAura(PlantInstance plant, BehaviorContext context, int row, int col) {
        Plant neighbor = context.getPlantAt(row, col);
        if (neighbor == null || neighbor.isDead()) {
            return;
        }
        neighbor.getStats().putExtra("fireAttack", Boolean.TRUE);
        neighbor.getStats().putExtra("meltBoost", Boolean.TRUE);
        double desiredMultiplier = plant != null && plant.isPlantFoodActive() ? 3.0 : 2.0;
        double currentMultiplier = neighbor.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (currentMultiplier < desiredMultiplier) {
            neighbor.getStats().putExtra("damageMultiplier", desiredMultiplier);
        }
    }

    private static int asInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
