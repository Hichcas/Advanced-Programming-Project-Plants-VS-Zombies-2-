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

        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();

        // Release a pult projectile slightly into the attack PAM instead of exactly at the
        // cooldown boundary. This keeps the visible projectile launch synchronized with the
        // plant's throwing motion.
        double pending = asDouble(plant.getRuntimeState().getOrDefault("pendingLobShot", 0.0), 0.0);
        if (pending > 0.0) {
            pending -= deltaTime;
            if (pending <= 0.0) {
                LobShotParams params = (LobShotParams) plant.getRuntimeState().get("pendingLobParams");
                if (params != null) {
                    spawnProjectiles(plant, context, params);
                }
                plant.getRuntimeState().remove("pendingLobShot");
                plant.getRuntimeState().remove("pendingLobParams");
            } else {
                plant.putRuntimeState("pendingLobShot", pending);
            }
        }

        double timer = updateLobTimer(plant, deltaTime);
        double cooldown = getCooldown(plant);
        if (timer < cooldown || pending > 0.0) {
            return;
        }

        timer = 0.0;
        plant.putRuntimeState("lobTimer", timer);

        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            return;
        }

        LobShotParams shotParams = calculateShotParams(plant);
        double releaseDelay = switch (key) {
            case "cabbage_pult" -> 0.50;
            case "kernel_pult" -> 0.52;
            case "melon_pult" -> 0.56;
            default -> 0.40;
        };
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 1.0);
        plant.putRuntimeState("pendingLobShot", releaseDelay);
        plant.putRuntimeState("pendingLobParams", shotParams);
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
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        if ("kernel_pult".equals(key)) {
            damage = Math.max(1, plant.getStats().getDamage());
            double butterChance = 0.25;
            if (plant.getDefinition().getBaseAbility() != null) {
                butterChance = plant.getDefinition().getBaseAbility().getDoubleParam("butterChancePercent", 25.0) / 100.0;
            }
            stunShot = Math.random() < butterChance;
        } else if (tiers != null && tiers.size() > 1) {
            damage = tiers.get(0);
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
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        for (int i = 0; i < params.projectileCount; i++) {
            Projectile projectile = ProjectileFactory.createLobProjectile(plant, params.damage);
            if (params.freezeAttack) projectile.setType(ProjectileType.ICE_PEA);
            if (params.fireAttack) projectile.setType(ProjectileType.FIRE_PEA);
            if (params.stunShot) {
                projectile.putExtra("stunOnHit", Boolean.TRUE);
                projectile.putExtra("kernelButter", Boolean.TRUE);
                projectile.putExtra("butterDurationSeconds", 4.0);
            } else if ("kernel_pult".equals(key)) {
                projectile.putExtra("kernelCorn", Boolean.TRUE);
            }

            if ("cabbage_pult".equals(key)) {
                projectile.putExtra("lobArcHeight", 310.0);
                projectile.putExtra("lobArcDuration", 1.15);
                projectile.putExtra("visualKey", "CABBAGE");
            } else if ("melon_pult".equals(key)) {
                projectile.putExtra("lobArcHeight", 340.0);
                projectile.putExtra("lobArcDuration", 1.20);
                projectile.putExtra("visualKey", "MELON");
                projectile.setAreaDamage(true);
                projectile.setAreaRadiusPx(115f);
            } else if ("kernel_pult".equals(key)) {
                projectile.putExtra("lobArcHeight", 300.0);
                projectile.putExtra("lobArcDuration", 1.15);
                projectile.putExtra("visualKey", "KERNEL");
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
