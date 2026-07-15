package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.enums.PlantCategory;


public class MintBehavior implements PlantBehavior {
    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        Boolean triggered = (Boolean) plant.getRuntimeState().getOrDefault("mintTriggered", Boolean.FALSE);
        if (triggered != null && triggered) {
            return;
        }
        plant.putRuntimeState("mintTriggered", Boolean.TRUE);

        PlantCategory family = plant.getDefinition() == null ? null : plant.getDefinition().getCategoryEnum();
        if (family == null) {
            return;
        }

        int selfRow = asInt(plant.getRuntimeState().getOrDefault("row", -1), -1);
        int selfCol = asInt(plant.getRuntimeState().getOrDefault("col", -1), -1);

        for (Plant other : context.getAllPlants()) {
            if (other == null || other.getDefinition() == null) {
                continue;
            }
            int otherRow = asInt(other.getRuntimeState("row"), -2);
            int otherCol = asInt(other.getRuntimeState("col"), -2);
            boolean isSelf = otherRow == selfRow && otherCol == selfCol;
            if (isSelf) {
                continue;
            }
            if (other.getDefinition().getCategoryEnum() == family) {
                other.applyPlantFood(context);
            }
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
