package com.PVZ.model.entity.plants.behavior;

import com.PVZ.model.entity.plants.AbilitySpec;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantStats;
import com.PVZ.model.entity.plants.behavior.impl.CompositeBehavior;
import com.PVZ.model.entity.plants.behavior.impl.DefaultPlantFoodBehavior;
import com.PVZ.model.entity.plants.behavior.impl.ElectricBlueberryBehavior;
import com.PVZ.model.entity.plants.behavior.impl.EmptyBehavior;
import com.PVZ.model.entity.plants.behavior.impl.ExplosiveBehavior;
import com.PVZ.model.entity.plants.behavior.impl.LobberBehavior;
import com.PVZ.model.entity.plants.behavior.impl.ManualPlantBehavior;
import com.PVZ.model.entity.plants.behavior.impl.ManualPlantFoodBehavior;
import com.PVZ.model.entity.plants.behavior.impl.MeleeEatBehavior;
import com.PVZ.model.entity.plants.behavior.impl.MintBehavior;
import com.PVZ.model.entity.plants.behavior.impl.ModifierBehavior;
import com.PVZ.model.entity.plants.behavior.impl.ShooterBehavior;
import com.PVZ.model.entity.plants.behavior.impl.SunProducerBehavior;
import com.PVZ.model.entity.plants.behavior.impl.TorchwoodBehavior;
import com.PVZ.model.entity.plants.behavior.impl.UtilityLaneBehavior;
import com.PVZ.model.entity.plants.behavior.impl.WallBehavior;

