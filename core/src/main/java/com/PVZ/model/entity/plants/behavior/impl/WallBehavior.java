package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;

public class WallBehavior implements PlantBehavior {
    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        Integer lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
        Integer row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            return;
        }

        for (Zombie zombie : zombies) {
            if (zombie != null) {
                zombie.stopMoving();
            }
        }

        if (plant.getStats().getSunDropAmount() > 0) {
            Double sunTimer = asDouble(plant.getRuntimeState().getOrDefault("sunDropTimer", 0.0), 0.0);
            sunTimer += deltaTime;
            if (sunTimer >= 1.0) {
                context.spawnSunAt(row, asInt(plant.getRuntimeState().getOrDefault("col", 0), 0), plant.getStats()
                        .getSunDropAmount());
                sunTimer = 0.0;
            }
            plant.putRuntimeState("sunDropTimer", sunTimer);
        }

        if (plant.getStats().getReflectDamage() > 0) {
            context.damageArea(lane, row, plant.getStats().getReflectDamage());
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

    private static double asDouble(Object value, double defaultValue) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null ? defaultValue : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
