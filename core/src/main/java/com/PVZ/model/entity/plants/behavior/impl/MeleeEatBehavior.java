package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.DamageType;

import java.util.List;

/**
 * Behavior for melee eating plants (Chomper, Bonk Choy, etc.).
 * Refactored to comply with Checkstyle and PMD (method length ≤ 50 lines).
 */
public class MeleeEatBehavior implements PlantBehavior {

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        // Handle cooldown
        if (!isCooldownReady(plant, deltaTime)) {
            return;
        }

        // Perform attack
        performMeleeAttack(plant, context);
    }

    /**
     * Checks if the melee cooldown has elapsed and updates the timer.
     * Returns true if ready to attack.
     */
    private boolean isCooldownReady(PlantInstance plant, double deltaTime) {
        double timer = asDouble(plant.getRuntimeState().getOrDefault("meleeTimer", 0.0), 0.0);
        timer += deltaTime;

        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }

        if (timer < cooldown) {
            plant.putRuntimeState("meleeTimer", timer);
            return false;
        }

        // Reset timer for next attack
        plant.putRuntimeState("meleeTimer", 0.0);
        return true;
    }

    /**
     * Performs the melee attack: determines area or single target,
     * calculates damage, applies to zombies in range.
     */
    private void performMeleeAttack(PlantInstance plant, BehaviorContext context) {
        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
        boolean isAreaAttack = isAreaMelee(plant);

        double tileWidth = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0), 177.0);
        double plantX = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
        double range = isAreaAttack ? tileWidth * 1.6 : tileWidth * 1.2;

        int damage = computeMeleeDamage(plant);
        boolean hitAnything = applyDamageToZombies(plant, context, lane, isAreaAttack, plantX, range, damage);

        if (hitAnything && plant.isPlantFoodActive()) {
            context.consumePlantFood(plant);
        }
    }

    /**
     * Determines if this plant has area melee attack.
     */
    private boolean isAreaMelee(PlantInstance plant) {
        return plant.getStats().getBooleanExtra("areaMelee", false)
            || "phat_beet".equals(plant.getDefinition().getPlantKey())
            || "kiwibeast".equals(plant.getDefinition().getPlantKey());
    }

    /**
     * Computes the final damage including multipliers and plant food bonus.
     */
    private int computeMeleeDamage(PlantInstance plant) {
        int damage = Math.max(0, plant.getStats().getDamage());
        double damageMultiplier = plant.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (plant.isPlantFoodActive()) {
            damageMultiplier = Math.max(damageMultiplier,
                plant.getStats().getDoubleExtra("plantFoodDamageMultiplier", 2.0));
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.round(damage * damageMultiplier));
    }

    /**
     * Applies damage to zombies in the affected lanes.
     * Returns true if at least one zombie was hit.
     */
    private boolean applyDamageToZombies(PlantInstance plant, BehaviorContext context,
                                         int lane, boolean isAreaAttack,
                                         double plantX, double range, int damage) {
        int[] lanes = isAreaAttack ? new int[]{lane - 1, lane, lane + 1} : new int[]{lane};
        boolean hitAnything = false;

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
                        // single-target plants only hit once per swing
                        break;
                    }
                }
            }
        }
        return hitAnything;
    }

    // ------------------------------------------------------------------------
    // Utility methods (unchanged)
    // ------------------------------------------------------------------------

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

