package com.PVZ.model.entity.zombies.factory;

import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieArmor;
import com.PVZ.model.entity.zombies.types.basic.*;
import com.PVZ.model.entity.zombies.types.heavy_gargantuar.ZombieGargantuar;
import com.PVZ.model.entity.zombies.types.heavy_gargantuar.ZombieImp;
import com.PVZ.model.entity.zombies.types.ranged_caster.*;
import com.PVZ.model.entity.zombies.types.special_movement.*;
import com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechCowboy;
import com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechDark;
import com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechEgypt;
import com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechPirate;
import com.PVZ.model.entity.zombies.types.zombotany.*;

/**
 * Factory for creating Zombie instances based on string aliases.
 * Refactored to comply with Checkstyle and PMD (method length ≤ 50 lines).
 */
public final class ZombieFactory {

    private ZombieFactory() {
        // Private constructor to prevent instantiation
    }

    public static Zombie createZombie(String alias) {
        if (alias == null) {
            throw new IllegalArgumentException("Zombie alias cannot be null");
        }

        // Delegate to specific handler methods
        Zombie zombie = createBasicZombie(alias);
        if (zombie != null) {
            return zombie;
        }

        zombie = createArmoredZombie(alias);
        if (zombie != null) {
            return zombie;
        }

        zombie = createGargantuarAndImp(alias);
        if (zombie != null) {
            return zombie;
        }

        zombie = createSpecialMovementZombie(alias);
        if (zombie != null) {
            return zombie;
        }

        zombie = createRangedCasterZombie(alias);
        if (zombie != null) {
            return zombie;
        }

        zombie = createZombossAndZombotany(alias);
        if (zombie != null) {
            return zombie;
        }

        throw new IllegalArgumentException("Unknown zombie alias: " + alias);
    }

    /**
     * Handles basic tutorial zombies without armor or with simple armor.
     */
    private static Zombie createBasicZombie(String alias) {
        return switch (alias) {
            case "ZombieTutorialDefault" -> new ZombieTutorial(alias, null);
            case "ZombieTutorialFlagDefault" -> new ZombieTutorial(alias, null);
            default -> null;
        };
    }

