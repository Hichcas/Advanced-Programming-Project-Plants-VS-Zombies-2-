package com.PVZ.model.entity.zombies.base;

import java.util.HashMap;
import java.util.Map;

public class ZombieTexturePaths {
    private static final Map<String, String> PATHS = new HashMap<>();

    static {
        PATHS.put("ZombieTutorialDefault", "Zombies/Zombie.png");
        PATHS.put("ZombieTutorialArmor1Default", "Zombies/ConeheadZombie.png");
        PATHS.put("ZombieTutorialArmor2Default", "Zombies/Buckethead.png");
        PATHS.put("ZombieTutorialArmor4Default", "Zombies/BrickHead.png");
        PATHS.put("ZombieTutorialFlagDefault", "Zombies/FlagZombie.png");
        PATHS.put("ZombieMummyDefault", "Zombies/Mummy.png");
        PATHS.put("ZombieMummyArmor1Default", "Zombies/ConeheadZombie.png");
        PATHS.put("ZombieMummyArmor2Default", "Zombies/Buckethead.png");
        PATHS.put("ZombieMummyArmor4Default", "Zombies/BrickHead.png");
        PATHS.put("ZombieIceageDefault", "Zombies/Zombie.png");
        PATHS.put("ZombieIceageArmor1Default", "Zombies/ConeheadZombie.png");
        PATHS.put("ZombieIceageArmor2Default", "Zombies/Buckethead.png");
        PATHS.put("ZombieIceageArmor3Default", "Zombies/Turquoise.png");
        PATHS.put("ZombieBeachDefault", "Zombies/Beach.png");
        PATHS.put("ZombieBeachArmor1Default", "Zombies/ConeheadZombie.png");
        PATHS.put("ZombieBeachArmor2Default", "Zombies/Buckethead.png");
        PATHS.put("ZombieDarkDefault", "Zombies/Zombie.png");
        PATHS.put("ZombieDarkArmor1Default", "Zombies/ConeheadZombie.png");
        PATHS.put("ZombieDarkArmor2Default", "Zombies/Buckethead.png");
        PATHS.put("ZombieDarkArmor3Default", "Zombies/Knight.png");
        PATHS.put("ZombieDarkArmor4Default", "Zombies/BrickHead.png");
        PATHS.put("ZombiePharaohDefault", "Zombies/Pharaoh.png");
        PATHS.put("ZombieCamelDefault", "Zombies/Camel.png");

        for (String a : new String[]{"ZombieGargantuarBasic", "ZombieEgyptGargantuar",
            "ZombieIceAgeGargantuar", "ZombieBeachGargantuar", "ZombieDarkGargantuar"})
            PATHS.put(a, "Zombies/Gargantuar.png");

        PATHS.put("ZombieTutorialImpDefault", "Zombies/Imp.png");
        PATHS.put("ZombieEgyptImpDefault", "Zombies/Imp.png");
        PATHS.put("ZombieIceageImpDefault", "Zombies/ImpDragon.png");
        PATHS.put("ZombieBeachImpDefault", "Zombies/Imp.png");
        PATHS.put("ZombieDarkImpDefault", "Zombies/ImpDragon.png");
        PATHS.put("ZombieIceAgeTroglobite", "Zombies/Troglobite.png");
        PATHS.put("ZombieIceAgeDodo", "Zombies/Dodo.png");
        PATHS.put("ZombieIceAgeHunter", "Zombies/Hunter.png");
        PATHS.put("ZombieBeachSnorkel", "Zombies/Snorkel.png");
        PATHS.put("ZombieBeachFisherman", "Zombies/Fisherman.png");
        PATHS.put("ZombieBeachOctopus", "Zombies/Octopus.png");
        PATHS.put("ZombieBeachSurfer", "Zombies/Surfer.png");
        PATHS.put("ZombieBeachFastSwimmer", "Zombies/FastSwimmer.png");
        PATHS.put("ZombieWeaselHoarderDefault", "Zombies/WeaselHoarder.png");
        PATHS.put("ZombieWeaselDefault", "Zombies/Weasel.png");
        PATHS.put("ZombieRaDefault", "Zombies/Ra.png");
        PATHS.put("ZombieExplorerDefault", "Zombies/Explorer.png");
        PATHS.put("ZombieTombRaiserDefault", "Zombies/TombRaiser.png");
        PATHS.put("ZombieWizardDefault", "Zombies/Wizard.png");
        PATHS.put("ZombieDarkJugglerDefault", "Zombies/Juggler.png");
        PATHS.put("ZombieDarkKing", "Zombies/King.png");

        for (String a : new String[]{"ZombieZombossMechEgypt", "ZombieZombossMechPirate",
            "ZombieZombossMechCowboy", "ZombieZombossMechDark"})
            PATHS.put(a, "Zombies/Zomboss.png");

        PATHS.put("DEFAULT", "Zombies/Zombie.png");
    }

    public static String getPath(String alias) {
        return PATHS.getOrDefault(alias, PATHS.get("DEFAULT"));
    }
}
