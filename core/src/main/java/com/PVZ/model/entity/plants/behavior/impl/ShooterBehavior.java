package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantTag;

import java.util.List;

public class ShooterBehavior implements PlantBehavior {

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        Double attackTimer = asDouble(plant.getRuntimeState().getOrDefault("attackTimer", 0.0), 0.0);
        attackTimer += deltaTime;

        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }
        if (plant.getDefinition() != null && plant.getDefinition().hasTag(PlantTag.CHARGE)) {
            cooldown = Math.max(cooldown, Math.max(1.0, plant.getStats().getChargeTimeSeconds() > 0
                    ? plant.getStats().getChargeTimeSeconds()
                    : 2.0));
        }

        if (attackTimer < cooldown) {
            plant.putRuntimeState("attackTimer", attackTimer);
            return;
        }

        attackTimer = 0.0;
        Integer lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            plant.putRuntimeState("attackTimer", attackTimer);
            return;
        }

        int damage = Math.max(0, plant.getStats().getDamage());
        double damageMultiplier = plant.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (plant.isPlantFoodActive()) {
            damageMultiplier = Math.max(damageMultiplier, plant.getStats().getDoubleExtra("plantFoodDamageMultiplier", 2.0));
        }
        damage = (int) Math.round(damage * damageMultiplier);

        int projectileCount = Math.max(1, plant.getStats().getIntExtra("projectileCount", 1));
        if (plant.isPlantFoodActive()) {
            projectileCount = Math.max(projectileCount, plant.getStats().getIntExtra("plantFoodProjectileCount", projectileCount));
        }

        for (int i = 0; i < projectileCount; i++) {
            Projectile projectile = ProjectileFactory.createProjectile(plant, damage);
            if (plant.getStats().getBooleanExtra("fireAttack", false)) {
                projectile.setType(ProjectileType.FIRE_PEA);
            }
            if (plant.getStats().getBooleanExtra("iceAttack", false)) {
                projectile.setType(ProjectileType.ICE_PEA);
            }
            if (plant.getStats().getBooleanExtra("passThrough", false)) {
                projectile.setPierce(Math.max(projectile.getPierce(), plant.getStats().getIntExtra("pierceBoost", 3)));
            }
            context.spawnProjectile(projectile);
        }

        plant.putRuntimeState("attackTimer", attackTimer);
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
