package model.entity.zombies.factory;

import model.entity.zombies.base.Zombie;
import model.entity.zombies.base.ZombieArmor;
import model.entity.zombies.types.basic.*;
import model.entity.zombies.types.heavy_gargantuar.ZombieGargantuar;
import model.entity.zombies.types.heavy_gargantuar.ZombieImp;
import model.entity.zombies.types.ranged_caster.*;
import model.entity.zombies.types.special_movement.*;
import model.entity.zombies.types.zomboss.ZombieZombossMechCowboy;
import model.entity.zombies.types.zomboss.ZombieZombossMechDark;
import model.entity.zombies.types.zomboss.ZombieZombossMechEgypt;
import model.entity.zombies.types.zomboss.ZombieZombossMechPirate;

public class ZombieFactory {

    public static Zombie createZombie(String alias) {
        return switch (alias) {
            case "ZombieTutorialDefault"       -> new ZombieTutorial("ZombieTutorialDefault", null);
            case "ZombieTutorialArmor1Default"  -> new ZombieTutorial("ZombieTutorialArmor1Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieTutorialArmor2Default"  -> new ZombieTutorial("ZombieTutorialArmor2Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieTutorialArmor4Default"  -> new ZombieTutorial("ZombieTutorialArmor4Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true));
            case "ZombieTutorialFlagDefault"    -> new ZombieTutorial("ZombieTutorialFlagDefault", null);
            case "ZombieMummyDefault"           -> new ZombieMummy("ZombieMummyDefault", null);
            case "ZombieMummyArmor1Default"     -> new ZombieMummy("ZombieMummyArmor1Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieMummyArmor2Default"     -> new ZombieMummy("ZombieMummyArmor2Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieMummyArmor4Default"     -> new ZombieMummy("ZombieMummyArmor4Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true));
            case "ZombieIceageDefault"          -> new ZombieIceage("ZombieIceageDefault", null);
            case "ZombieIceageArmor1Default"    -> new ZombieIceage("ZombieIceageArmor1Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieIceageArmor2Default"    -> new ZombieIceage("ZombieIceageArmor2Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieIceageArmor3Default"    -> new ZombieIceage("ZombieIceageArmor3Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.ICE_BLOCK, 800, false, false, true));
            case "ZombieBeachDefault"           -> new ZombieBeach("ZombieBeachDefault", null);
            case "ZombieBeachArmor1Default"     -> new ZombieBeach("ZombieBeachArmor1Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieBeachArmor2Default"     -> new ZombieBeach("ZombieBeachArmor2Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieDarkDefault"            -> new ZombieDark("ZombieDarkDefault", null);
            case "ZombieDarkArmor1Default"      -> new ZombieDark("ZombieDarkArmor1Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true));
            case "ZombieDarkArmor2Default"      -> new ZombieDark("ZombieDarkArmor2Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true));
            case "ZombieDarkArmor3Default"      -> new ZombieDark("ZombieDarkArmor3Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.SHOULDER_ARMOR, 1600, false, false, false),
                                                    new ZombieArmor(ZombieArmor.ArmorType.CROWN, 1600, true, true, true));
            case "ZombieDarkArmor4Default"      -> new ZombieDark("ZombieDarkArmor4Default",
                                                    new ZombieArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true));
            case "ZombiePharaohDefault"         -> new ZombiePharaoh();
            case "ZombieCamelDefault"           -> new ZombieCamel();
            case "ZombieGargantuarBasic"        -> new ZombieGargantuar("ZombieGargantuarBasic", ZombieGargantuar.Theme.BASIC);
            case "ZombieEgyptGargantuar"        -> new ZombieGargantuar("ZombieEgyptGargantuar", ZombieGargantuar.Theme.EGYPT);
            case "ZombieIceAgeGargantuar"       -> new ZombieGargantuar("ZombieIceAgeGargantuar", ZombieGargantuar.Theme.ICEAGE);
            case "ZombieBeachGargantuar"        -> new ZombieGargantuar("ZombieBeachGargantuar", ZombieGargantuar.Theme.BEACH);
            case "ZombieDarkGargantuar"         -> new ZombieGargantuar("ZombieDarkGargantuar", ZombieGargantuar.Theme.DARK);
            case "ZombieTutorialImpDefault"     -> new ZombieImp("ZombieTutorialImpDefault", ZombieImp.Theme.BASIC);
            case "ZombieEgyptImpDefault"        -> new ZombieImp("ZombieEgyptImpDefault", ZombieImp.Theme.EGYPT);
            case "ZombieIceageImpDefault"       -> new ZombieImp("ZombieIceageImpDefault", ZombieImp.Theme.ICEAGE);
            case "ZombieBeachImpDefault"        -> new ZombieImp("ZombieBeachImpDefault", ZombieImp.Theme.BEACH);
            case "ZombieDarkImpDefault"         -> new ZombieImp("ZombieDarkImpDefault", ZombieImp.Theme.DARK);
            case "ZombieIceAgeTroglobite"       -> new ZombieIceAgeTroglobite();
            case "ZombieIceAgeDodo"             -> new ZombieIceAgeDodo();
            case "ZombieWeaselHoarderDefault"   -> new ZombieWeaselHoarder();
            case "ZombieWeaselDefault"          -> new ZombieWeasel();
            case "ZombieBeachSnorkel"           -> new ZombieBeachSnorkel();
            case "ZombieBeachSurfer"            -> new ZombieBeachSurfer();
            case "ZombieBeachFastSwimmer"       -> new ZombieBeachFastSwimmer();
            case "ZombieRaDefault"              -> new ZombieRa();
            case "ZombieExplorerDefault"        -> new ZombieExplorer();
            case "ZombieTombRaiserDefault"      -> new ZombieTombRaiser();
            case "ZombieIceAgeHunter"           -> new ZombieIceAgeHunter();
            case "ZombieBeachFisherman"         -> new ZombieBeachFisherman();
            case "ZombieBeachOctopus"           -> new ZombieBeachOctopus();
            case "ZombieWizardDefault"          -> new ZombieWizard();
            case "ZombieDarkJugglerDefault"     -> new ZombieDarkJuggler();
            case "ZombieDarkKing"              -> new ZombieDarkKing();
            case "ZombieZombossMechEgypt"       -> new ZombieZombossMechEgypt();
            case "ZombieZombossMechPirate"      -> new ZombieZombossMechPirate();
            case "ZombieZombossMechCowboy"      -> new ZombieZombossMechCowboy();
            case "ZombieZombossMechDark"        -> new ZombieZombossMechDark();
            default -> throw new IllegalArgumentException("Unknown zombie alias: " + alias);
        };
    }
}
