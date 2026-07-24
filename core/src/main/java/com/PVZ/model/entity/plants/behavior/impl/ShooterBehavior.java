package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantCategory;
import com.PVZ.model.enums.PlantTag;

import java.util.List;

/**
 * Behavior for shooter plants that periodically fires projectiles.
 * Refactored to comply with Checkstyle (method length ≤ 50 lines).
 */
public class ShooterBehavior implements PlantBehavior {

    /** Seconds between consecutive peas of a burst (Repeater fires 2, Mega Gatling Pea fires 4). */
    private static final double BURST_GAP_SECONDS = 0.12;

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        // Drain any in-progress burst first, so consecutive peas keep firing between cooldowns.
        drainBurst(plant, context, deltaTime);

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
        startBurst(plant, damage, projectileCount);

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
     * Begins a burst of {@code projectileCount} consecutive shots. Spawning them all in the
     * same tick made Repeater's 2 peas and Mega Gatling Pea's 4 peas overlap into what looked
     * like a single pea; instead the burst is drained one pea at a time by {@link #drainBurst}.
     */
    private void startBurst(PlantInstance plant, int damage, int projectileCount) {
        plant.putRuntimeState("burstRemaining", Math.max(1, projectileCount));
        plant.putRuntimeState("burstDamage", damage);
        // large value so the very first pea fires on the next drainBurst() call
        plant.putRuntimeState("burstTimer", 999.0);
    }

    /**
     * Fires the next pea of an in-progress burst once the inter-shot gap has elapsed.
     */
    private void drainBurst(PlantInstance plant, BehaviorContext context, double deltaTime) {
        int remaining = asInt(plant.getRuntimeState().getOrDefault("burstRemaining", 0), 0);
        if (remaining <= 0) {
            return;
        }
        double timer = asDouble(plant.getRuntimeState().getOrDefault("burstTimer", 0.0), 0.0);
        timer += deltaTime;
        if (timer < BURST_GAP_SECONDS) {
            plant.putRuntimeState("burstTimer", timer);
            return;
        }
        int damage = asInt(plant.getRuntimeState().getOrDefault("burstDamage", 0), 0);
        spawnOne(plant, context, damage);
        plant.putRuntimeState("burstRemaining", remaining - 1);
        plant.putRuntimeState("burstTimer", 0.0);
    }

    /**
     * Creates and spawns a single projectile with the appropriate type and pierce attributes.
     */
    private void spawnOne(PlantInstance plant, BehaviorContext context, int damage) {
        Projectile projectile = ProjectileFactory.createProjectile(plant, damage);

        if (plant.getStats().getBooleanExtra("fireAttack", false)) {
            projectile.setType(ProjectileType.FIRE_PEA);
        }
        if (plant.getStats().getBooleanExtra("iceAttack", false)) {
            projectile.setType(ProjectileType.ICE_PEA);
        }
        // Fume-shroom breathes a smoke cloud (drawn in code) that passes through zombies,
        // not a solid pea bullet.
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        if ("fume_shroom".equals(key)) {
            projectile.setType(ProjectileType.FUME);
        }
        boolean shouldPierce = plant.getStats().getBooleanExtra("passThrough", false)
                || (plant.getDefinition() != null
                    && plant.getDefinition().getCategoryEnum() == PlantCategory.THROUGH_STRIKE);
        if (shouldPierce) {
            int pierceBoost = plant.getStats().getIntExtra("pierceBoost", 3);
            projectile.setPierce(Math.max(projectile.getPierce(), pierceBoost));
        }

        context.spawnProjectile(projectile);
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
