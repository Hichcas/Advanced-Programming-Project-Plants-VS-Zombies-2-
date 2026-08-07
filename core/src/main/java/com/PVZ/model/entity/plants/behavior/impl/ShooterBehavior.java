package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantCategory;
import com.PVZ.model.enums.PlantTag;

import java.util.List;

public class ShooterBehavior implements PlantBehavior {
    private static final double BURST_GAP_SECONDS = 0.12;
    // How long the "shooting" PAM clip stays selected after a shot is fired.
    // Tune this to roughly match the real length of each plant's shooting animation.
    private static final double SHOOT_ANIM_SECONDS = 0.5;

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        drainBurst(plant, context, deltaTime);

        Double attackTimer = asDouble(plant.getRuntimeState().getOrDefault("attackTimer", 0.0), 0.0);
        attackTimer += deltaTime;

        double cooldown = getCooldown(plant);
        if (attackTimer < cooldown) {
            plant.putRuntimeState("attackTimer", attackTimer);
            return;
        }
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

    private int calculateDamage(PlantInstance plant) {
        int damage = Math.max(0, plant.getStats().getDamage());
        double multiplier = plant.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (plant.isPlantFoodActive()) {
            double pfMultiplier = plant.getStats().getDoubleExtra("plantFoodDamageMultiplier", 2.0);
            multiplier = Math.max(multiplier, pfMultiplier);
        }
        return (int) Math.round(damage * multiplier);
    }

    private int calculateProjectileCount(PlantInstance plant) {
        int count = Math.max(1, plant.getStats().getIntExtra("projectileCount", 1));
        if (plant.isPlantFoodActive()) {
            int pfCount = plant.getStats().getIntExtra("plantFoodProjectileCount", count);
            count = Math.max(count, pfCount);
        }
        return count;
    }


    private void startBurst(PlantInstance plant, int damage, int projectileCount) {
        plant.putRuntimeState("burstRemaining", Math.max(1, projectileCount));
        plant.putRuntimeState("burstDamage", damage);
        plant.putRuntimeState("burstTimer", 999.0);
    }

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


    private void spawnOne(PlantInstance plant, BehaviorContext context, int damage) {
        Projectile projectile = ProjectileFactory.createProjectile(plant, damage);

        // Mark that a "shooting" animation should play for a short window.
        // Plant.draw() reads this to pick the "shooting" PAM clip instead of "idle".
        // Duration comes from the real PAM clip length when known (see PamAnimationCatalog),
        // falling back to a generic guess otherwise.
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", SHOOT_ANIM_SECONDS);

        if (plant.getStats().getBooleanExtra("fireAttack", false)) {
            projectile.setType(ProjectileType.FIRE_PEA);
        }
        if (plant.getStats().getBooleanExtra("iceAttack", false)) {
            projectile.setType(ProjectileType.ICE_PEA);
        }

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
