package com.PVZ.model.quest;

import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.enums.PlantCategory;
import com.PVZ.model.enums.PlantFamily;
import com.PVZ.model.enums.PlantTag;
import com.PVZ.model.enums.PlantType;

import java.util.Map;

public class PlantFamilyMapper {

    private static final Map<PlantType, PlantFamily> MINT_OVERRIDES = Map.ofEntries();

    public static PlantFamily getFamily(PlantType plantType) {
        if (plantType == null) {
            return PlantFamily.GENERAL;
        }

        PlantFamily mintFamily = getMintFamily(plantType);
        if (mintFamily != null) return mintFamily;

        PlantFamily override = MINT_OVERRIDES.get(plantType);
        if (override != null) return override;

        PlantDefinition def = plantType.getDefinition();
        if (def == null) return PlantFamily.GENERAL;

        PlantCategory category = def.getCategoryEnum();
        boolean isShroom = def.getTagEnums() != null && def.getTagEnums().contains(PlantTag.SHROOM);

        if (isShroom) return PlantFamily.MUSHROOM;
        if (category == null) return PlantFamily.GENERAL;

        if (category == PlantCategory.EXPLOSIVE) return PlantFamily.EXPLOSIVE;
        if (category == PlantCategory.SUN_PRODUCER) return PlantFamily.SUN_PRODUCER;

        return switch (category) {
            case SHOOTER, LOBBER, THROUGH_STRIKE, HOMING -> PlantFamily.SHOOTER;
            case MELEE -> PlantFamily.MELEE;
            case WALL -> PlantFamily.WALL;
            case MODIFIER -> PlantFamily.MODIFIER;
            default -> PlantFamily.GENERAL;
        };
    }

    /**
     * Family used by stage rules that allow at most one pick from a family.
     * All mint plants belong to the single MINT bucket for selection/locked-family
     * rules; their individual mint families remain available to quest/behaviour logic.
     */
    public static PlantFamily getExclusivityFamily(PlantType plantType) {
        if (plantType == null) return PlantFamily.GENERAL;
        if (getMintFamily(plantType) != null) return PlantFamily.MINT;
        return getFamily(plantType);
    }

    public static PlantFamily getMintTargetFamily(PlantType plantType) {
        if (plantType == null || plantType.getDefinition() == null) return PlantFamily.GENERAL;
        if (plantType == PlantType.ENLIGHTEN_MINT) return PlantFamily.SUN_PRODUCER;
        if (plantType == PlantType.APPEASE_MINT) return PlantFamily.SHOOTER;
        if (plantType == PlantType.ARMA_MINT) return PlantFamily.LOBBER;
        if (plantType == PlantType.PIERCE_MINT) return PlantFamily.PIERCE_MINT;
        if (plantType == PlantType.CAT_TAIL_MINT) return PlantFamily.CAT_TAIL_MINT;
        if (plantType == PlantType.ENCHANT_MINT) return PlantFamily.MODIFIER;
        if (plantType == PlantType.REINFORCE_MINT) return PlantFamily.WALL;
        if (plantType == PlantType.ENFORCE_MINT) return PlantFamily.MELEE;
        if (plantType == PlantType.BOMBARD_MINT) return PlantFamily.EXPLOSIVE;
        return PlantFamily.GENERAL;
    }

    private static PlantFamily getMintFamily(PlantType plantType) {
        if (plantType == null) return null;
        return switch (plantType) {
            case ENLIGHTEN_MINT -> PlantFamily.ENLIGHTEN_MINT;
            case APPEASE_MINT -> PlantFamily.APPEASE_MINT;
            case ARMA_MINT -> PlantFamily.ARMA_MINT;
            case BOMBARD_MINT -> PlantFamily.BOMBARD_MINT;
            case ENFORCE_MINT -> PlantFamily.ENFORCE_MINT;
            case REINFORCE_MINT -> PlantFamily.REINFORCE_MINT;
            case ENCHANT_MINT -> PlantFamily.ENCHANT_MINT;
            case PIERCE_MINT -> PlantFamily.PIERCE_MINT;
            case CAT_TAIL_MINT -> PlantFamily.CAT_TAIL_MINT;
            default -> null;
        };
    }
}
