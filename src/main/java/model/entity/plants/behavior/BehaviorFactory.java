package model.entity.plants.behavior;

import model.entity.plants.AbilitySpec;
import model.entity.plants.PlantDefinition;
import model.entity.plants.PlantInstance;
import model.entity.plants.behavior.impl.DefaultPlantFoodBehavior;
import model.entity.plants.behavior.impl.EmptyBehavior;
import model.entity.plants.behavior.impl.ExplosiveBehavior;
import model.entity.plants.behavior.impl.LobberBehavior;
import model.entity.plants.behavior.impl.MintBehavior;
import model.entity.plants.behavior.impl.ModifierBehavior;
import model.entity.plants.behavior.impl.ShooterBehavior;
import model.entity.plants.behavior.impl.SunProducerBehavior;
import model.entity.plants.behavior.impl.WallBehavior;
import model.entity.zombies.base.Zombie;

import java.util.List;
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
            case "sun_production", "growing_sun" -> new SunProducerBehavior();
            case "instant_sun" -> new InstantSunBehavior();
            case "direct_shot", "burst_shot", "fire_shot", "piercing_shot", "homing_shot", "wall_defense", "hypnotize" -> new ShooterBehavior();
            case "bounce_shot", "lobber_kernel" -> new LobberBehavior();
            case "explosion" -> new ExplosiveBehavior();
            case "melee_eat" -> new MeleeBehavior();
            case "move_zombies", "water_support", "copy_plant", "magnet_disarm" -> new ModifierBehavior();
            case "mint_family_buff" -> new MintBehavior();
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

        return switch (normalize(behaviorId)) {
            case "instant_sun" -> (plant, context) -> {
                int amount = plantFood.getIntParam("sunAmount", 150);
                int duration = Math.max(1, plantFood.getIntParam("durationSeconds", 1));
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(duration);
                context.spawnSun(amount);
            };
            case "burst_attack" -> (plant, context) -> {
                int duration = Math.max(1, plantFood.getIntParam("durationSeconds", 5));
                int projectiles = Math.max(1, plantFood.getIntParam("projectiles", 5));
                double damageMultiplier = plantFood.getDoubleParam("damageMultiplier", 1.5);
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(duration);
                plant.getStats().putExtra("projectileCount", projectiles);
                plant.getStats().putExtra("projectileDamageMultiplier", damageMultiplier);
                plant.getStats().putExtra("projectileType", plantFood.getStringParam("projectileType", "PEA"));
                plant.getStats().putExtra("projectileEffect", plantFood.getStringParam("effect", "BURST"));
            };
            case "freeze_burst", "fire_burst", "plasma_burst", "hypnotize", "water_clone", "lane_clear", "magnet_pulse" -> (plant, context) -> {
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(Math.max(1, plantFood.getIntParam("durationSeconds", 1)));
                plant.putRuntimeState("plantFoodEffect", normalize(behaviorId));
                plant.putRuntimeState("plantFoodEffectParams", plantFood.getParams());
            };
            case "explosion" -> (plant, context) -> {
                int damage = plantFood.getIntParam("damage", plant.getStats().getAoeDamage());
                int duration = Math.max(1, plantFood.getIntParam("durationSeconds", 1));
                plant.setPlantFoodActive(true);
                plant.setPlantFoodTicksRemaining(duration);
                Integer lane = asInt(plant.getRuntimeState().get("lane"), 0);
                Integer row = asInt(plant.getRuntimeState().get("row"), 0);
                context.damageArea(lane, row, damage);
            };
            case "none" -> new DefaultPlantFoodBehavior();
            default -> new DefaultPlantFoodBehavior();
        };
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

        if (abilitySpec.getResolvedBehaviorId() != null && !abilitySpec.getResolvedBehaviorId().isBlank()) {
            return abilitySpec.getResolvedBehaviorId();
        }

        if (abilitySpec.getKind() != null && !abilitySpec.getKind().isBlank()) {
            return abilitySpec.getKind();
        }

        if (abilitySpec.getRaw() != null && !abilitySpec.getRaw().isBlank()) {
            return abilitySpec.getRaw();
        }

        return abilitySpec.getStringParam("behaviorId", null);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private static Integer asInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static final class InstantSunBehavior implements PlantBehavior {
        @Override
        public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
            if (plant == null || context == null) {
                return;
            }

            Boolean triggered = (Boolean) plant.getRuntimeState().getOrDefault("instantSunTriggered", Boolean.FALSE);
            if (Boolean.TRUE.equals(triggered)) {
                return;
            }

            int amount = plant.getStats().getSunAmount();
            context.spawnSun(amount);
            plant.putRuntimeState("instantSunTriggered", Boolean.TRUE);
            plant.setCurrentHp(0);
        }
    }

    private static final class MeleeBehavior implements PlantBehavior {
        @Override
        public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
            if (plant == null || context == null) {
                return;
            }

            Integer lane = asInt(plant.getRuntimeState().get("lane"), 0);
            List<Zombie> zombies = context.getZombiesInLane(lane);
            if (zombies.isEmpty()) {
                return;
            }

            int damage = Math.max(1, plant.getStats().getDamage());
            for (Zombie zombie : zombies) {
                if (zombie != null && zombie.isInMeleeRange()) {
                    zombie.takeDamage(damage);
                    break;
                }
            }
        }
    }
}
