package com.PVZ.model.entity.plants.behavior;

import com.PVZ.model.entity.plants.AbilitySpec;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantStats;
import com.PVZ.model.entity.plants.behavior.impl.DefaultPlantFoodBehavior;
import com.PVZ.model.entity.plants.behavior.impl.EmptyBehavior;
import com.PVZ.model.entity.plants.behavior.impl.ExplosiveBehavior;
import com.PVZ.model.entity.plants.behavior.impl.LobberBehavior;
import com.PVZ.model.entity.plants.behavior.impl.ManualPlantBehavior;
import com.PVZ.model.entity.plants.behavior.impl.ManualPlantFoodBehavior;
import com.PVZ.model.entity.plants.behavior.impl.MintBehavior;
import com.PVZ.model.entity.plants.behavior.impl.ModifierBehavior;
import com.PVZ.model.entity.plants.behavior.impl.ShooterBehavior;
import com.PVZ.model.entity.plants.behavior.impl.SunProducerBehavior;
import com.PVZ.model.entity.plants.behavior.impl.WallBehavior;

import java.util.Locale;

public final class BehaviorFactory {
    private BehaviorFactory() {
    }

    public static PlantBehavior create(PlantDefinition definition) {
        return createMainBehavior(definition);
    }

    public static PlantBehavior createMainBehavior(PlantDefinition definition) {
        if (definition == null) {
            return new EmptyBehavior();
        }

        String behaviorId = resolveBehaviorId(definition.getBaseAbility());
        if (behaviorId == null || behaviorId.isBlank()) {
            return fallbackByCategory(definition);
        }

        return switch (normalize(behaviorId)) {
            case "sun_producer", "sunproducer", "produce_sun", "sun", "sun_burst", "sun_production", "growing_sun" -> new SunProducerBehavior();
            case "instant_sun" -> new ManualPlantBehavior(definition, definition.getBaseAbility());
            case "shooter", "pea_shooter", "peashooter", "direct_shot", "burst_shot", "fire_shot", "ice_shot", "poison_shot", "piercing_shot", "homing_shot", "target_lock" -> new ShooterBehavior();
            case "lobber", "lobber_kernel", "lob", "bounce_shot", "pult" -> new LobberBehavior();
            case "explosive", "bomb", "mine", "aoe", "burst_explode", "lane_clear" -> new ExplosiveBehavior();
            case "wall", "wall_nut", "defense", "wall_defense" -> new WallBehavior();
            case "mint", "mint_family_buff", "family_buff" -> new MintBehavior();
            case "modifier", "utility", "water_support", "move_zombies", "magnet_disarm", "magnet_pulse", "hypnotize", "copy_plant" -> new ManualPlantBehavior(definition, definition.getBaseAbility());
            case "melee_eat", "melee", "attack_melee" -> new ManualPlantBehavior(definition, definition.getBaseAbility());
            default -> fallbackByCategory(definition);
        };
    }

