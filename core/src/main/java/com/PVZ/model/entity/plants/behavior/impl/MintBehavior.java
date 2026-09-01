package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;


public class MintBehavior implements PlantBehavior {
    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        Boolean triggered = (Boolean) plant.getRuntimeState().getOrDefault("mintTriggered", Boolean.FALSE);
        if (triggered != null && triggered) {
            return;
        }
        plant.putRuntimeState("mintTriggered", Boolean.TRUE);

        com.PVZ.model.enums.PlantType mintType = plant.getType();
        com.PVZ.model.enums.PlantFamily targetFamily =
            com.PVZ.model.quest.PlantFamilyMapper.getMintTargetFamily(mintType);

        int selfRow = asInt(plant.getRuntimeState().getOrDefault("row", -1), -1);
        int selfCol = asInt(plant.getRuntimeState().getOrDefault("col", -1), -1);

        for (Plant other : context.getAllPlants()) {
            if (other == null || other.getDefinition() == null) {
                continue;
            }
            int otherRow = asInt(other.getRuntimeState("row"), -2);
            int otherCol = asInt(other.getRuntimeState("col"), -2);
            boolean isSelf = otherRow == selfRow && otherCol == selfCol;
            if (isSelf) {
                continue;
            }
            com.PVZ.model.enums.PlantFamily otherFamily =
                com.PVZ.model.quest.PlantFamilyMapper.getFamily(other.getType());
            boolean matches = switch (targetFamily) {
                case SUN_PRODUCER -> other.getDefinition().getCategoryEnum()
                    == com.PVZ.model.enums.PlantCategory.SUN_PRODUCER;
                case SHOOTER -> other.getDefinition().getCategoryEnum() == com.PVZ.model.enums.PlantCategory.SHOOTER;
                case LOBBER -> other.getDefinition().getCategoryEnum() == com.PVZ.model.enums.PlantCategory.LOBBER;
                case PIERCE_MINT -> other.getDefinition().getCategoryEnum()
                    == com.PVZ.model.enums.PlantCategory.THROUGH_STRIKE;
                case CAT_TAIL_MINT -> other.getDefinition().getCategoryEnum()
                    == com.PVZ.model.enums.PlantCategory.HOMING;
                case MODIFIER -> other.getDefinition().getCategoryEnum() == com.PVZ.model.enums.PlantCategory.MODIFIER;
                case WALL -> other.getDefinition().getCategoryEnum() == com.PVZ.model.enums.PlantCategory.WALL;
                case MELEE -> other.getDefinition().getCategoryEnum() == com.PVZ.model.enums.PlantCategory.MELEE;
                case EXPLOSIVE -> other.getDefinition().getCategoryEnum()
                    == com.PVZ.model.enums.PlantCategory.EXPLOSIVE;
                default -> otherFamily == targetFamily;
            };
            if (matches && other.hasPlantFoodEffect()) {
                other.applyPlantFood(context);
            }
        }
        context.removePlant(selfRow, selfCol);
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
