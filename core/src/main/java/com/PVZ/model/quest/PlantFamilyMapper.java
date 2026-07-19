package com.PVZ.model.quest;

import com.PVZ.model.enums.*;
import com.PVZ.model.entity.plants.PlantDefinition;

import java.util.Map;

public class PlantFamilyMapper {

    // نگاشت دستی گیاهانی که تحت پوشش PlantCategory یا PlantTag قرار نمی‌گیرند
    private static final Map<PlantType, PlantFamily> MINT_OVERRIDES = Map.ofEntries(
        // در صورت نیاز اینجا اضافه کنید
    );

    public static PlantFamily getFamily(PlantType plantType) {
        // 1. اگر خود گیاه یک Mint خالص است
        PlantFamily mintFamily = getMintFamily(plantType);
        if (mintFamily != null) return mintFamily;

        // 2. اگر در نگاشت دستی وجود دارد
        PlantFamily override = MINT_OVERRIDES.get(plantType);
        if (override != null) return override;

        // 3. تشخیص بر اساس PlantDefinition
        PlantDefinition def = plantType.getDefinition();
        if (def == null) return PlantFamily.GENERAL;

        PlantCategory category = def.getCategoryEnum();   // استفاده از نسخهٔ enum
        java.util.Set<PlantTag> tagSet = def.getTagEnums();         // مجموعهٔ PlantTag

        if (category == PlantCategory.EXPLOSIVE) return PlantFamily.EXPLOSIVE;
        if (category == PlantCategory.SUN_PRODUCER) return PlantFamily.SUN_PRODUCER;
        if (tagSet.contains(PlantTag.SHROOM)) return PlantFamily.MUSHROOM;

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
//            case PEPPER_MINT    -> PlantFamily.PEPPER_MINT;
//            case WINTER_MINT    -> PlantFamily.WINTER_MINT;
            case ENLIGHTEN_MINT -> PlantFamily.ENLIGHTEN_MINT;
            case APPEASE_MINT   -> PlantFamily.APPEASE_MINT;
            case ARMA_MINT      -> PlantFamily.ARMA_MINT;
            case BOMBARD_MINT   -> PlantFamily.BOMBARD_MINT;
            case ENFORCE_MINT   -> PlantFamily.ENFORCE_MINT;
            case REINFORCE_MINT -> PlantFamily.REINFORCE_MINT;
            case ENCHANT_MINT   -> PlantFamily.ENCHANT_MINT;
            case PIERCE_MINT    -> PlantFamily.PIERCE_MINT;
            case CAT_TAIL_MINT  -> PlantFamily.CAT_TAIL_MINT;
            default             -> null;
        };
    }
}
