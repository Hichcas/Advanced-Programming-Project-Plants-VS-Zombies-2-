package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.AbilitySpec;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantFlag;

import java.util.List;


public class SunProducerBehavior implements PlantBehavior {

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", row), row);

        String plantKey = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        if ("sun_bean".equals(plantKey)) {
            return; // Sun Bean produces 5 Suns per received hit, not periodically.
        }
        if (plant.getStats().getSunDropAmount() > 0) {
            handleSunBean(plant, context, deltaTime, row, col, lane);
            return;
        }

        AbilitySpec ability = plant.getDefinition() == null ? null : plant.getDefinition().getBaseAbility();
        if (ability != null && hasGrowthParams(ability)) {
            handleGrowthAbility(plant, context, deltaTime, row, col, ability);
            return;
        }

        handleNormalProduction(plant, context, deltaTime, row, col);
    }

    @Override
    public void onDamaged(PlantInstance plant, BehaviorContext context, Zombie attacker,
                          int damageAmount, boolean destroyed) {
        if (plant == null || context == null) {
            return;
        }
        String plantKey = plant.getDefinition() == null || plant.getDefinition().getPlantKey() == null
            ? ""
            : plant.getDefinition().getPlantKey().toLowerCase();
        if (!"sun_bean".equals(plantKey)) {
            return;
        }
        int amount = plant.getStats() == null ? 5 : Math.max(5, plant.getStats().getSunDropAmount());
        int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        context.spawnSunAtSmall(row, col, amount, 260.0, 0.70f);
    }

    private void handleSunBean(PlantInstance plant, BehaviorContext context, double deltaTime,
                               int row, int col, int lane) {
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            return;
        }

        Double timer = asDouble(plant.getRuntimeState().getOrDefault("sunBeanTimer", 0.0),
            0.0);
        timer += deltaTime;

        if (timer >= 1.0) {
            int amount = plant.getStats().getSunDropAmount();
            if (amount <= 0) {
                amount = 5;
            }
            logSunProduction(plant, row, col);
            spawnProducedSun(plant, context, row, col, amount);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "producing", 0.6);
            timer = 0.0;
        }
        plant.putRuntimeState("sunBeanTimer", timer);
    }

    private boolean hasGrowthParams(AbilitySpec ability) {
        List<?> sunAmounts = asList(ability.getParam("sunAmounts"));
        List<?> stageTimes = asList(ability.getParam("growthStageTimes"));
        return !sunAmounts.isEmpty() && !stageTimes.isEmpty();
    }

    private void handleGrowthAbility(PlantInstance plant, BehaviorContext context, double deltaTime,
                                     int row, int col, AbilitySpec ability) {
        List<?> sunAmounts = asList(ability.getParam("sunAmounts"));
        List<?> stageTimes = asList(ability.getParam("growthStageTimes"));

        double timer = asDouble(plant.getRuntimeState().getOrDefault("growthTimer", 0.0),
            0.0);
        int stage = asInt(plant.getRuntimeState().getOrDefault("growthStage", 0),
            0);
        boolean triggered = asBoolean(plant.getRuntimeState().getOrDefault("growthTriggered",
            Boolean.FALSE), false);


        if (!triggered && stage == 0) {
            int amount = asInt(sunAmounts.get(0), plant.getStats().getSunAmount());
            logSunProduction(plant, row, col);
            spawnProducedSun(plant, context, row, col, amount);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "producing", 0.6);
            plant.putRuntimeState("growthTriggered", Boolean.TRUE);
        }

        timer += deltaTime;
        while (stage + 1 < sunAmounts.size() && stage < stageTimes.size()) {
            double nextThreshold = asDouble(stageTimes.get(stage), 0.0);
            if (timer < nextThreshold) {
                break;
            }
            stage++;
            int amount = asInt(sunAmounts.get(Math.min(stage, sunAmounts.size() - 1)),
                plant.getStats().getSunAmount());
            logSunProduction(plant, row, col);
            spawnProducedSun(plant, context, row, col, amount);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "producing", 0.6);
        }

        plant.putRuntimeState("growthTimer", timer);
        plant.putRuntimeState("growthStage", stage);
    }


    private void handleNormalProduction(PlantInstance plant, BehaviorContext context,
                                        double deltaTime, int row, int col) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("sunTimer", 0.0),
            0.0);
        timer += deltaTime;

        double productionTime = plant.getStats().getProductionTimeSeconds();
        if (productionTime <= 0) {
            productionTime = plant.getStats().getActionIntervalSeconds();
        }
        if (productionTime <= 0) {
            plant.putRuntimeState("sunTimer", timer);
            return;
        }

        if (timer >= productionTime) {
            timer = 0.0;
            int amount = plant.getStats().getSunAmount();
            if (amount <= 0) {
                amount = 50;
            }
            if (plant.getStats().hasFlag(PlantFlag.DOUBLE_SUN_CHANCE) && Math.random() < 0.5) {
                amount *= 2;
            }
            logSunProduction(plant, row, col);
            spawnProducedSun(plant, context, row, col, amount);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "producing", 0.6);
        }

        plant.putRuntimeState("sunTimer", timer);
    }


    private void spawnProducedSun(PlantInstance plant, BehaviorContext context,
                                  int row, int col, int amount) {
        String plantKey = plant.getDefinition() == null || plant.getDefinition().getPlantKey() == null
            ? "" : plant.getDefinition().getPlantKey().toLowerCase();
        if ("sunflower".equals(plantKey)) {
            // Phase 3: Sunflower-produced suns reach the ground 1/3 faster.
            context.spawnSunAt(row, col, amount, 270.0);
            return;
        }
        context.spawnSunAt(row, col, amount);
    }

    private void logSunProduction(PlantInstance plant, int row, int col) {
        String name = plant.getDefinition() == null ? "Unknown" : plant.getDefinition().getName();
        System.out.println("plant " + name + " produced a sun at (" + row + ", " + col + ")");
    }

    private static List<?> asList(Object value) {
        if (value instanceof List<?> list) {
            return list;
        }
        return List.of();
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
