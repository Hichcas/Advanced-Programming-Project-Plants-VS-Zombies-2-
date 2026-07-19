package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.DamageType;

import java.util.List;

public class MeleeEatBehavior implements PlantBehavior {

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        double timer = asDouble(plant.getRuntimeState().getOrDefault("meleeTimer", 0.0), 0.0);
        timer += deltaTime;

        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }

        if (timer < cooldown) {
            plant.putRuntimeState("meleeTimer", timer);
            return;
        }

        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
        boolean isAreaAttack = plant.getStats().getBooleanExtra("areaMelee", false)
                || "phat_beet".equals(plant.getDefinition().getPlantKey())
                || "kiwibeast".equals(plant.getDefinition().getPlantKey());

        double tileWidth = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0), 177.0);
        double plantX = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
        double range = isAreaAttack ? tileWidth * 1.6 : tileWidth * 1.2;

        int damage = Math.max(0, plant.getStats().getDamage());
        double damageMultiplier = plant.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (plant.isPlantFoodActive()) {
            damageMultiplier = Math.max(damageMultiplier, plant.getStats().getDoubleExtra("plantFoodDamageMultiplier", 2.0));
        }
        damage = (int) Math.min(Integer.MAX_VALUE, Math.round(damage * damageMultiplier));

        boolean hitAnything = false;
        int[] lanes = isAreaAttack ? new int[]{lane - 1, lane, lane + 1} : new int[]{lane};
        for (int targetLane : lanes) {
            if (targetLane < 0) {
                continue;
            }
            List<Zombie> zombies = context.getZombiesInLane(targetLane);
            for (Zombie z : zombies) {
                if (z == null || z.isDead()) {
                    continue;
                }
                if (Math.abs(z.getX() - plantX) <= range) {
                    z.takeDamage(damage, DamageType.NORMAL);
                    hitAnything = true;
                    if (!isAreaAttack) {
                        // single-target melee plants (chomper/bonk_choy/wasabi_whip) only bite once per swing
                        break;
                    }
                }
            }
        }

        if (hitAnything && plant.isPlantFoodActive()) {
            context.consumePlantFood(plant);
        }

        timer = 0.0;
        plant.putRuntimeState("meleeTimer", timer);
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
