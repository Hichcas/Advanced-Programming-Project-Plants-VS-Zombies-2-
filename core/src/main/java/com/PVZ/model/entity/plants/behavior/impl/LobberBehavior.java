package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;

public class LobberBehavior implements PlantBehavior {
    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        double timer = updateLobTimer(plant, deltaTime);
        double cooldown = getCooldown(plant);
        if (timer < cooldown) {
            return;
        }

        timer = 0.0;
        plant.putRuntimeState("lobTimer", timer);

        Integer lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            return;
        }

        LobShotParams shotParams = calculateShotParams(plant);
        spawnProjectiles(plant, context, shotParams);
    }

    private double updateLobTimer(PlantInstance plant, double deltaTime) {
        Double current = asDouble(plant.getRuntimeState().getOrDefault("lobTimer", 0.0), 0.0);
        double newTimer = current + deltaTime;
        plant.putRuntimeState("lobTimer", newTimer);
        return newTimer;
    }

    private double getCooldown(PlantInstance plant) {
        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 2.0;
        }
        return cooldown;
    }

    private LobShotParams calculateShotParams(PlantInstance plant) {
        List<Integer> tiers = plant.getDefinition() != null
            && plant.getDefinition().getDamageSpec() != null
            ? plant.getDefinition().getDamageSpec().getTiers()
            : null;

        int damage;
        boolean stunShot = false;
        if (tiers != null && tiers.size() > 1) {
            int tierIndex = asInt(plant.getRuntimeState().getOrDefault("lobTierIndex", 0), 0);
            damage = tiers.get(tierIndex % tiers.size());
            stunShot = (tierIndex % tiers.size()) == tiers.size() - 1;
            plant.putRuntimeState("lobTierIndex", tierIndex + 1);
        } else {
            damage = Math.max(0, plant.getStats().getDamage());
        }

        double damageMultiplier = plant.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (plant.isPlantFoodActive()) {
            damageMultiplier = Math.max(damageMultiplier,
                plant.getStats().getDoubleExtra("plantFoodDamageMultiplier", 2.0));
        }
        damage = (int) Math.round(damage * damageMultiplier);

        int projectileCount = Math.max(1, plant.getStats().getIntExtra("projectileCount", 1));
        boolean freezeAttack = plant.getStats().getBooleanExtra("freezeAttack", false);
        boolean fireAttack = plant.getStats().getBooleanExtra("fireAttack", false);

        return new LobShotParams(damage, projectileCount, stunShot, freezeAttack, fireAttack);
    }

    private void spawnProjectiles(PlantInstance plant, BehaviorContext context, LobShotParams params) {
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", 0.5);
        for (int i = 0; i < params.projectileCount; i++) {
            Projectile projectile = ProjectileFactory.createLobProjectile(plant, params.damage);
            if (params.freezeAttack) {
                projectile.setType(ProjectileType.ICE_PEA);
            }
            if (params.fireAttack) {
                projectile.setType(ProjectileType.FIRE_PEA);
            }
            if (params.stunShot) {
                projectile.putExtra("stunOnHit", Boolean.TRUE);
            }
            context.spawnProjectile(projectile);
        }
    }

    private static class LobShotParams {
        final int damage;
        final int projectileCount;
        final boolean stunShot;
        final boolean freezeAttack;
        final boolean fireAttack;

        LobShotParams(int damage, int projectileCount, boolean stunShot,
                      boolean freezeAttack, boolean fireAttack) {
            this.damage = damage;
            this.projectileCount = projectileCount;
            this.stunShot = stunShot;
            this.freezeAttack = freezeAttack;
            this.fireAttack = fireAttack;
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