    public static PlantFoodBehavior createPlantFoodBehavior(PlantDefinition definition) {
        if (definition == null || definition.getPlantFoodEffect() == null) {
            return new DefaultPlantFoodBehavior();
        }

        AbilitySpec plantFood = definition.getPlantFoodEffect();
        String behaviorId = resolveBehaviorId(plantFood);
        if (behaviorId == null || behaviorId.isBlank()) {
            return new DefaultPlantFoodBehavior();
        }

        String normalized = normalize(behaviorId);
        if ("custom".equals(normalized)) {
            return new ManualPlantFoodBehavior(definition, plantFood);
        }

        if ("none".equals(normalized)) {
            return (plant, context) -> {
            };
        }

        if ("burst_sun".equals(normalized) || "instant_sun".equals(normalized) || "sun_burst".equals(normalized)) {
            return (plant, context) -> {
                int amount = plantFood.getIntParam("instantSunAmount", plantFood.getIntParam("sunAmount", 150));
                if (amount <= 0) {
                    amount = 150;
                }
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(Math.max(1, plantFood.getIntParam("durationSeconds", 5)));
                int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
                int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
                context.spawnSunAt(row, col, amount);
            };
        }

        if ("burst_shot".equals(normalized) || "multi_shot".equals(normalized) || "double_projectile".equals(normalized)) {
            return (plant, context) -> {
                int duration = plantFood.getIntParam("durationSeconds", 5);
                int projectiles = plantFood.getIntParam("projectiles", 5);
                double damageMultiplier = plantFood.getDoubleParam("damageMultiplier", 2.0);
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(Math.max(1, duration));
                plant.getStats().putExtra("projectileCount", projectiles);
                plant.getStats().putExtra("plantFoodProjectileCount", projectiles);
                plant.getStats().putExtra("damageMultiplier", damageMultiplier);
                plant.getStats().putExtra("plantFoodDamageMultiplier", damageMultiplier);
            };
        }

        if ("freeze_burst".equals(normalized)) {
            return (plant, context) -> {
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(3);
                context.freezeAllZombies(3.0);
                plant.getStats().putExtra("iceAttack", Boolean.TRUE);
            };
        }

        if ("plasma_burst".equals(normalized)) {
            return (plant, context) -> {
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(1);
                int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
                int lane = asInt(plant.getRuntimeState().getOrDefault("lane", row), row);
                context.damageArea(lane, row, Math.max(1000, plant.getStats().getDamage() * 20));
            };
        }

        if ("hypnotize".equals(normalized)) {
            return (plant, context) -> {
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(4);
                int lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
                context.hypnotizeZombiesInLane(lane, 5.0);
            };
        }

        if ("fire_burst".equals(normalized)) {
            return (plant, context) -> {
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(5);
                plant.getStats().putExtra("fireAttack", Boolean.TRUE);
                plant.getStats().putExtra("damageMultiplier", 2.0);
                plant.getStats().putExtra("plantFoodDamageMultiplier", 2.0);
                plant.getStats().putExtra("projectileCount", 5);
            };
        }

        if ("lane_clear".equals(normalized) || "explosion".equals(normalized)) {
            return (plant, context) -> {
                int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
                int lane = asInt(plant.getRuntimeState().getOrDefault("lane", row), row);
                context.damageArea(lane, row, Math.max(500, plant.getStats().getDamage()));
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(1);
            };
        }

        if ("water_clone".equals(normalized)) {
            return (plant, context) -> {
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(3);
                plant.putRuntimeState("waterCloneReady", Boolean.TRUE);
            };
        }

        if ("magnet_pulse".equals(normalized)) {
            return (plant, context) -> {
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(3);
                int lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
                context.disarmZombiesInLane(lane);
            };
        }

        return new DefaultPlantFoodBehavior();
    }

    private static PlantBehavior fallbackByCategory(PlantDefinition definition) {
        switch (definition.getCategoryEnum()) {
            case SUN_PRODUCER:
                return new SunProducerBehavior();
            case SHOOTER:
            case THROUGH_STRIKE:
            case HOMING:
                return new ShooterBehavior();
            case LOBBER:
                return new LobberBehavior();
            case EXPLOSIVE:
                return new ExplosiveBehavior();
            case WALL:
                return new WallBehavior();
            case MINT:
                return new MintBehavior();
            case MODIFIER:
            case MELEE:
                return new ManualPlantBehavior(definition, definition.getBaseAbility());
            default:
                return new EmptyBehavior();
        }
    }

    private static String resolveBehaviorId(AbilitySpec abilitySpec) {
        if (abilitySpec == null) {
            return null;
        }
        if (abilitySpec.getBehaviorId() != null && !abilitySpec.getBehaviorId().isBlank()) {
            return abilitySpec.getBehaviorId();
        }
        if (abilitySpec.getId() != null && !abilitySpec.getId().isBlank()) {
            return abilitySpec.getId();
        }
        return abilitySpec.getStringParam("behaviorId", null);
    }

    private static String normalize(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
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
}
