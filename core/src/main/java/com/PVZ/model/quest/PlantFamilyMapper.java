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
        PlantFamily mintFamily = getMintFamily(plantType);
        if (mintFamily != null) return mintFamily;

        PlantFamily override = MINT_OVERRIDES.get(plantType);
        if (override != null) return override;

        PlantDefinition def = plantType.getDefinition();
        if (def == null) return PlantFamily.GENERAL;

        PlantCategory category = def.getCategoryEnum();
        boolean isShroom = def.getTagEnums().contains(PlantTag.SHROOM);

        if (isShroom) return PlantFamily.MUSHROOM;

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

    private static PlantFamily getMintFamily(PlantType plantType) {
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
