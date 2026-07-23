package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;
import java.util.Set;

/**
 * Behavior for explosive plants that detonate on contact or after arming.
 * Refactored to comply with Checkstyle (method length ≤ 50 lines).
 */
public class ExplosiveBehavior implements PlantBehavior {

    private static final Set<String> CONTACT_TRIGGERED = Set.of(
        "potato_mine", "primal_potato_mine", "tangle_kelp", "iceberg_lettuce", "squash");

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (!isArmed(plant, deltaTime)) {
            return;
        }

        Integer lane = (Integer) plant.getRuntimeState().getOrDefault("lane", 0);
        Integer row = (Integer) plant.getRuntimeState().getOrDefault("row", 0);
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();

        if (requiresContact(key) && context.getZombiesInLane(lane).isEmpty()) {
            return;
        }

        int damage = calculateDamage(plant);
        handleExplosion(plant, context, lane, row, key, damage);
    }

    // ---------- Arming logic ----------
    private boolean isArmed(PlantInstance plant, double deltaTime) {
        Boolean armed = (Boolean) plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE);
        if (armed != null && armed) {
            return true;
        }

        Double armTimer = (Double) plant.getRuntimeState().getOrDefault("armTimer", 0.0);
        armTimer += deltaTime;

        double armTime = plant.getStats().getArmTimeSeconds();
        if (armTimer >= Math.max(armTime, 0.0)) {
            plant.putRuntimeState("armed", Boolean.TRUE);
            armTimer = 0.0;
        }

        plant.putRuntimeState("armTimer", armTimer);
        return Boolean.TRUE.equals(plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE));
    }

    private boolean requiresContact(String key) {
        return key != null && CONTACT_TRIGGERED.contains(key);
    }

    private int calculateDamage(PlantInstance plant) {
        int explosionDamage = plant.getStats().getExplodeDamage();
        if (explosionDamage > 0) {
            return explosionDamage;
        }
        return Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage());
    }

    // ---------- Explosion type handlers ----------
    private void handleExplosion(PlantInstance plant, BehaviorContext context,
                                 int lane, int row, String key, int damage) {
        if ("ice_shroom".equals(key)) {
            handleIceShroom(plant, context);
        } else if ("jalapeno".equals(key)) {
            handleJalapeno(plant, context, lane, damage);
        } else if ("squash".equals(key)) {
            handleSquash(plant, context, lane, damage);
        } else if ("grapshot".equals(key) || "grapeshot".equals(key)) {
            handleGrapeshot(plant, context, lane, row, damage);
        } else {
            handleDefaultExplosion(plant, context, lane, row, damage);
        }
    }

    private void handleIceShroom(PlantInstance plant, BehaviorContext context) {
        double freezeTime = Math.max(3.0, plant.getStats().getFreezeTimeSeconds());
        context.freezeAllZombies(freezeTime);
        plant.takeDamage(plant.getCurrentHp());
    }

    private void handleJalapeno(PlantInstance plant, BehaviorContext context, int lane, int damage) {
        context.damageLane(lane, damage);
        if (plant.getStats().getBooleanExtra("meltsIce", false)) {
            context.meltIceInLane(lane);
        }
        plant.takeDamage(plant.getCurrentHp());
    }

    private void handleSquash(PlantInstance plant, BehaviorContext context, int lane, int damage) {
        List<Zombie> targets = context.getZombiesInLane(lane);
        if (!targets.isEmpty()) {
            Zombie nearest = targets.get(0);
            context.damageSingleTarget(nearest, damage);
        }
        plant.takeDamage(plant.getCurrentHp());
    }

    private void handleGrapeshot(PlantInstance plant, BehaviorContext context,
                                 int lane, int row, int damage) {
        context.damageArea(lane, row, damage);
        int grapeCount = plant.getStats().getIntExtra("grapeCount", 8);
        double grapeLifespan = plant.getStats().getDoubleExtra("grapeLifespanSeconds", 5.0);
        context.spawnBouncingProjectiles(lane, row, grapeCount, damage / 4, grapeLifespan);
        plant.takeDamage(plant.getCurrentHp());
    }

    private void handleDefaultExplosion(PlantInstance plant, BehaviorContext context,
                                        int lane, int row, int damage) {
        context.damageArea(lane, row, damage);
        plant.takeDamage(plant.getCurrentHp());
    }
}
