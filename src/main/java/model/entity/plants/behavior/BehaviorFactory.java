package model.entity.plants.behavior;

import model.entity.plants.AbilitySpec;
import model.entity.plants.PlantDefinition;
import model.entity.plants.PlantStats;
import model.entity.plants.behavior.impl.DefaultPlantFoodBehavior;
import model.entity.plants.behavior.impl.EmptyBehavior;
import model.entity.plants.behavior.impl.ExplosiveBehavior;
import model.entity.plants.behavior.impl.LobberBehavior;
import model.entity.plants.behavior.impl.MintBehavior;
import model.entity.plants.behavior.impl.ModifierBehavior;
import model.entity.plants.behavior.impl.ProjectileFactory;
import model.entity.plants.behavior.impl.ShooterBehavior;
import model.entity.plants.behavior.impl.SunProducerBehavior;
import model.entity.plants.behavior.impl.WallBehavior;

import java.util.Locale;
import java.util.Objects;

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
            case "sun_producer", "sunproducer", "produce_sun", "sun", "sun_burst" -> new SunProducerBehavior();
            case "shooter", "pea_shooter", "peashooter", "direct_shot", "burst_shot", "fire_shot", "ice_shot", "poison_shot" -> new ShooterBehavior();
            case "lobber", "lobber_shot", "pult", "lob" -> new LobberBehavior();
            case "explosive", "bomb", "mine", "aoe", "burst_explode" -> new ExplosiveBehavior();
            case "wall", "wall_nut", "defense" -> new WallBehavior();
            case "mint", "mint_behavior", "family_buff" -> new MintBehavior();
            case "modifier", "utility" -> new ModifierBehavior();
            case "through_strike", "pierce", "piercing_shot", "homing", "target_lock" -> new ShooterBehavior();
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

        if ("burst_sun".equals(normalized) || "instant_sun".equals(normalized) || "sun_burst".equals(normalized)) {
            return (plant, context) -> {
                int amount = plantFood.getIntParam("instantSunAmount",
                        plantFood.getIntParam("sunAmount", 150));
                int duration = plantFood.getIntParam("durationSeconds", 5);

                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(duration);
                context.spawnSun(amount);
            };
        }

        if ("burst_shot".equals(normalized) || "multi_shot".equals(normalized) || "double_projectile".equals(normalized)) {
            return (plant, context) -> {
                int duration = plantFood.getIntParam("durationSeconds", 5);
                int projectiles = plantFood.getIntParam("projectiles", 5);
                double damageMultiplier = plantFood.getDoubleParam("damageMultiplier", 1.5);

                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(duration);
                plant.getStats().putExtra("projectileCount", projectiles);
                plant.getStats().putExtra("damageMultiplier", damageMultiplier);
                plant.getStats().putExtra("plantFoodProjectileType", "burst");
            };
        }

        if ("explosive".equals(normalized) || "burst_explode".equals(normalized) || "aoe".equals(normalized)) {
            return (plant, context) -> {
                int duration = plantFood.getIntParam("durationSeconds", 1);
                int damage = plantFood.getIntParam("damage", plant.getStats().getAoeDamage());

                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(duration);
                context.damageArea(getIntRuntime(plant, "lane", 0), getIntRuntime(plant, "row", 0), damage);
            };
        }

        return new DefaultPlantFoodBehavior();
    }

    private static PlantBehavior fallbackByCategory(PlantDefinition definition) {
        switch (definition.getCategoryEnum()) {
            case SUN_PRODUCER:
                return new SunProducerBehavior();
            case SHOOTER:
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
                return new ModifierBehavior();
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

    private static int getIntRuntime(model.entity.plants.PlantInstance plant, String key, int defaultValue) {
        Object value = plant.getRuntimeState().get(key);
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