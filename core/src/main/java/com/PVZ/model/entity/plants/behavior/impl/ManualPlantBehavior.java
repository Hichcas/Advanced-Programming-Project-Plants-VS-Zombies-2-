package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.AbilitySpec;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;
import java.util.Locale;

public class ManualPlantBehavior implements PlantBehavior {
    private final PlantDefinition definition;
    private final AbilitySpec abilitySpec;

    public ManualPlantBehavior(PlantDefinition definition, AbilitySpec abilitySpec) {
        this.definition = definition;
        this.abilitySpec = abilitySpec;
    }

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        String behaviorId = normalize(resolveBehaviorId());
        int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", row), row);

        switch (behaviorId) {
            case "instant_sun" -> handleInstantSun(plant, context, row, col);
            case "move_zombies", "garlic" -> handleMoveZombies(plant, context, lane, deltaTime);
            case "magnet_disarm", "magnet_pulse" -> handleMagnet(plant, context, lane, deltaTime);
            case "hypnotize" -> handleHypnotize(plant, context, lane, deltaTime);
            case "copy_plant" -> handleCopyPlant(plant);
            case "water_support" -> handleWaterSupport(plant);
            case "melee_eat" -> handleMelee(plant, context, lane, row, deltaTime);
            case "sun_production" -> handleSunBeanLike(plant, context, row, col, lane, deltaTime);
            default -> {
                if (definition != null && definition.getCategoryEnum().name().equals("MELEE")) {
                    handleMelee(plant, context, lane, row, deltaTime);
                }
            }
        }
    }

    private void handleInstantSun(PlantInstance plant, BehaviorContext context, int row, int col) {
        boolean triggered = asBoolean(plant.getRuntimeState().getOrDefault("instantSunTriggered", Boolean.FALSE), false);
        if (triggered) {
            return;
        }

        int amount = plant.getStats().getSunAmount();
        if (amount <= 0 && abilitySpec != null) {
            amount = abilitySpec.getIntParam("sunAmount", 375);
        }
        if (amount <= 0) {
            amount = 375;
        }
        System.out.println("plant " + plant.getDefinition().getName() + " produced a sun at (" + row + ", " + col + ")");
        context.spawnSunAt(row, col, amount);
        plant.putRuntimeState("instantSunTriggered", Boolean.TRUE);
    }

    private void handleMoveZombies(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("moveTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = Math.max(1.0, plant.getStats().getActionIntervalSeconds());
        if (timer < cooldown) {
            plant.putRuntimeState("moveTimer", timer);
            return;
        }
        timer = 0.0;
        int targetLane = lane > 0 ? lane - 1 : lane + 1;
        context.moveZombiesFromLane(lane, targetLane);
        plant.putRuntimeState("moveTimer", timer);
    }

    private void handleMagnet(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("magnetTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = Math.max(1.0, plant.getStats().getActionIntervalSeconds());
        if (timer < cooldown) {
            plant.putRuntimeState("magnetTimer", timer);
            return;
        }
        timer = 0.0;
        context.disarmZombiesInLane(lane);
        plant.putRuntimeState("magnetTimer", timer);
    }

    private void handleHypnotize(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("hypnoTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = Math.max(1.0, plant.getStats().getActionIntervalSeconds());
        if (timer < cooldown) {
            plant.putRuntimeState("hypnoTimer", timer);
            return;
        }
        timer = 0.0;
        context.hypnotizeZombiesInLane(lane, 3.0);
        plant.putRuntimeState("hypnoTimer", timer);
    }

    private void handleCopyPlant(PlantInstance plant) {
        Object copied = plant.getStats().getExtra("copiedPlantType");
        if (copied != null) {
            plant.putRuntimeState("copiedPlantType", String.valueOf(copied));
        }
    }

    private void handleWaterSupport(PlantInstance plant) {
        plant.putRuntimeState("waterSupport", Boolean.TRUE);
    }

    private void handleMelee(PlantInstance plant, BehaviorContext context, int lane, int row, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("meleeTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }
        if (timer < cooldown) {
            plant.putRuntimeState("meleeTimer", timer);
            return;
        }
        timer = 0.0;
        int damage = Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage());
        if (damage <= 0) {
            damage = 30;
        }
        context.damageArea(lane, row, damage);
        if (plant.getStats().getEatTimeSeconds() > 0) {
            plant.putRuntimeState("digestTimer", plant.getStats().getEatTimeSeconds());
        }
        plant.putRuntimeState("meleeTimer", timer);
    }

    private void handleSunBeanLike(PlantInstance plant, BehaviorContext context, int row, int col, int lane, double deltaTime) {
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            return;
        }

        Double timer = asDouble(plant.getRuntimeState().getOrDefault("sunBeanTimer", 0.0), 0.0);
        timer += deltaTime;
        if (timer >= 1.0) {
            int amount = plant.getStats().getSunDropAmount();
            if (amount <= 0) {
                amount = 5;
            }
            System.out.println("plant " + plant.getDefinition().getName() + " produced a sun at (" + row + ", " + col + ")");
            context.spawnSunAt(row, col, amount);
            timer = 0.0;
        }
        plant.putRuntimeState("sunBeanTimer", timer);
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
        if (definition != null && definition.getBaseAbility() != null) {
            return definition.getBaseAbility().getResolvedBehaviorId();
        }
        return "";
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static boolean asBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value == null ? defaultValue : Boolean.parseBoolean(String.valueOf(value));
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
