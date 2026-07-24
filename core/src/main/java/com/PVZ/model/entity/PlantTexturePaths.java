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

    private PlantTexturePaths() {
    }

    public static String getPath(String plantTypeName) {
        return PATHS.getOrDefault(plantTypeName, FALLBACK);
    }
}
