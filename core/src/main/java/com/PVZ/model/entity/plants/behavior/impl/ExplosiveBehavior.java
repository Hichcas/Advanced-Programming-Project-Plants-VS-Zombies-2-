package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;
import java.util.Set;


public class ExplosiveBehavior implements PlantBehavior {

    private static final Set<String> CONTACT_TRIGGERED = Set.of(
        "potato_mine", "primal_potato_mine", "tangle_kelp", "iceberg_lettuce", "squash");

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        Boolean armed = (Boolean) plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE);
        if (armed == null || !armed) {
            Double armTimer = (Double) plant.getRuntimeState().getOrDefault("armTimer", 0.0);
            armTimer += deltaTime;
            double armTime = plant.getStats().getArmTimeSeconds();
            if (armTimer >= Math.max(armTime, 0.0)) {
                plant.putRuntimeState("armed", Boolean.TRUE);
                armTimer = 0.0;
            }
            plant.putRuntimeState("armTimer", armTimer);
            if (!Boolean.TRUE.equals(plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE)))
                return;
        }
        Integer lane = (Integer) plant.getRuntimeState().getOrDefault("lane", 0);
        Integer row = (Integer) plant.getRuntimeState().getOrDefault("row", 0);
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        boolean requiresContact = key != null && CONTACT_TRIGGERED.contains(key);
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (requiresContact && zombies.isEmpty()) {
            return;
        }
        if ("ice_shroom".equals(key)) {
            context.freezeAllZombies(Math.max(3.0, plant.getStats().getFreezeTimeSeconds()));
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        int damage = plant.getStats().getExplodeDamage() > 0 ? plant.getStats().getExplodeDamage()
            : Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage());
        if ("jalapeno".equals(key)) {
            context.damageLane(lane, damage);
            if (plant.getStats().getBooleanExtra("meltsIce", false)) {
                context.meltIceInLane(lane);
            }
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        if ("squash".equals(key)) {
            String squashState = (String) plant.getRuntimeState().getOrDefault("squashState",
                "idle");
            if ("idle".equals(squashState)) {
                int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
                List<Zombie> candidates = new java.util.ArrayList<>(context.getZombiesInLane(lane));
                candidates.removeIf(z -> z == null || z.isDead());
                candidates.removeIf(z -> {
                    int zCol = mapColOf(context, z);
                    return java.lang.Math.abs(zCol - col) > 1;
                });
                if (candidates.isEmpty())
                    return;
                plant.putRuntimeState("squashState", "leaping");
                plant.putRuntimeState("squashTimer", 0.0);
                return;
            }
            double jumpTimer = asDouble(plant.getRuntimeState().getOrDefault("squashTimer",
                0.0), 0.0);
            jumpTimer += deltaTime;
            plant.putRuntimeState("squashTimer", jumpTimer);
            if (jumpTimer < 0.4)
                return;
            List<Zombie> targets = context.getZombiesInLane(lane);
            if (!targets.isEmpty()) {
                Zombie nearest = targets.get(0);
                context.damageSingleTarget(nearest, damage);
            }
            boolean canCrushTwice = plant.getStats().getBooleanExtra("canCrush2x", false);
            int crushesDone = asInt(plant.getRuntimeState().getOrDefault("squashCrushes", 0), 0) + 1;
            if (canCrushTwice && crushesDone < 2) {
                plant.putRuntimeState("squashCrushes", crushesDone);
                plant.putRuntimeState("squashState", "idle");
                plant.putRuntimeState("squashTimer", 0.0);
                return;
            }
            plant.takeDamage(plant.getCurrentHp());
            return;
        }

        if ("iceberg_lettuce".equals(key)) {
            context.freezeZombiesInLane(lane, Math.max(3.0, plant.getStats().getFreezeTimeSeconds()));
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        if ("tangle_kelp".equals(key)) {
            context.killClosestZombieInLane(lane);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        if ("grapshot".equals(key) || "grapeshot".equals(key)) {
            context.damageArea(lane, row, damage);
            int grapeCount = plant.getStats().getIntExtra("grapeCount", 8);
            double grapeLifespan = plant.getStats().getDoubleExtra("grapeLifespanSeconds", 5.0);
            context.spawnBouncingProjectiles(lane, row, grapeCount, damage / 4, grapeLifespan);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
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
