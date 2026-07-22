package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;


public class UtilityLaneBehavior implements PlantBehavior {

    public enum Mode { MAGNET_DISARM, MOVE_ZOMBIES, HYPNOTIZE }

    private final Mode mode;

    public UtilityLaneBehavior(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        double timer = asDouble(plant.getRuntimeState().getOrDefault("utilityTimer", 0.0), 0.0);
        timer += deltaTime;

        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = mode == Mode.MAGNET_DISARM ? 2.0 : 3.0;
        }

        if (timer < cooldown) {
            plant.putRuntimeState("utilityTimer", timer);
            return;
        }

        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
        switch (mode) {
            case MAGNET_DISARM -> context.disarmZombiesInLane(lane);
            case HYPNOTIZE -> {
                String plantKey = plant.getDefinition() == null
                        || plant.getDefinition().getPlantKey() == null
                        ? "" : plant.getDefinition().getPlantKey().toLowerCase();
                // Hypno-shroom only hypnotizes the zombie that eats it (handled in onDamaged);
                // it must NOT periodically hypnotize the whole lane. Other HYPNOTIZE plants
                // (e.g. Caulipower) keep the lane-hypnotize behavior.
                if (!"hypno_shroom".equals(plantKey)) {
                    context.hypnotizeZombiesInLane(lane, 999.0);
                }
            }
            case MOVE_ZOMBIES -> {
                context.moveZombiesFromLane(lane, lane + 1);
                context.moveZombiesFromLane(lane, lane - 1);
            }
        }

        plant.putRuntimeState("utilityTimer", 0.0);
    }


    @Override
    public void onDamaged(PlantInstance plant, BehaviorContext context, Zombie attacker, int damageAmount,
            boolean destroyed) {
        if (plant == null || context == null || attacker == null) {
            return;
        }
        if (mode != Mode.HYPNOTIZE || !destroyed) {
            return;
        }
        String plantKey = plant.getDefinition() == null || plant.getDefinition().getPlantKey() == null
                ? ""
                : plant.getDefinition().getPlantKey().toLowerCase();
        if (!"hypno_shroom".equals(plantKey)) {
            return;
        }
        attacker.hypnotize(999.0f);
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
