package com.PVZ.model.entity.zombies.base;

import java.util.HashMap;
import java.util.Map;

public class ZombieTexturePaths {
    private static final Map<String, String> PAM_PATHS = new HashMap<>();

    static {
        // PAM Animation File Mappings with resolution prefix
        PAM_PATHS.put("ZombieTutorialDefault", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM");
        PAM_PATHS.put("ZombieTutorialArmor1Default", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
        PAM_PATHS.put("ZombieTutorialArmor2Default", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
        PAM_PATHS.put("ZombieTutorialArmor4Default", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
        PAM_PATHS.put("ZombieTutorialFlagDefault", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL_FLAG/ZOMBIE_TUTORIAL_FLAG.PAM");
        PAM_PATHS.put("ZombieMummyDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
        PAM_PATHS.put("ZombieMummyArmor1Default", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
        PAM_PATHS.put("ZombieMummyArmor2Default", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
        PAM_PATHS.put("ZombieMummyArmor4Default", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
        PAM_PATHS.put("ZombieCamelDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_CAMEL/ZOMBIE_EGYPT_CAMEL.PAM");
        PAM_PATHS.put("ZombieRaDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_RA/ZOMBIE_EGYPT_RA.PAM");
        PAM_PATHS.put("ZombiePharaohDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_SARCOPHAGUS/ZOMBIE_EGYPT_SARCOPHAGUS.PAM");
        PAM_PATHS.put("ZombieExplorerDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EXPLORER/ZOMBIE_EXPLORER.PAM");
        PAM_PATHS.put("ZombieTombRaiserDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_TOMBRAISER/ZOMBIE_EGYPT_TOMBRAISER.PAM");
        PAM_PATHS.put("ZombieEgyptGargantuar", "768/INITIAL/ZOMBIE/EGYPT_GARGANTUAR/EGYPT_GARGANTUAR.PAM");
        PAM_PATHS.put("ZombieGargantuarBasic", "768/INITIAL/ZOMBIE/TUTORIAL_GARGANTUAR/TUTORIAL_GARGANTUAR.PAM");
        PAM_PATHS.put("ZombieEgyptImpDefault", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_IMP/ZOMBIE_EGYPT_IMP.PAM");
        PAM_PATHS.put("ZombieTutorialImpDefault", "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL_IMP/ZOMBIE_TUTORIAL_IMP.PAM");
        PAM_PATHS.put("ZombieZombossMechEgypt", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_ZOMBOSS/ZOMBIE_EGYPT_ZOMBOSS.PAM");
        PAM_PATHS.put("DEFAULT", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM");
    }

    public static String getPath(String alias) {
        return getPamPath(alias);
    }

    public static String getPamPath(String alias) {
        return PAM_PATHS.getOrDefault(alias, PAM_PATHS.get("DEFAULT"));
    }
}
