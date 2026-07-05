package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.AbilitySpec;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantFoodBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;
import java.util.Locale;

public class ManualPlantFoodBehavior implements PlantFoodBehavior {
    private final PlantDefinition definition;
    private final AbilitySpec abilitySpec;

    public ManualPlantFoodBehavior(PlantDefinition definition, AbilitySpec abilitySpec) {
        this.definition = definition;
        this.abilitySpec = abilitySpec;
    }

    @Override
    public void onPlantFood(PlantInstance plant, BehaviorContext context) {
        if (plant == null || context == null) {
            return;
        }

        String behaviorId = normalize(resolveBehaviorId());
        int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", row), row);

        plant.setPlantFoodActive(true);
        plant.setPlantFoodTicksRemaining(5);

        switch (behaviorId) {
            case "instant_sun" -> {
                int amount = abilitySpec == null ? 0 : abilitySpec.getIntParam("sunAmount", 150);
                if (amount <= 0) {
                    amount = 150;
                }
                context.spawnSunAt(row, col, amount);
            }
            case "burst_attack", "burst_shot", "multi_shot", "double_projectile" -> {
                plant.getStats().putExtra("plantFoodProjectileCount", abilitySpec == null ? 5 : abilitySpec.getIntParam("projectiles", 5));
                plant.getStats().putExtra("plantFoodDamageMultiplier", abilitySpec == null ? 2.0 : abilitySpec.getDoubleParam("damageMultiplier", 2.0));
                plant.getStats().putExtra("burstAttack", Boolean.TRUE);
            }
            case "freeze_burst" -> {
                context.freezeAllZombies(3.0);
                plant.getStats().putExtra("iceAttack", Boolean.TRUE);
            }
            case "plasma_burst" -> {
                context.damageArea(lane, row, Math.max(plant.getStats().getDamage() * 20, 1000));
            }
            case "hypnotize" -> {
                context.hypnotizeZombiesInLane(lane, 5.0);
            }
            case "fire_burst" -> {
                plant.getStats().putExtra("fireAttack", Boolean.TRUE);
                plant.getStats().putExtra("plantFoodDamageMultiplier", 2.0);
                plant.getStats().putExtra("plantFoodProjectileCount", 5);
            }
            case "lane_clear" -> {
                context.damageArea(lane, row, Math.max(plant.getStats().getDamage(), 500));
            }
            case "water_clone" -> {
                plant.putRuntimeState("waterCloneReady", Boolean.TRUE);
            }
            case "magnet_pulse" -> {
                context.disarmZombiesInLane(lane);
            }
            case "custom" -> handleCustom(plant, context, lane, row, col);
            case "none" -> {
            }
            default -> handleGenericCustom(plant, context, lane, row, col, behaviorId);
        }
    }

    private void handleCustom(PlantInstance plant, BehaviorContext context, int lane, int row, int col) {
        String plantKey = definition == null ? "" : normalize(definition.getName());
        switch (plantKey) {
            case "electric_blueberry" -> context.killRandomZombies(3);
            case "cactus" -> {
                plant.getStats().putExtra("pierceBoost", 999);
                plant.getStats().putExtra("plantFoodDamageMultiplier", 20.0);
                plant.getStats().putExtra("plantFoodProjectileCount", 1);
                plant.getStats().putExtra("passThrough", Boolean.TRUE);
            }
            case "fume_shroom" -> {
                plant.getStats().putExtra("pierceBoost", 99);
                plant.getStats().putExtra("plantFoodDamageMultiplier", 4.0);
                plant.getStats().putExtra("passThrough", Boolean.TRUE);
            }
            case "cabbage_pult", "melon_pult", "winter_melon", "pepper_pult" -> {
                plant.getStats().putExtra("plantFoodProjectileCount", 3);
                plant.getStats().putExtra("plantFoodDamageMultiplier", 3.0);
                if (plantKey.contains("winter")) {
                    plant.getStats().putExtra("iceAttack", Boolean.TRUE);
                }
                if (plantKey.contains("pepper")) {
                    plant.getStats().putExtra("fireAttack", Boolean.TRUE);
                }
            }
            case "iceberg_lettuce" -> context.freezeAllZombies(5.0);
            case "phat_beet" -> context.damageArea(lane, row, Math.max(plant.getStats().getAoeDamage(), plant.getStats().getDamage() * 4));
            case "chomper" -> context.killClosestZombieInLane(lane);
            case "wasabi_whip" -> context.damageArea(lane, row, Math.max(plant.getStats().getDamage() * 3, 120));
            case "wall_nut", "tall_nut", "endurian", "pumpkin" -> {
                plant.heal(Math.max(plant.getStats().getMaxHp() / 2, 500));
                plant.getStats().putExtra("fortified", Boolean.TRUE);
            }
            case "sweet_potato" -> context.pullAdjacentZombiesToLane(lane);
            case "explode_o_nut" -> {
                plant.getStats().putExtra("explodeOnDeath", Boolean.TRUE);
                plant.heal(Math.max(plant.getStats().getMaxHp() / 2, 400));
            }
            case "sun_bean" -> plant.getStats().putExtra("sunDropAmount", 5);
            case "hypno_shroom" -> context.hypnotizeZombiesInLane(lane, 6.0);
            default -> handleGenericCustom(plant, context, lane, row, col, plantKey);
        }
    }

    private void handleGenericCustom(PlantInstance plant, BehaviorContext context, int lane, int row, int col, String behaviorId) {
        if (behaviorId.contains("burst")) {
            plant.getStats().putExtra("plantFoodProjectileCount", 5);
            plant.getStats().putExtra("plantFoodDamageMultiplier", 2.0);
        }
        if (behaviorId.contains("freeze")) {
            context.freezeZombiesInLane(lane, 3.0);
        }
        if (behaviorId.contains("fire")) {
            plant.getStats().putExtra("fireAttack", Boolean.TRUE);
        }
        if (behaviorId.contains("ice")) {
            plant.getStats().putExtra("iceAttack", Boolean.TRUE);
        }
    }

    private String resolveBehaviorId() {
        if (abilitySpec != null) {
            if (abilitySpec.getBehaviorId() != null && !abilitySpec.getBehaviorId().isBlank()) {
                return abilitySpec.getBehaviorId();
            }
            if (abilitySpec.getId() != null && !abilitySpec.getId().isBlank()) {
                return abilitySpec.getId();
            }
        }
        if (definition != null && definition.getPlantFoodEffect() != null) {
            return definition.getPlantFoodEffect().getResolvedBehaviorId();
        }
        return "";
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
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
