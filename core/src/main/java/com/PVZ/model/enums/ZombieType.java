package com.PVZ.model.enums;

import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieArmor;
import com.PVZ.model.entity.zombies.types.basic.*;
import com.PVZ.model.entity.zombies.types.heavy_gargantuar.*;
import com.PVZ.model.entity.zombies.types.special_movement.*;
import com.PVZ.model.entity.zombies.types.ranged_caster.*;
import com.PVZ.model.entity.zombies.types.zomboss.*;

import java.util.HashMap;
import java.util.Map;

public enum ZombieType {
      TUTORIAL_DEFAULT("ZombieTutorialDefault") {
            public Zombie create() { return new ZombieTutorial("ZombieTutorialDefault", null); }
      },
      TUTORIAL_ARMOR1("ZombieTutorialArmor1Default") {
            public Zombie create() { return new ZombieTutorial("ZombieTutorialArmor1Default",
                  new ZombieArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true)); }
      },
      TUTORIAL_ARMOR2("ZombieTutorialArmor2Default") {
            public Zombie create() { return new ZombieTutorial("ZombieTutorialArmor2Default",
                  new ZombieArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true)); }
      },
      TUTORIAL_ARMOR4("ZombieTutorialArmor4Default") {
            public Zombie create() { return new ZombieTutorial("ZombieTutorialArmor4Default",
                  new ZombieArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true)); }
      },
      TUTORIAL_FLAG("ZombieTutorialFlagDefault") {
            public Zombie create() { return new ZombieTutorial("ZombieTutorialFlagDefault", null); }
      },
      MUMMY_DEFAULT("ZombieMummyDefault") {
            public Zombie create() { return new ZombieMummy("ZombieMummyDefault", null); }
      },
      MUMMY_ARMOR1("ZombieMummyArmor1Default") {
            public Zombie create() { return new ZombieMummy("ZombieMummyArmor1Default",
                  new ZombieArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true)); }
      },
      MUMMY_ARMOR2("ZombieMummyArmor2Default") {
            public Zombie create() { return new ZombieMummy("ZombieMummyArmor2Default",
                  new ZombieArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true)); }
      },
      MUMMY_ARMOR4("ZombieMummyArmor4Default") {
            public Zombie create() { return new ZombieMummy("ZombieMummyArmor4Default",
                  new ZombieArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true)); }
      },
      ICEAGE_DEFAULT("ZombieIceageDefault") {
            public Zombie create() { return new ZombieIceage("ZombieIceageDefault", null); }
      },
      ICEAGE_ARMOR1("ZombieIceageArmor1Default") {
            public Zombie create() { return new ZombieIceage("ZombieIceageArmor1Default",
                  new ZombieArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true)); }
      },
      ICEAGE_ARMOR2("ZombieIceageArmor2Default") {
            public Zombie create() { return new ZombieIceage("ZombieIceageArmor2Default",
                  new ZombieArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true)); }
      },
      ICEAGE_ARMOR3("ZombieIceageArmor3Default") {
            public Zombie create() { return new ZombieIceage("ZombieIceageArmor3Default",
                  new ZombieArmor(ZombieArmor.ArmorType.ICE_BLOCK, 800, false, false, true)); }
      },
      BEACH_DEFAULT("ZombieBeachDefault") {
            public Zombie create() { return new ZombieBeach("ZombieBeachDefault", null); }
      },
      BEACH_ARMOR1("ZombieBeachArmor1Default") {
            public Zombie create() { return new ZombieBeach("ZombieBeachArmor1Default",
                  new ZombieArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true)); }
      },
      BEACH_ARMOR2("ZombieBeachArmor2Default") {
            public Zombie create() { return new ZombieBeach("ZombieBeachArmor2Default",
                  new ZombieArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true)); }
      },
      DARK_DEFAULT("ZombieDarkDefault") {
            public Zombie create() { return new ZombieDark("ZombieDarkDefault", null); }
      },
      DARK_ARMOR1("ZombieDarkArmor1Default") {
            public Zombie create() { return new ZombieDark("ZombieDarkArmor1Default",
                  new ZombieArmor(ZombieArmor.ArmorType.CONE, 370, true, false, true)); }
      },
      DARK_ARMOR2("ZombieDarkArmor2Default") {
            public Zombie create() { return new ZombieDark("ZombieDarkArmor2Default",
                  new ZombieArmor(ZombieArmor.ArmorType.BUCKET, 1100, true, true, true)); }
      },
      DARK_ARMOR3("ZombieDarkArmor3Default") {
            public Zombie create() { return new ZombieDark("ZombieDarkArmor3Default",
                  new ZombieArmor(ZombieArmor.ArmorType.SHOULDER_ARMOR, 1600, false, false, false),
                  new ZombieArmor(ZombieArmor.ArmorType.CROWN, 1600, true, true, true)); }
      },
      DARK_ARMOR4("ZombieDarkArmor4Default") {
            public Zombie create() { return new ZombieDark("ZombieDarkArmor4Default",
                  new ZombieArmor(ZombieArmor.ArmorType.BRICK, 2200, true, false, true)); }
      },
      PHARAOH("ZombiePharaohDefault") {
            public Zombie create() { return new ZombiePharaoh(); }
      },
      CAMEL("ZombieCamelDefault") {
            public Zombie create() { return new ZombieCamel(); }
      },
      GARGANTUAR_BASIC("ZombieGargantuarBasic") {
            public Zombie create() { return new ZombieGargantuar("ZombieGargantuarBasic", ZombieGargantuar.Theme.BASIC); }
      },
      GARGANTUAR_EGYPT("ZombieEgyptGargantuar") {
            public Zombie create() { return new ZombieGargantuar("ZombieEgyptGargantuar", ZombieGargantuar.Theme.EGYPT); }
      },
      GARGANTUAR_ICEAGE("ZombieIceAgeGargantuar") {
            public Zombie create() { return new ZombieGargantuar("ZombieIceAgeGargantuar", ZombieGargantuar.Theme.ICEAGE); }
      },
      GARGANTUAR_BEACH("ZombieBeachGargantuar") {
            public Zombie create() { return new ZombieGargantuar("ZombieBeachGargantuar", ZombieGargantuar.Theme.BEACH); }
      },
      GARGANTUAR_DARK("ZombieDarkGargantuar") {
            public Zombie create() { return new ZombieGargantuar("ZombieDarkGargantuar", ZombieGargantuar.Theme.DARK); }
      },
      IMP_TUTORIAL("ZombieTutorialImpDefault") {
            public Zombie create() { return new ZombieImp("ZombieTutorialImpDefault", ZombieImp.Theme.BASIC); }
      },
      IMP_EGYPT("ZombieEgyptImpDefault") {
            public Zombie create() { return new ZombieImp("ZombieEgyptImpDefault", ZombieImp.Theme.EGYPT); }
      },
      IMP_ICEAGE("ZombieIceageImpDefault") {
            public Zombie create() { return new ZombieImp("ZombieIceageImpDefault", ZombieImp.Theme.ICEAGE); }
      },
      IMP_BEACH("ZombieBeachImpDefault") {
            public Zombie create() { return new ZombieImp("ZombieBeachImpDefault", ZombieImp.Theme.BEACH); }
      },
      IMP_DARK("ZombieDarkImpDefault") {
            public Zombie create() { return new ZombieImp("ZombieDarkImpDefault", ZombieImp.Theme.DARK); }
      },
      TROGLOBITE("ZombieIceAgeTroglobite") {
            public Zombie create() { return new ZombieIceAgeTroglobite(); }
      },
      DODO("ZombieIceAgeDodo") {
            public Zombie create() { return new ZombieIceAgeDodo(); }
      },
      WEASEL_HOARDER("ZombieWeaselHoarderDefault") {
            public Zombie create() { return new ZombieWeaselHoarder(); }
      },
      WEASEL("ZombieWeaselDefault") {
            public Zombie create() { return new ZombieWeasel(); }
      },
      BEACH_SNORKEL("ZombieBeachSnorkel") {
            public Zombie create() { return new ZombieBeachSnorkel(); }
      },
      BEACH_SURFER("ZombieBeachSurfer") {
            public Zombie create() { return new ZombieBeachSurfer(); }
      },
      BEACH_FAST_SWIMMER("ZombieBeachFastSwimmer") {
            public Zombie create() { return new ZombieBeachFastSwimmer(); }
      },
      RA("ZombieRaDefault") {
            public Zombie create() { return new ZombieRa(); }
      },
      EXPLORER("ZombieExplorerDefault") {
            public Zombie create() { return new ZombieExplorer(); }
      },
      TOMB_RAISER("ZombieTombRaiserDefault") {
            public Zombie create() { return new ZombieTombRaiser(); }
      },
      ICEAGE_HUNTER("ZombieIceAgeHunter") {
            public Zombie create() { return new ZombieIceAgeHunter(); }
      },
      BEACH_FISHERMAN("ZombieBeachFisherman") {
            public Zombie create() { return new ZombieBeachFisherman(); }
      },
      BEACH_OCTOPUS("ZombieBeachOctopus") {
            public Zombie create() { return new ZombieBeachOctopus(); }
      },
      WIZARD("ZombieWizardDefault") {
            public Zombie create() { return new ZombieWizard(); }
      },
      DARK_JUGGLER("ZombieDarkJugglerDefault") {
            public Zombie create() { return new ZombieDarkJuggler(); }
      },
      DARK_KING("ZombieDarkKing") {
            public Zombie create() { return new ZombieDarkKing(); }
      },
      ZOMBOSS_EGYPT("ZombieZombossMechEgypt") {
            public Zombie create() { return new ZombieZombossMechEgypt(); }
      },
      ZOMBOSS_PIRATE("ZombieZombossMechPirate") {
            public Zombie create() { return new ZombieZombossMechPirate(); }
      },
      ZOMBOSS_COWBOY("ZombieZombossMechCowboy") {
            public Zombie create() { return new ZombieZombossMechCowboy(); }
      },
      ZOMBOSS_DARK("ZombieZombossMechDark") {
            public Zombie create() { return new ZombieZombossMechDark(); }
      };

      public final String alias;

      ZombieType(String alias) {
            this.alias = alias;
      }

      public abstract Zombie create();

      private static final Map<String, ZombieType> BY_ALIAS = new HashMap<>();

      static {
            for (ZombieType zt : values())
                  BY_ALIAS.put(zt.alias, zt);
      }

      public static ZombieType fromAlias(String alias) {
            ZombieType zt = BY_ALIAS.get(alias);
            if (zt == null) throw new IllegalArgumentException("Unknown alias: " + alias);
            return zt;
      }
}
