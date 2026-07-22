package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantTag;

import java.util.List;

/**
 * Behavior for shooter plants that periodically fires projectiles.
 * Refactored to comply with Checkstyle (method length ≤ 50 lines).
 */
public class ShooterBehavior implements PlantBehavior {

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        Double attackTimer = asDouble(plant.getRuntimeState().getOrDefault("attackTimer", 0.0), 0.0);
        attackTimer += deltaTime;

        double cooldown = getCooldown(plant);
        if (attackTimer < cooldown) {
            plant.putRuntimeState("attackTimer", attackTimer);
            return;
        }

        // Reset timer for next attack
        attackTimer = 0.0;

        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            plant.putRuntimeState("attackTimer", attackTimer);
            return;
        }

        int damage = calculateDamage(plant);
        int projectileCount = calculateProjectileCount(plant);
        spawnProjectiles(plant, context, damage, projectileCount);

        plant.putRuntimeState("attackTimer", attackTimer);
    }

    // ---------- Helper methods ----------

    /**
     * Computes the attack cooldown for the plant, considering charge tags.
     */
    private double getCooldown(PlantInstance plant) {
        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }
        if (plant.getDefinition() != null && plant.getDefinition().hasTag(PlantTag.CHARGE)) {
            double chargeTime = plant.getStats().getChargeTimeSeconds();
            if (chargeTime <= 0) {
                chargeTime = 2.0;
            }
            cooldown = Math.max(cooldown, Math.max(1.0, chargeTime));
        }
        return cooldown;
    }

    /**
     * Calculates the final damage per projectile, factoring plant food multiplier.
     */
    private int calculateDamage(PlantInstance plant) {
        int damage = Math.max(0, plant.getStats().getDamage());
        double multiplier = plant.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (plant.isPlantFoodActive()) {
            double pfMultiplier = plant.getStats().getDoubleExtra("plantFoodDamageMultiplier", 2.0);
            multiplier = Math.max(multiplier, pfMultiplier);
        }
        return (int) Math.round(damage * multiplier);
    }

    /**
     * Calculates the number of projectiles to fire, considering plant food.
     */
    private int calculateProjectileCount(PlantInstance plant) {
        int count = Math.max(1, plant.getStats().getIntExtra("projectileCount", 1));
        if (plant.isPlantFoodActive()) {
            int pfCount = plant.getStats().getIntExtra("plantFoodProjectileCount", count);
            count = Math.max(count, pfCount);
        }
        return count;
    }

    /**
     * Creates and spawns the projectiles with appropriate attributes.
     */
    private void spawnProjectiles(PlantInstance plant, BehaviorContext context,
                                  int damage, int projectileCount) {
        for (int i = 0; i < projectileCount; i++) {
            Projectile projectile = ProjectileFactory.createProjectile(plant, damage);

            if (plant.getStats().getBooleanExtra("fireAttack", false)) {
                projectile.setType(ProjectileType.FIRE_PEA);
            }
            if (plant.getStats().getBooleanExtra("iceAttack", false)) {
                projectile.setType(ProjectileType.ICE_PEA);
            }
            if (plant.getStats().getBooleanExtra("passThrough", false)) {
                int pierceBoost = plant.getStats().getIntExtra("pierceBoost", 3);
                projectile.setPierce(Math.max(projectile.getPierce(), pierceBoost));
            }

            context.spawnProjectile(projectile);
        }
    }

    // ---------- Utility methods ----------

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
