package com.PVZ.model.enums;

import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieArmor;
import com.PVZ.model.entity.zombies.types.basic.*;
import com.PVZ.model.entity.zombies.types.heavy_gargantuar.*;
import com.PVZ.model.entity.zombies.types.special_movement.*;
import com.PVZ.model.entity.zombies.types.ranged_caster.*;
import com.PVZ.model.entity.zombies.types.zomboss.*;
import com.PVZ.model.entity.zombies.types.zombotany.*;

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
            public Zombie create() { return new ZombieGargantuar("ZombieGargantuarBasic", ZombieGargantuar.Theme
                    .BASIC); }
      },
      GARGANTUAR_EGYPT("ZombieEgyptGargantuar") {
            public Zombie create() { return new ZombieGargantuar("ZombieEgyptGargantuar", ZombieGargantuar.Theme
                    .EGYPT); }
      },
      GARGANTUAR_ICEAGE("ZombieIceAgeGargantuar") {
            public Zombie create() { return new ZombieGargantuar("ZombieIceAgeGargantuar", ZombieGargantuar.Theme
                    .ICEAGE); }
      },
      GARGANTUAR_BEACH("ZombieBeachGargantuar") {
            public Zombie create() { return new ZombieGargantuar("ZombieBeachGargantuar", ZombieGargantuar.Theme
                    .BEACH); }
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
      },
      ZOMBOTANY_PEASHOOTER("ZombotanyPeashooterDefault") {
            public Zombie create() { return new ZombotanyPeashooter(); }
      },
      ZOMBOTANY_WALLNUT("ZombotanyWallnutDefault") {
            public Zombie create() { return new ZombotanyWallnut(); }
      },
      ZOMBOTANY_JALAPENO("ZombotanyJalapenoDefault") {
            public Zombie create() { return new ZombotanyJalapeno(); }
      },
      ZOMBOTANY_SQUASH("ZombotanySquashDefault") {
            public Zombie create() { return new ZombotanySquash(); }
      },
      ALLSTAR("ZombieModernAllStar") {
            public Zombie create() { return new ZombieAllStar(); }
      },
      FOOTBALL("ZombieFootball") {
            public Zombie create() { return new ZombieAllStar("ZombieFootball"); }
      },
      IMP_DRAGON("ZombieDarkImpDragon") {
            public Zombie create() { return new ZombieImp("ZombieDarkImpDragon", ZombieImp.Theme.DARK); }
      },
      ARCADE("Zombie80sArcade") {
            public Zombie create() { return new ZombieArcade(); }
      },
      LOSTCITY_JANE("ZombieLostCityJane") {
            public Zombie create() { return new ZombieLostCityJane(); }
      },
      CRYSTAL_SKULL("ZombieLostCityCrystalSkull") {
            public Zombie create() { return new ZombieCrystalSkull(); }
      },
      PROSPECTOR("ZombieProspector") {
            public Zombie create() { return new ZombieProspector(); }
      },
      PIANO("ZombiePiano") {
            public Zombie create() { return new ZombiePiano(); }
      },
      NEWSPAPER("ZombieModernNewspaper") {
            public Zombie create() { return new ZombieNewspaper(); }
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
            if (alias == null) return TUTORIAL_DEFAULT;
            ZombieType zt = BY_ALIAS.get(alias);
            if (zt != null) return zt;

            // Common aliases and alternate names
            if ("ZombieBeachSnorkeler".equals(alias) || "ZombieBeachSnorkelDefault".equals(alias)) return BEACH_SNORKEL;
            if ("ZombieBeachSurferDefault".equals(alias)) return BEACH_SURFER;
            if ("ZombieBeachOctopusDefault".equals(alias)) return BEACH_OCTOPUS;
            if ("ZombieBeachFishermanDefault".equals(alias)) return BEACH_FISHERMAN;
            if ("ZombieBeachImp".equals(alias)) return IMP_BEACH;
            if ("ZombieBeachFlag".equals(alias)) return BEACH_DEFAULT;
            if ("ZombieDarkJester".equals(alias) || "ZombieDarkJuggler".equals(alias)
                || "ZombieDarkJugglerDefault".equals(alias)) return DARK_JUGGLER;
            if ("ZombieWizard".equals(alias) || "ZombieDarkWizard".equals(alias)
                || "ZombieWizardDefault".equals(alias)) return WIZARD;
            if ("ZombieDarkKing".equals(alias) ||
                "ZombieDarkKingDefault".equals(alias)) return DARK_KING;
            if ("ZombieDarkImpDragon".equals(alias) || "ZombieDarkImpDragonDefault".equals(alias)
                || "ZombieImpDragon".equals(alias)) return IMP_DRAGON;
            if ("ZombieModernAllStar".equals(alias) || "ZombieAllStar".equals(alias) ||
                "ZombieFootball".equals(alias)) return ALLSTAR;
            if ("ZombieArcade".equals(alias) || "Zombie80sArcade".equals(alias) ||
                "ZombieArcadeDefault".equals(alias)) return ARCADE;
            if ("ZombieLostCityJane".equals(alias) || "ZombieLostCityJaneDefault".equals(alias)
                || "ZombieJane".equals(alias)) return LOSTCITY_JANE;
            if ("ZombieCrystalSkull".equals(alias) || "ZombieLostCityCrystalSkull".equals(alias)
                || "ZombieCrystalSkullDefault".equals(alias)) return CRYSTAL_SKULL;
            if ("ZombieProspector".equals(alias) || "ZombieProspectorDefault".equals(alias)
                || "ZombieWestProspector".equals(alias)) return PROSPECTOR;
            if ("ZombiePiano".equals(alias) || "ZombiePianoDefault".equals(alias)
                || "ZombieWestPiano".equals(alias)) return PIANO;
            if ("ZombieNewspaper".equals(alias) || "ZombieModernNewspaper".equals(alias)
                || "ZombieNewspaperDefault".equals(alias)) return NEWSPAPER;

            if (alias.endsWith("Default")) {
                zt = BY_ALIAS.get(alias.substring(0, alias.length() - 7));
                if (zt != null) return zt;
            }
            if (!alias.endsWith("Default")) {
                zt = BY_ALIAS.get(alias + "Default");
                if (zt != null) return zt;
            }

            System.err.println("Warning: Unknown zombie alias '" + alias + "', falling back to TUTORIAL_DEFAULT");
            return TUTORIAL_DEFAULT;
      }
}
