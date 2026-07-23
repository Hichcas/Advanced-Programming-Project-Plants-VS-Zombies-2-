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

    // --- Squash: leap onto adjacent zombie then crush ---
    if("squash".equals(key))

    {
        // State machine: LEAPING phase (set by handler or first tick after zombie
        // adjacent)
        String squashState = (String) plant.getRuntimeState().getOrDefault("squashState", "idle");
        if ("idle".equals(squashState)) {
            // Wait until a zombie is on or adjacent to the squash's tile column
            int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            List<Zombie> candidates = new java.util.ArrayList<>(context.getZombiesInLane(lane));
            candidates.removeIf(z -> z == null || z.isDead());
            // Only trigger on a zombie within ~1 column of the squash
            candidates.removeIf(z -> {
                int zCol = mapColOf(context, z);
                return java.lang.Math.abs(zCol - col) > 1;
            });
            if (candidates.isEmpty()) {
                return; // no adjacent zombie — stay alive and wait
            }
            // Begin the leap: mark state so we animate a short hold before crush
            plant.putRuntimeState("squashState", "leaping");
            plant.putRuntimeState("squashTimer", 0.0);
            // The visual leap is handled by checking squashState in DrawHandler
            // (renders the plant at an offset Y when leaping).
            return;
        }
        // Leaping phase — after a short delay the squash lands on the chosen zombie
        double jumpTimer = asDouble(plant.getRuntimeState().getOrDefault("squashTimer", 0.0), 0.0);
        jumpTimer += deltaTime;
        plant.putRuntimeState("squashTimer", jumpTimer);
        if (jumpTimer < 0.4) {
            return; // still in the air
        }
        // Land and crush
        List<Zombie> targets = context.getZombiesInLane(lane);
        if (!targets.isEmpty()) {
            Zombie nearest = targets.get(0);
            context.damageSingleTarget(nearest, damage);
        }

        boolean canCrushTwice = plant.getStats().getBooleanExtra("canCrush2x", false);
        int crushesDone = asInt(plant.getRuntimeState().getOrDefault("squashCrushes", 0), 0) + 1;
        if (canCrushTwice && crushesDone < 2) {
            // Level-4 Squash ("Can crush 2x"): survive the first crush, reset to idle
            // and wait for the next adjacent zombie instead of dying immediately.
            plant.putRuntimeState("squashCrushes", crushesDone);
            plant.putRuntimeState("squashState", "idle");
            plant.putRuntimeState("squashTimer", 0.0);
            return;
        }
        plant.takeDamage(plant.getCurrentHp());
        return;
    }plant.takeDamage(plant.getCurrentHp());
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

    private static int mapColOf(BehaviorContext context, com.PVZ.model.entity.zombies.base.Zombie z) {
        if (context instanceof com.PVZ.model.game.BattleController bc) {
            return bc.getTileColumn((float) z.getX());
        }
        if (context instanceof com.PVZ.model.game.RegularGameEngine rge) {
            return rge.getTileColumn((float) z.getX());
        }
        return 0;
    }
}