import java.util.ArrayList;
import java.util.Collections;
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

        String plantKeyForWallCheck = definition.getPlantKey() == null ? "" : normalize(definition.getPlantKey());
        if (definition.getCategoryEnum() == com.PVZ.model.enums.PlantCategory.WALL
                && !"garlic".equals(plantKeyForWallCheck)) {
            return new WallBehavior();
        }

        // These plants' baseAbility.kind is the same generic string as an ordinary
        // peashooter/lobber (direct_shot / bounce_shot / homing_shot), but their raw
        // Persian description is a genuinely different shot pattern (multi-lane,
        // diagonal, front+back, whole-lawn homing, staggered bounce). ManualPlantBehavior
        // now has dedicated handlers for each of these, keyed by plant id.
        String plantKey = definition.getPlantKey() == null ? "" : normalize(definition.getPlantKey());
        switch (plantKey) {
            case "rotobaga", "threepeater", "split_pea", "starfruit", "cat_tail", "bowling_bulb" -> {
                return new ManualPlantBehavior(definition, definition.getBaseAbility());
            }
            case "garlic" -> {
                // "با خورده شدن، زامبی را مجبور به حرکت به لاین مجاور می‌کند" - blocks like a
                // wall AND periodically shoves the attacking zombie into an adjacent lane.
                // It was previously forced into plain WallBehavior by the category==WALL
                // rule above and never pushed anyone.
                return new CompositeBehavior(new WallBehavior(),
                        new UtilityLaneBehavior(UtilityLaneBehavior.Mode.MOVE_ZOMBIES));
            }
            case "torchwood" -> {
                // "تبدیل تیر عبوری به آتشی" - torchwood itself never attacks; it just makes
                // its lane-neighbors' shots fire-shots. Previously it fell through to
                // ShooterBehavior (its kind is PIERCING_SHOT) and incorrectly shot at zombies
                // on its own.
                return new TorchwoodBehavior();
            }
            case "electric_blueberry" -> {
                // "شلیک رعدوبرق (جهت رندوم، نابودی کامل یک زامبی)" - an instant kill, not
                // ordinary projectile damage, so plain ShooterBehavior undersells it.
                return new ElectricBlueberryBehavior();
            }
            default -> {
            }
        }

        return switch (normalize(behaviorId)) {
            case "sun_producer", "sunproducer", "produce_sun", "sun", "sun_burst", "sun_production", "growing_sun" -> new SunProducerBehavior();
            case "instant_sun" -> new ManualPlantBehavior(definition, definition.getBaseAbility());
            case "shooter", "pea_shooter", "peashooter", "direct_shot", "burst_shot", "fire_shot", "ice_shot", "poison_shot", "piercing_shot", "homing_shot", "target_lock" -> new ShooterBehavior();
            case "lobber", "lobber_kernel", "lob", "bounce_shot", "pult" -> new LobberBehavior();
            case "explosive", "bomb", "mine", "aoe", "burst_explode", "lane_clear" -> new ExplosiveBehavior();
            case "wall", "wall_nut", "defense", "wall_defense" -> new WallBehavior();
            case "mint", "mint_family_buff", "family_buff" -> new MintBehavior();
            case "modifier", "utility", "water_support", "copy_plant" -> new ManualPlantBehavior(definition, definition.getBaseAbility());
            case "magnet_disarm" -> new UtilityLaneBehavior(UtilityLaneBehavior.Mode.MAGNET_DISARM);
            case "move_zombies" -> new UtilityLaneBehavior(UtilityLaneBehavior.Mode.MOVE_ZOMBIES);
            case "hypnotize" -> new UtilityLaneBehavior(UtilityLaneBehavior.Mode.HYPNOTIZE);
            case "magnet_pulse" -> new ManualPlantBehavior(definition, definition.getBaseAbility());
            case "melee_eat", "melee", "attack_melee" -> new MeleeEatBehavior();
            default -> fallbackByCategory(definition);
        };
    }

    public static PlantFoodBehavior createPlantFoodBehavior(PlantDefinition definition) {
        if (definition == null || definition.getPlantFoodEffect() == null) {
            return new DefaultPlantFoodBehavior();
        }

        AbilitySpec plantFood = definition.getPlantFoodEffect();
        String plantKey = definition.getPlantKey() == null ? "" : normalize(definition.getPlantKey());
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

                switch (plantKey) {
                    case "rotobaga" -> {
                        projectiles = 4;
                        damageMultiplier = 2.0;
                    }
                    case "pea_pod" -> {
                        projectiles = 1;
                        damageMultiplier = 20.0;
                    }
                    case "repeater" -> {
                        projectiles = 3;
                        damageMultiplier = 20.0;
                    }
                    case "threepeater" -> {
                        projectiles = 3;
                        damageMultiplier = 2.0;
                    }
                    case "split_pea" -> {
                        projectiles = 3;
                        damageMultiplier = 2.0;
                    }
                    case "starfruit" -> {
                        projectiles = 5;
                        damageMultiplier = 2.0;
                    }
                    case "cat_tail", "cattail" -> {
                        projectiles = 1;
                        damageMultiplier = 2.0;
                    }
                    case "mega_gatling_pea" -> {
                        projectiles = 4;
                        damageMultiplier = 20.0;
                    }
                    default -> {
                    }
                }

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

                if ("caulipower".equals(plantKey)) {
                    List<com.PVZ.model.entity.zombies.base.Zombie> zombies = new ArrayList<>(context.getAllZombies());
                    Collections.shuffle(zombies);
                    int targets = Math.min(3, zombies.size());
                    for (int i = 0; i < targets; i++) {
                        com.PVZ.model.entity.zombies.base.Zombie zombie = zombies.get(i);
                        if (zombie != null && !zombie.isDead()) {
                            zombie.hypnotize(5.0f);
                        }
                    }
                    return;
                }

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

                switch (plantKey) {
                    case "torchwood" -> {
                        plant.getStats().putExtra("fireAttack", Boolean.TRUE);
                        plant.getStats().putExtra("damageMultiplier", 3.0);
                        plant.getStats().putExtra("plantFoodDamageMultiplier", 3.0);
                        plant.getStats().putExtra("torchwoodBoost", Boolean.TRUE);
                    }
                    case "tangle_kelp" -> context.killRandomZombies(3);
                    default -> plant.putRuntimeState("waterCloneReady", Boolean.TRUE);
                }
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
                return new ManualPlantBehavior(definition, definition.getBaseAbility());
            case MELEE:
                return new MeleeEatBehavior();
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
