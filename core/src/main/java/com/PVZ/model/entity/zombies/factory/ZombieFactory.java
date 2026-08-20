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
import com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechBeach;
import com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechIceAge;
import com.PVZ.model.entity.zombies.types.zombotany.ZombotanyJalapeno;
import com.PVZ.model.entity.zombies.types.zombotany.ZombotanyPeashooter;
import com.PVZ.model.entity.zombies.types.zombotany.ZombotanySquash;
import com.PVZ.model.entity.zombies.types.zombotany.ZombotanyWallnut;


public final class ZombieFactory {

    private ZombieFactory() {
    }

    public static Zombie createZombie(String alias) {
        if (alias == null) {
            throw new IllegalArgumentException("Zombie alias cannot be null");
        }

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


    private static Zombie createBasicZombie(String alias) {
        return switch (alias) {
            case "ZombieTutorialDefault", "ZombieTutorialFlagDefault" -> new ZombieTutorial(alias, null);
            default -> null;
        };
    }

    private static Zombie createArmoredZombie(String alias) {
        Zombie z = createTutorialArmored(alias);
        if (z != null) return z;
        z = createMummyArmored(alias);
        if (z != null) return z;
        z = createIceageArmored(alias);
        if (z != null) return z;
        z = createBeachArmored(alias);
        if (z != null) return z;
        return createDarkArmored(alias);
    }

    private static Zombie createTutorialArmored(String alias) {
        return switch (alias) {
            case "ZombieTutorialArmor1Default" ->
                new ZombieTutorial(alias, createArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieTutorialArmor2Default" ->
                new ZombieTutorial(alias, createArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieTutorialArmor4Default" ->
                new ZombieTutorial(alias, createArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true));
            default -> null;
        };
    }

    private static Zombie createMummyArmored(String alias) {
        return switch (alias) {
            case "ZombieMummyDefault" -> new ZombieMummy(alias, null);
            case "ZombieMummyArmor1Default" ->
                new ZombieMummy(alias, createArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieMummyArmor2Default" ->
                new ZombieMummy(alias, createArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieMummyArmor4Default" ->
                new ZombieMummy(alias, createArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true));
            default -> null;
        };
    }

    private static Zombie createIceageArmored(String alias) {
        return switch (alias) {
            case "ZombieIceageDefault", "ZombieIceAgeDefault" -> new ZombieIceage(alias, null);
            case "ZombieIceageArmor1Default", "ZombieIceAgeArmor1Default" ->
                new ZombieIceage(alias, createArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieIceageArmor2Default", "ZombieIceAgeArmor2Default" ->
                new ZombieIceage(alias, createArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieIceageArmor3Default", "ZombieIceAgeArmor3Default" ->
                new ZombieIceage(alias, createArmor(ZombieArmor.ArmorType.ICE_BLOCK, 800, false, false, true));
            default -> null;
        };
    }

    private static Zombie createBeachArmored(String alias) {
        return switch (alias) {
            case "ZombieBeachDefault" -> new ZombieBeach(alias, null);
            case "ZombieBeachArmor1Default" ->
                new ZombieBeach(alias, createArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieBeachArmor2Default" ->
                new ZombieBeach(alias, createArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            default -> null;
        };
    }

    private static Zombie createDarkArmored(String alias) {
        return switch (alias) {
            case "ZombieDarkDefault" -> new ZombieDark(alias, null);
            case "ZombieDarkArmor1Default" ->
                new ZombieDark(alias, createArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieDarkArmor2Default" ->
                new ZombieDark(alias, createArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieDarkArmor3Default" -> new ZombieDark(alias,
                createArmor(ZombieArmor.ArmorType.CROWN, 1800, true, true, true));
            case "ZombieDarkArmor4Default" ->
                new ZombieDark(alias, createArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true));
            default -> null;
        };
    }

    private static Zombie createGargantuarAndImp(String alias) {
        return switch (alias) {
            case "ZombieGargantuarBasic" -> new ZombieGargantuar(alias, ZombieGargantuar.Theme.BASIC);
            case "ZombieEgyptGargantuar" -> new ZombieGargantuar(alias, ZombieGargantuar.Theme.EGYPT);
            case "ZombieIceAgeGargantuar" -> new ZombieGargantuar(alias, ZombieGargantuar.Theme.ICEAGE);
            case "ZombieBeachGargantuar" -> new ZombieGargantuar(alias, ZombieGargantuar.Theme.BEACH);
            case "ZombieDarkGargantuar" -> new ZombieGargantuar(alias, ZombieGargantuar.Theme.DARK);
            case "ZombieTutorialImpDefault" -> new ZombieImp(alias, ZombieImp.Theme.BASIC);
            case "ZombieEgyptImpDefault" -> new ZombieImp(alias, ZombieImp.Theme.EGYPT);
            case "ZombieIceageImpDefault", "ZombieIceAgeImpDefault" -> new ZombieImp(alias, ZombieImp.Theme.ICEAGE);
            case "ZombieBeachImpDefault" -> new ZombieImp(alias, ZombieImp.Theme.BEACH);
            case "ZombieDarkImpDefault", "ZombieDarkImpDragon", "ZombieDarkImpDragonDefault" -> new ZombieImp(alias, ZombieImp.Theme.DARK);
            default -> null;
        };
    }

    private static Zombie createSpecialMovementZombie(String alias) {
        return switch (alias) {
            case "ZombiePharaohDefault" -> new ZombiePharaoh();
            case "ZombieCamelDefault" -> new ZombieCamel();
            case "ZombieIceAgeTroglobite" -> new ZombieIceAgeTroglobite();
            case "ZombieIceAgeDodo" -> new ZombieIceAgeDodo();
            case "ZombieWeaselHoarderDefault" -> new ZombieWeaselHoarder();
            case "ZombieWeaselDefault" -> new ZombieWeasel();
            case "ZombieBeachSnorkel", "ZombieBeachSnorkelDefault" -> new ZombieBeachSnorkel();
            case "ZombieBeachSurfer", "ZombieBeachSurferDefault" -> new ZombieBeachSurfer();
            case "ZombieBeachFastSwimmer", "ZombieBeachFastSwimmerDefault" -> new ZombieBeachFastSwimmer();
            case "ZombieModernAllStar", "ZombieFootball", "ZombieAllStar" -> new ZombieAllStar(alias);
            case "Zombie80sArcade", "ZombieArcade" -> new ZombieArcade(alias);
            case "ZombieLostCityJane", "ZombieJane" -> new ZombieLostCityJane(alias);
            case "ZombieProspector", "ZombieWestProspector" -> new ZombieProspector(alias);
            case "ZombiePiano", "ZombieWestPiano" -> new ZombiePiano(alias);
            case "ZombieModernNewspaper", "ZombieNewspaper" -> new ZombieNewspaper(alias);
            default -> null;
        };
    }

    private static Zombie createRangedCasterZombie(String alias) {
        return switch (alias) {
            case "ZombieRaDefault" -> new ZombieRa();
            case "ZombieExplorerDefault" -> new ZombieExplorer();
            case "ZombieTombRaiserDefault" -> new ZombieTombRaiser();
            case "ZombieIceAgeHunter" -> new ZombieIceAgeHunter();
            case "ZombieBeachFisherman", "ZombieBeachFishermanDefault" -> new ZombieBeachFisherman();
            case "ZombieBeachOctopus", "ZombieBeachOctopusDefault" -> new ZombieBeachOctopus();
            case "ZombieWizardDefault", "ZombieWizard", "ZombieDarkWizard", "ZombieDarkWizardDefault" -> new ZombieWizard();
            case "ZombieDarkJugglerDefault", "ZombieDarkJuggler", "ZombieDarkJester", "ZombieDarkJesterDefault" -> new ZombieDarkJuggler();
            case "ZombieDarkKing", "ZombieDarkKingDefault" -> new ZombieDarkKing();
            case "ZombieLostCityCrystalSkull", "ZombieCrystalSkull" -> new ZombieCrystalSkull(alias);
            default -> null;
        };
    }

    private static Zombie createZombossAndZombotany(String alias) {
        return switch (alias) {
            case "ZombieZombossMechEgypt" -> new ZombieZombossMechEgypt();
            case "ZombieZombossMechPirate" -> new ZombieZombossMechPirate();
            case "ZombieZombossMechCowboy" -> new ZombieZombossMechCowboy();
            case "ZombieZombossMechDark" -> new ZombieZombossMechDark();
            case "ZombieZombossMechBeach" -> new ZombieZombossMechBeach();
            case "ZombieZombossMechIceAge" -> new ZombieZombossMechIceAge();
            case "ZombotanyPeashooterDefault" -> new ZombotanyPeashooter();
            case "ZombotanyWallnutDefault" -> new ZombotanyWallnut();
            case "ZombotanyJalapenoDefault" -> new ZombotanyJalapeno();
            case "ZombotanySquashDefault" -> new ZombotanySquash();
            default -> null;
        };
    }

    private static ZombieArmor createArmor(ZombieArmor.ArmorType type, int health,
                                           boolean canBeHypnotized, boolean isMetal, boolean isRemovable) {
        return new ZombieArmor(type, health, canBeHypnotized, isMetal, isRemovable);
    }
}
