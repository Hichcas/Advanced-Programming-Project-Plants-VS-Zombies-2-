package com.PVZ.model.entity;

import java.util.HashMap;
import java.util.Map;


public final class PlantTexturePaths {
    private static final Map<String, String> PATHS = new HashMap<>();
    private static final String BASE = "Plants/";
    private static final String FALLBACK = BASE + "Unknown.png";

    static {
        PATHS.put("SUNFLOWER", BASE + "Sunflower.png");
        PATHS.put("TWIN_SUNFLOWER", BASE + "TwinSunflower.png");
        PATHS.put("SUN_SHROOM", BASE + "SunShroom.png");
        PATHS.put("PRIMAL_SUNFLOWER", BASE + "PrimalSunflower.png");
        PATHS.put("GOLD_BLOOM", BASE + "GoldBloom.png");
        PATHS.put("PEASHOOTER", BASE + "Peashooter.png");
        PATHS.put("REPEATER", BASE + "Repeater.png");
        PATHS.put("THREEPEATER", BASE + "Threepeater.png");
        PATHS.put("SNOW_PEA", BASE + "SnowPea.png");
        PATHS.put("ROTOBAGA", BASE + "Rotobaga.png");
        PATHS.put("PEA_POD", BASE + "PeaPod.png");
        PATHS.put("SPLIT_PEA", BASE + "SplitPea.png");
        PATHS.put("CITRON", BASE + "Citron.png");
        PATHS.put("CAULIPOWER", BASE + "Caulipower.png");
        PATHS.put("ELECTRIC_BLUEBERRY", BASE + "ElectricBlueberry.png");
        PATHS.put("BOWLING_BULB", BASE + "BowlingBulb.png");
        PATHS.put("CACTUS", BASE + "Cactus.png");
        PATHS.put("FIRE_PEASHOOTER", BASE + "FirePeashooter.png");
        PATHS.put("STARFRUIT", BASE + "Starfruit.png");
        PATHS.put("GOO_PEASHOOTER", BASE + "GooPeashooter.png");
        PATHS.put("MEGA_GATLING_PEA", BASE + "MegaGatlingPea.png");
        PATHS.put("SEA_SHROOM", BASE + "SeaShroom.png");
        PATHS.put("PUFF_SHROOM", BASE + "PuffShroom.png");
        PATHS.put("FUME_SHROOM", BASE + "FumeShroom.png");
        PATHS.put("CABBAGE_PULT", BASE + "CabbagePult.png");
        PATHS.put("KERNEL_PULT", BASE + "KernelPult.png");
        PATHS.put("MELON_PULT", BASE + "MelonPult.png");
        PATHS.put("WINTER_MELON", BASE + "WinterMelon.png");
        PATHS.put("PEPPER_PULT", BASE + "PepperPult.png");
        PATHS.put("POTATO_MINE", BASE + "PotatoMine.png");
        PATHS.put("PRIMAL_POTATO_MINE", BASE + "PrimalPotatoMine.png");
        PATHS.put("CHERRY_BOMB", BASE + "CherryBomb.png");
        PATHS.put("SQUASH", BASE + "Squash.png");
        PATHS.put("GRAPESHOT", BASE + "Grapeshot.png");
        PATHS.put("JALAPENO", BASE + "Jalapeno.png");
        PATHS.put("DOOM_SHROOM", BASE + "DoomShroom.png");
        PATHS.put("TANGLE_KELP", BASE + "TangleKelp.png");
        PATHS.put("ICEBERG_LETTUCE", BASE + "IcebergLettuce.png");
        PATHS.put("BONK_CHOY", BASE + "BonkChoy.png");
        PATHS.put("PHAT_BEET", BASE + "PhatBeet.png");
        PATHS.put("CHOMPER", BASE + "Chomper.png");
        PATHS.put("WASABI_WHIP", BASE + "WasabiWhip.png");
        PATHS.put("KIWIBEAST", BASE + "Kiwibeast.png");
        PATHS.put("WALL_NUT", BASE + "WallNut.png");
        PATHS.put("TALL_NUT", BASE + "TallNut.png");
        PATHS.put("ENDURIAN", BASE + "Endurian.png");
        PATHS.put("GARLIC", BASE + "Garlic.png");
        PATHS.put("SWEET_POTATO", BASE + "SweetPotato.png");
        PATHS.put("EXPLODE_O_NUT", BASE + "ExplodeONut.png");
        PATHS.put("PUMPKIN", BASE + "Pumpkin.png");
        PATHS.put("SUN_BEAN", BASE + "SunBean.png");
        PATHS.put("TORCHWOOD", BASE + "Torchwood.png");
        PATHS.put("MAGNET_SHROOM", BASE + "MagnetShroom.png");
        PATHS.put("HYPNO_SHROOM", BASE + "HypnoShroom.png");
        PATHS.put("CAT_TAIL", BASE + "CatTail.png");
        PATHS.put("IMITATER", BASE + "Imitater.png");
        PATHS.put("ICE_SHROOM", BASE + "IceShroom.png");
        PATHS.put("LILY_PAD", BASE + "LilyPad.png");
        PATHS.put("HOT_POTATO", BASE + "HotPotato.png");
        PATHS.put("GRAVE_BUSTER", BASE + "GraveBuster.png");
        PATHS.put("ENLIGHTEN_MINT", BASE + "EnlightenMint.png");
        PATHS.put("APPEASE_MINT", BASE + "AppeaseMint.png");
        PATHS.put("ARMA_MINT", BASE + "ArmaMint.png");
        PATHS.put("BOMBARD_MINT", BASE + "BombardMint.png");
        PATHS.put("ENFORCE_MINT", BASE + "EnforceMint.png");
        PATHS.put("REINFORCE_MINT", BASE + "ReinforceMint.png");
        PATHS.put("ENCHANT_MINT", BASE + "EnchantMint.png");
        PATHS.put("PIERCE_MINT", BASE + "PierceMint.png");
        PATHS.put("CAT_TAIL_MINT", BASE + "CattailMint.png");
    }