    /**
     * Handles zombies with armor (cone, bucket, brick, etc.) grouped by theme.
     */
    private static Zombie createArmoredZombie(String alias) {
        return switch (alias) {
            // Tutorial armored variants
            case "ZombieTutorialArmor1Default" ->
                new ZombieTutorial(alias, createArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieTutorialArmor2Default" ->
                new ZombieTutorial(alias, createArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieTutorialArmor4Default" ->
                new ZombieTutorial(alias, createArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true));

            // Mummy armored variants
            case "ZombieMummyDefault" -> new ZombieMummy(alias, null);
            case "ZombieMummyArmor1Default" ->
                new ZombieMummy(alias, createArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieMummyArmor2Default" ->
                new ZombieMummy(alias, createArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieMummyArmor4Default" ->
                new ZombieMummy(alias, createArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true));

            // Iceage armored variants
            case "ZombieIceageDefault" -> new ZombieIceage(alias, null);
            case "ZombieIceageArmor1Default" ->
                new ZombieIceage(alias, createArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieIceageArmor2Default" ->
                new ZombieIceage(alias, createArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieIceageArmor3Default" ->
                new ZombieIceage(alias, createArmor(ZombieArmor.ArmorType.ICE_BLOCK, 800, false, false, true));

            // Beach armored variants
            case "ZombieBeachDefault" -> new ZombieBeach(alias, null);
            case "ZombieBeachArmor1Default" ->
                new ZombieBeach(alias, createArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieBeachArmor2Default" ->
                new ZombieBeach(alias, createArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));

            // Dark armored variants
            case "ZombieDarkDefault" -> new ZombieDark(alias, null);
            case "ZombieDarkArmor1Default" ->
                new ZombieDark(alias, createArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieDarkArmor2Default" ->
                new ZombieDark(alias, createArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieDarkArmor3Default" ->
                new ZombieDark(alias,
                    createArmor(ZombieArmor.ArmorType.SHOULDER_ARMOR, 1600, false, false, false),
                    createArmor(ZombieArmor.ArmorType.CROWN, 1600, true, true, true));
            case "ZombieDarkArmor4Default" ->
                new ZombieDark(alias, createArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true));

            default -> null;
        };
    }

    /**
     * Handles Gargantuar and Imp zombies.
     */
    private static Zombie createGargantuarAndImp(String alias) {
        return switch (alias) {
            case "ZombieGargantuarBasic" ->
                new ZombieGargantuar(alias, ZombieGargantuar.Theme.BASIC);
            case "ZombieEgyptGargantuar" ->
                new ZombieGargantuar(alias, ZombieGargantuar.Theme.EGYPT);
            case "ZombieIceAgeGargantuar" ->
                new ZombieGargantuar(alias, ZombieGargantuar.Theme.ICEAGE);
            case "ZombieBeachGargantuar" ->
                new ZombieGargantuar(alias, ZombieGargantuar.Theme.BEACH);
            case "ZombieDarkGargantuar" ->
                new ZombieGargantuar(alias, ZombieGargantuar.Theme.DARK);
            case "ZombieTutorialImpDefault" ->
                new ZombieImp(alias, ZombieImp.Theme.BASIC);
            case "ZombieEgyptImpDefault" ->
                new ZombieImp(alias, ZombieImp.Theme.EGYPT);
            case "ZombieIceageImpDefault" ->
                new ZombieImp(alias, ZombieImp.Theme.ICEAGE);
            case "ZombieBeachImpDefault" ->
                new ZombieImp(alias, ZombieImp.Theme.BEACH);
            case "ZombieDarkImpDefault" ->
                new ZombieImp(alias, ZombieImp.Theme.DARK);
            default -> null;
        };
    }

    /**
     * Handles special movement zombies (Pharaoh, Camel, Troglobite, Dodo, Weasel, etc.)
     */
    private static Zombie createSpecialMovementZombie(String alias) {
        return switch (alias) {
            case "ZombiePharaohDefault" -> new ZombiePharaoh();
            case "ZombieCamelDefault" -> new ZombieCamel();
            case "ZombieIceAgeTroglobite" -> new ZombieIceAgeTroglobite();
            case "ZombieIceAgeDodo" -> new ZombieIceAgeDodo();
            case "ZombieWeaselHoarderDefault" -> new ZombieWeaselHoarder();
            case "ZombieWeaselDefault" -> new ZombieWeasel();
            case "ZombieBeachSnorkel" -> new ZombieBeachSnorkel();
            case "ZombieBeachSurfer" -> new ZombieBeachSurfer();
            case "ZombieBeachFastSwimmer" -> new ZombieBeachFastSwimmer();
            default -> null;
        };
    }

    /**
     * Handles ranged/caster zombies (Ra, Explorer, TombRaiser, Hunter, Fisherman, Octopus, Wizard, Juggler, King)
     */
    private static Zombie createRangedCasterZombie(String alias) {
        return switch (alias) {
            case "ZombieRaDefault" -> new ZombieRa();
            case "ZombieExplorerDefault" -> new ZombieExplorer();
            case "ZombieTombRaiserDefault" -> new ZombieTombRaiser();
            case "ZombieIceAgeHunter" -> new ZombieIceAgeHunter();
            case "ZombieBeachFisherman" -> new ZombieBeachFisherman();
            case "ZombieBeachOctopus" -> new ZombieBeachOctopus();
            case "ZombieWizardDefault" -> new ZombieWizard();
            case "ZombieDarkJugglerDefault" -> new ZombieDarkJuggler();
            case "ZombieDarkKing" -> new ZombieDarkKing();
            default -> null;
        };
    }

    /**
     * Handles Zomboss mechs and Zombotany plants.
     */
    private static Zombie createZombossAndZombotany(String alias) {
        return switch (alias) {
            case "ZombieZombossMechEgypt" -> new ZombieZombossMechEgypt();
            case "ZombieZombossMechPirate" -> new ZombieZombossMechPirate();
            case "ZombieZombossMechCowboy" -> new ZombieZombossMechCowboy();
            case "ZombieZombossMechDark" -> new ZombieZombossMechDark();
            case "ZombotanyPeashooterDefault" -> new ZombotanyPeashooter();
            case "ZombotanyWallnutDefault" -> new ZombotanyWallnut();
            case "ZombotanyJalapenoDefault" -> new ZombotanyJalapeno();
            case "ZombotanySquashDefault" -> new ZombotanySquash();
            default -> null;
        };
    }

    /**
     * Helper to create ZombieArmor instances with consistent parameters.
     */
    private static ZombieArmor createArmor(ZombieArmor.ArmorType type, int health,
                                           boolean canBeHypnotized, boolean isMetal, boolean isRemovable) {
        return new ZombieArmor(type, health, canBeHypnotized, isMetal, isRemovable);
    }
}
