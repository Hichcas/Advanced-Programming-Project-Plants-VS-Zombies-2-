package com.PVZ.model.entity.zombies.base;

import java.util.HashMap;
import java.util.Map;

public class ZombieTexturePaths {
    private static final Map<String, String> PAM_PATHS = new HashMap<>();

    static {
        // PAM Animation File Mappings with resolution prefix
        PAM_PATHS.put("ZombieTutorialDefault", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM");
        PAM_PATHS.put("ZombieTutorialArmor1Default", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM");
        PAM_PATHS.put("ZombieTutorialArmor2Default", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM");
        PAM_PATHS.put("ZombieTutorialArmor4Default", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM");
        PAM_PATHS.put("ZombieTutorialFlagDefault", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL_FLAG/ZOMBIE_TUTORIAL_FLAG.PAM");
        PAM_PATHS.put("ZombieMummyDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
        PAM_PATHS.put("ZombieMummyArmor1Default", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
        PAM_PATHS.put("ZombieMummyArmor2Default", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
        PAM_PATHS.put("ZombieMummyArmor4Default", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
        PAM_PATHS.put("ZombieCamelDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_CAMEL/ZOMBIE_EGYPT_CAMEL.PAM");
        PAM_PATHS.put("ZombieCamelMiddle", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_CAMEL/ZOMBIE_EGYPT_CAMEL.PAM");
        PAM_PATHS.put("ZombieCamelRear", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_CAMEL/ZOMBIE_EGYPT_CAMEL.PAM");
        PAM_PATHS.put("ZombieRaDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_RA/ZOMBIE_EGYPT_RA.PAM");
        PAM_PATHS.put("ZombieRa", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_RA/ZOMBIE_EGYPT_RA.PAM");
        PAM_PATHS.put("ZombiePharaohDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_SARCOPHAGUS/ZOMBIE_EGYPT_SARCOPHAGUS.PAM");
        PAM_PATHS.put("ZombiePharaoh", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_SARCOPHAGUS/ZOMBIE_EGYPT_SARCOPHAGUS.PAM");
        PAM_PATHS.put("ZombieExplorerDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EXPLORER/ZOMBIE_EXPLORER.PAM");
        PAM_PATHS.put("ZombieExplorer", "768/INITIAL/ZOMBIE/ZOMBIE_EXPLORER/ZOMBIE_EXPLORER.PAM");
        PAM_PATHS.put("ZombieTombRaiserDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_TOMBRAISER/ZOMBIE_EGYPT_TOMBRAISER.PAM");
        PAM_PATHS.put("ZombieTombRaiser", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_TOMBRAISER/ZOMBIE_EGYPT_TOMBRAISER.PAM");
        PAM_PATHS.put("ZombieEgyptGargantuar", "768/INITIAL/ZOMBIE/EGYPT_GARGANTUAR/EGYPT_GARGANTUAR.PAM");
        PAM_PATHS.put("ZombieGargantuarBasic", "768/INITIAL/ZOMBIE/TUTORIAL_GARGANTUAR/TUTORIAL_GARGANTUAR.PAM");
        PAM_PATHS.put("ZombieEgyptImpDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_IMP/ZOMBIE_EGYPT_IMP.PAM");
        PAM_PATHS.put("ZombieTutorialImpDefault", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL_IMP/ZOMBIE_TUTORIAL_IMP.PAM");
        PAM_PATHS.put("ZombieZombossMechEgypt", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_ZOMBOSS/ZOMBIE_EGYPT_ZOMBOSS.PAM");
        // Zombotany has no dedicated PAM entry in the supplied animation catalog;
        // compose the plant head over the authentic tutorial zombie body.
        PAM_PATHS.put("ZombotanyPeashooterDefault", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM");
        PAM_PATHS.put("ZombotanyWallnutDefault", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM");
        PAM_PATHS.put("ZombotanyJalapenoDefault", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM");
        PAM_PATHS.put("ZombotanySquashDefault", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM");
        // World-specific PAM skins used by the graphical mini-games.
        PAM_PATHS.put("ZombieIceageDefault", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_BASIC/ZOMBIE_ICEAGE_BASIC.PAM");
        PAM_PATHS.put("ZombieIceageArmor1Default", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_BASIC/ZOMBIE_ICEAGE_BASIC.PAM");
        PAM_PATHS.put("ZombieIceageArmor2Default", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_BASIC/ZOMBIE_ICEAGE_BASIC.PAM");
        PAM_PATHS.put("ZombieIceageArmor3Default", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_BASIC_BRICK/ZOMBIE_ICEAGE_BASIC_BRICK.PAM");

        PAM_PATHS.put("ZombieIceageBlockheadDefault", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_BASIC_BRICK/ZOMBIE_ICEAGE_BASIC_BRICK.PAM");
        PAM_PATHS.put("ZombieIceageFlagDefault", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_FLAG/ZOMBIE_ICEAGE_FLAG.PAM");

        PAM_PATHS.put("ZombieIceAgeHunter", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_HUNTER/ZOMBIE_ICEAGE_HUNTER.PAM");
        PAM_PATHS.put("ZombieIceAgeDodo", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_DODORIDER/ZOMBIE_ICEAGE_DODORIDER.PAM");
        PAM_PATHS.put("ZombieIceAgeTroglobite", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_TROGLOBITE/ZOMBIE_ICEAGE_TROGLOBITE.PAM");


        PAM_PATHS.put("ZombieWeaselHoarder", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_WEASELHOARDER/ZOMBIE_ICEAGE_WEASELHOARDER.PAM");
        PAM_PATHS.put("ZombieWeaselHoarderDefault", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_WEASELHOARDER/ZOMBIE_ICEAGE_WEASELHOARDER.PAM");
        PAM_PATHS.put("ZombieWeasel", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_WEASEL/ZOMBIE_ICEAGE_WEASEL.PAM");
        PAM_PATHS.put("ZombieWeaselDefault", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_WEASEL/ZOMBIE_ICEAGE_WEASEL.PAM");
        PAM_PATHS.put("ZombieIceAgeGargantuar", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_GARGANTUAR/ZOMBIE_ICEAGE_GARGANTUAR.PAM");
        PAM_PATHS.put("ZombieIceAgeImpDefault", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_IMP/ZOMBIE_ICEAGE_IMP.PAM");
        PAM_PATHS.put("ZombieIceageImpDefault", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_IMP/ZOMBIE_ICEAGE_IMP.PAM");
        PAM_PATHS.put("ZombieZombossMechIceAge", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_ZOMBOSS/ZOMBIE_ICEAGE_ZOMBOSS.PAM");

        PAM_PATHS.put("ZombieBeachDefault", "768/FULL/ZOMBIE/ZOMBIE_BEACH_BASIC/ZOMBIE_BEACH_BASIC.PAM");
        PAM_PATHS.put("ZombieBeachArmor1Default", "768/FULL/ZOMBIE/ZOMBIE_BEACH_BASICFEM/ZOMBIE_BEACH_BASICFEM.PAM");
        PAM_PATHS.put("ZombieBeachArmor2Default", "768/FULL/ZOMBIE/ZOMBIE_BEACH_BASICFEM/ZOMBIE_BEACH_BASICFEM.PAM");
        PAM_PATHS.put("ZombieBeachFisherman", "768/FULL/ZOMBIE/ZOMBIE_BEACH_FISHERMAN/ZOMBIE_BEACH_FISHERMAN.PAM");
        PAM_PATHS.put("ZombieBeachFishermanDefault", "768/FULL/ZOMBIE/ZOMBIE_BEACH_FISHERMAN/ZOMBIE_BEACH_FISHERMAN.PAM");

        PAM_PATHS.put("ZombieBeachOctopus", "768/FULL/ZOMBIE/ZOMBIE_BEACH_OCTOPUS/ZOMBIE_BEACH_OCTOPUS.PAM");
        PAM_PATHS.put("ZombieBeachOctopusDefault", "768/FULL/ZOMBIE/ZOMBIE_BEACH_OCTOPUS/ZOMBIE_BEACH_OCTOPUS.PAM");
        PAM_PATHS.put("ZombieBeachSnorkel", "768/FULL/ZOMBIE/ZOMBIE_BEACH_SNORKELER/ZOMBIE_BEACH_SNORKELER.PAM");
        PAM_PATHS.put("ZombieBeachSnorkelDefault", "768/FULL/ZOMBIE/ZOMBIE_BEACH_SNORKELER/ZOMBIE_BEACH_SNORKELER.PAM");
        PAM_PATHS.put("ZombieBeachSnorkeler", "768/FULL/ZOMBIE/ZOMBIE_BEACH_SNORKELER/ZOMBIE_BEACH_SNORKELER.PAM");
        PAM_PATHS.put("ZombieBeachFastSwimmer", "768/FULL/ZOMBIE/ZOMBIE_BEACH_SNORKELER/ZOMBIE_BEACH_SNORKELER.PAM");

        PAM_PATHS.put("ZombieBeachSurfer", "768/FULL/ZOMBIE/ZOMBIE_BEACH_SURFER/ZOMBIE_BEACH_SURFER.PAM");
        PAM_PATHS.put("ZombieBeachSurferDefault", "768/FULL/ZOMBIE/ZOMBIE_BEACH_SURFER/ZOMBIE_BEACH_SURFER.PAM");
        PAM_PATHS.put("ZombieBeachImp", "768/FULL/ZOMBIE/ZOMBIE_BEACH_IMP_MERMAID/ZOMBIE_BEACH_IMP_MERMAID.PAM");
        PAM_PATHS.put("ZombieBeachImpDefault", "768/FULL/ZOMBIE/ZOMBIE_BEACH_IMP_MERMAID/ZOMBIE_BEACH_IMP_MERMAID.PAM");
        PAM_PATHS.put("ZombieBeachFlag", "768/FULL/ZOMBIE/ZOMBIE_BEACH_FLAG/ZOMBIE_BEACH_FLAG.PAM");
        PAM_PATHS.put("ZombieBeachFlagDefault", "768/FULL/ZOMBIE/ZOMBIE_BEACH_FLAG/ZOMBIE_BEACH_FLAG.PAM");
        PAM_PATHS.put("ZombieBeachGargantuar", "768/FULL/ZOMBIE/BEACH_GARGANTUAR/BEACH_GARGANTUAR.PAM");

        PAM_PATHS.put("ZombieDarkDefault", "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC/ZOMBIE_DARK_BASIC.PAM");
        PAM_PATHS.put("ZombieDarkArmor1Default", "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC_BRICK/ZOMBIE_DARK_BASIC_BRICK.PAM");
        PAM_PATHS.put("ZombieDarkArmor2Default", "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC_BRICK/ZOMBIE_DARK_BASIC_BRICK.PAM");
        PAM_PATHS.put("ZombieDarkArmor3Default", "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC_BRICK/ZOMBIE_DARK_BASIC_BRICK.PAM");
        PAM_PATHS.put("ZombieDarkArmor4Default", "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC_BRICK/ZOMBIE_DARK_BASIC_BRICK.PAM");
        PAM_PATHS.put("DEFAULT", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
    }

    public static String getPath(String alias) {
        return getPamPath(alias);
    }

    public static String getPamPath(String alias) {
        if (alias == null) return PAM_PATHS.get("DEFAULT");
        if (PAM_PATHS.containsKey(alias)) {
            return PAM_PATHS.get(alias);
        }
        if (!alias.endsWith("Default") && PAM_PATHS.containsKey(alias + "Default")) {
            return PAM_PATHS.get(alias + "Default");
        }
        if (alias.endsWith("Default") && PAM_PATHS.containsKey(alias.substring(0, alias.length() - 7))) {
            return PAM_PATHS.get(alias.substring(0, alias.length() - 7));
        }
        return PAM_PATHS.get("DEFAULT");
    }

    public static String getArmorSubBranchTrack(Zombie zombie) {
        if (zombie == null) return null;
        ZombieArmor armor = zombie.getArmor();
        if (armor == null || armor.isDestroyed()) return null;

        double base = armor.getBaseHealth();
        double current = armor.getHealth();
        if (base <= 0) return null;
        double ratio = current / base;

        ZombieArmor.ArmorType type = armor.getType();
        if (type == ZombieArmor.ArmorType.CONE) {
            if (ratio > 0.666) return "zombie_armor_cone_norm";
            if (ratio > 0.333) return "zombie_armor_cone_damage_01";
            return "zombie_armor_cone_damage_02";
        } else if (type == ZombieArmor.ArmorType.BUCKET) {
            if (ratio > 0.666) return "zombie_armor_bucket_norm";
            if (ratio > 0.333) return "zombie_armor_bucket_damage_01";
            return "zombie_armor_bucket_damage_02";
        } else if (type == ZombieArmor.ArmorType.BRICK || type == ZombieArmor.ArmorType.ICE_BLOCK) {
            if (ratio > 0.666) return "zombie_armor_brick_norm";
            if (ratio > 0.333) return "zombie_armor_brick_damage_01";
            return "zombie_armor_brick_damage_02";
        } else if (type == ZombieArmor.ArmorType.CROWN) {
            if (ratio > 0.666) return "zombie_armor_crown_norm";
            if (ratio > 0.333) return "zombie_armor_crown_damage_01";
            return "zombie_armor_crown_damage_02";
        }
        return null;
    }

    public static String getEffectivePamAlias(Zombie zombie) {
        if (zombie == null) {
            return "DEFAULT";
        }
        String alias = zombie.getAlias();
        ZombieArmor armor = zombie.getArmor();
        if (armor == null || armor.isDestroyed()) {
            if (alias != null && alias.contains("Armor")) {
                if (alias.startsWith("ZombieTutorialArmor")) return "ZombieTutorialDefault";
                if (alias.startsWith("ZombieMummyArmor")) return "ZombieMummyDefault";
                if (alias.startsWith("ZombieIceageArmor")) return "ZombieIceageDefault";
                if (alias.startsWith("ZombieBeachArmor")) return "ZombieBeachDefault";
                if (alias.startsWith("ZombieDarkArmor")) return "ZombieDarkDefault";
            }
            return alias != null ? alias : "DEFAULT";
        }
        return alias != null ? alias : "DEFAULT";
    }
}