    private static final Map<String, String> PAM_PATHS = new HashMap<>();
    private static final String PAM_INITIAL = "768/INITIAL/PLANT/";
    private static final String PAM_FULL = "768/FULL/PLANT/";

    private static void pamInitial(String plantType, String folder) {
        PAM_PATHS.put(plantType, PAM_INITIAL + folder + "/" + folder + ".PAM");
    }

    private static void pamFull(String plantType, String folder) {
        PAM_PATHS.put(plantType, PAM_FULL + folder + "/" + folder + ".PAM");
    }

    static {
        pamInitial("SUNFLOWER", "SUNFLOWER");
        pamInitial("TWIN_SUNFLOWER", "SUNFLOWER_TWIN");
        pamInitial("GOLD_BLOOM", "GOLDBLOOM");
        pamInitial("PEASHOOTER", "PEASHOOTER");
        pamInitial("REPEATER", "REPEATER");
        pamInitial("THREEPEATER", "THREEPEATER");
        pamInitial("SNOW_PEA", "SNOWPEA");
        pamInitial("CAULIPOWER", "CAULIPOWER");
        pamInitial("ELECTRIC_BLUEBERRY", "ELECTRICBLUEBERRY");
        pamInitial("CACTUS", "CACTUS");
        pamInitial("FIRE_PEASHOOTER", "FIREPEASHOOTER");
        pamInitial("STARFRUIT", "STARFRUIT");
        pamInitial("GOO_PEASHOOTER", "GOOPEASHOOTER");
        pamInitial("MEGA_GATLING_PEA", "MEGAGATLING");
        pamInitial("PUFF_SHROOM", "PUFFSHROOM");
        pamInitial("FUME_SHROOM", "FUMESHROOM");
        pamInitial("CABBAGE_PULT", "CABBAGEPULT");
        pamInitial("KERNEL_PULT", "KERNALPULT"); // note: game's own folder is spelled "KERNAL"
        pamInitial("MELON_PULT", "MELONPULT");
        pamInitial("POTATO_MINE", "POTATOMINE");
        pamInitial("SQUASH", "SQUASH");
        pamInitial("GRAPESHOT", "GRAPESHOT");
        pamInitial("JALAPENO", "JALAPENO");
        pamInitial("ICEBERG_LETTUCE", "ICEBURG"); // folder is "ICEBURG"
        pamInitial("CHOMPER", "CHOMPER");
        pamInitial("WASABI_WHIP", "WASABIWHIP");
        pamInitial("KIWIBEAST", "KIWIBEAST");
        pamInitial("WALL_NUT", "WALLNUT");
        pamInitial("SWEET_POTATO", "SWEETPOTATO");
        pamInitial("EXPLODE_O_NUT", "EXPLODEONUT");
        pamInitial("PUMPKIN", "PUMPKIN");
        pamInitial("TORCHWOOD", "TORCHWOOD");
        pamInitial("HYPNO_SHROOM", "HYPNOSHROOM");
        pamInitial("IMITATER", "IMITATER");
        pamInitial("GRAVE_BUSTER", "GRAVEBUSTER");
        pamInitial("BONK_CHOY", "BONKCHOY");
        pamFull("SUN_SHROOM", "SUNSHROOM");
        pamFull("PRIMAL_SUNFLOWER", "PRIMAL_SUNFLOWER");
        pamFull("ROTOBAGA", "ROTORUTABAGA");
        pamFull("PEA_POD", "PEAPOD");
        pamFull("SPLIT_PEA", "SPLITPEA");
        pamFull("CITRON", "CITRON");
        pamFull("BOWLING_BULB", "BOWLINGBULB");
        pamFull("SEA_SHROOM", "SEASHROOM");
        pamFull("WINTER_MELON", "WINTERMELON");
        pamFull("PEPPER_PULT", "PEPPERPULT");
        pamFull("PRIMAL_POTATO_MINE", "PRIMAL_POTATOMINE");
        pamFull("CHERRY_BOMB", "CHERRYBOMB");
        pamFull("DOOM_SHROOM", "DOOMSHROOM");
        pamFull("TANGLE_KELP", "TANGLEKELP");
        pamFull("PHAT_BEET", "PHATBEETS");
        pamFull("TALL_NUT", "TALLNUT");
        pamFull("ENDURIAN", "ENDURIAN");
        pamFull("GARLIC", "GARLIC");
        pamFull("SUN_BEAN", "SUNBEAN");
        pamFull("MAGNET_SHROOM", "MAGNETSHROOM");
        pamFull("ICE_SHROOM", "ICESHROOM");
        pamFull("LILY_PAD", "LILYPAD");
        pamFull("HOT_POTATO", "HOTPOTATO");
        PAM_PATHS.put("ENLIGHTEN_MINT", "768/INITIAL/EMPOWERMINTS/PLANT/ENLIGHTENMINT/ENLIGHTENMINT.PAM");
        PAM_PATHS.put("APPEASE_MINT", "768/INITIAL/EMPOWERMINTS/PLANT/APPEASEMINT/APPEASEMINT.PAM");
        PAM_PATHS.put("ARMA_MINT", "768/INITIAL/EMPOWERMINTS/PLANT/ARMAMINT/ARMAMINT.PAM");
        PAM_PATHS.put("BOMBARD_MINT", "768/INITIAL/EMPOWERMINTS/PLANT/BOMBARDMINT/BOMBARDMINT.PAM");
        PAM_PATHS.put("ENFORCE_MINT", "768/INITIAL/EMPOWERMINTS/PLANT/ENFORCEMINT/ENFORCEMINT.PAM");
        PAM_PATHS.put("REINFORCE_MINT", "768/INITIAL/EMPOWERMINTS/PLANT/REINFORCEMINT/REINFORCEMINT.PAM");
        PAM_PATHS.put("ENCHANT_MINT", "768/INITIAL/EMPOWERMINTS/PLANT/ENCHANTMINT/ENCHANTMINT.PAM");
        PAM_PATHS.put("PIERCE_MINT", "768/INITIAL/EMPOWERMINTS/PLANT/SPEARMINT/SPEARMINT.PAM");
        pamInitial("WALLNUT_BOWLING", "WALLNUT");
        pamInitial("GIANT_WALLNUT", "WALLNUT");
    }

    private PlantTexturePaths() {
    }

    public static String getPath(String plantTypeName) {
        return PATHS.getOrDefault(plantTypeName, FALLBACK);
    }

    public static String getPamPath(String plantTypeName) {
        return PAM_PATHS.get(plantTypeName);
    }

    public static boolean hasPamAnimation(String plantTypeName) {
        return PAM_PATHS.containsKey(plantTypeName);
    }
}
