package model.enums;

import model.entity.Plant;
import model.entity.plants.PlantDefinition;
import model.entity.plants.PlantFactory;
import model.entity.plants.PlantLibrary;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum PlantType {
    SUNFLOWER(1, "Sunflower"),
    TWIN_SUNFLOWER(2, "Twin Sunflower"),
    SUN_SHROOM(3, "Sun-shroom"),
    PRIMAL_SUNFLOWER(4, "Primal Sunflower"),
    GOLD_BLOOM(5, "Gold Bloom"),

    PEASHOOTER(6, "Peashooter"),
    REPEATER(7, "Repeater"),
    THREEPEATER(8, "Threepeater"),
    SNOW_PEA(9, "Snow Pea"),
    ROTOBAGA(10, "Rotobaga"),
    PEA_POD(11, "Pea Pod"),
    SPLIT_PEA(12, "Split Pea"),
    CITRON(13, "Citron"),
    CAULIPOWER(14, "Caulipower"),
    ELECTRIC_BLUEBERRY(15, "Electric Blueberry"),
    BOWLING_BULB(16, "Bowling Bulb"),
    CACTUS(17, "Cactus"),
    FIRE_PEASHOOTER(18, "Fire Peashooter"),
    STARFRUIT(19, "Starfruit"),
    GOO_PEASHOOTER(20, "Goo Peashooter"),
    MEGA_GATLING_PEA(21, "Mega Gatling Pea"),

    SEA_SHROOM(22, "Sea-shroom"),
    PUFF_SHROOM(23, "Puff-shroom"),
    FUME_SHROOM(24, "Fume-shroom"),
    CABBAGE_PULT(25, "Cabbage-pult"),
    KERNEL_PULT(26, "Kernel-pult"),
    MELON_PULT(27, "Melon-pult"),
    WINTER_MELON(28, "Winter Melon"),
    PEPPER_PULT(29, "Pepper-pult"),

    POTATO_MINE(30, "Potato Mine"),
    PRIMAL_POTATO_MINE(31, "Primal Potato Mine"),
    CHERRY_BOMB(32, "Cherry Bomb"),
    SQUASH(33, "Squash"),
    GRAPESHOT(34, "Grapeshot"),
    JALAPENO(35, "Jalapeno"),
    DOOM_SHROOM(36, "Doom-shroom"),
    TANGLE_KELP(37, "Tangle Kelp"),
    ICEBERG_LETTUCE(38, "Iceberg Lettuce"),

    BONK_CHOY(39, "Bonk Choy"),
    PHAT_BEET(40, "Phat Beet"),
    CHOMPER(41, "Chomper"),
    WASABI_WHIP(42, "Wasabi Whip"),
    KIWIBEAST(43, "Kiwibeast"),

    WALL_NUT(44, "Wall-nut"),
    TALL_NUT(45, "Tall-nut"),
    ENDURIAN(46, "Endurian"),
    GARLIC(47, "Garlic"),
    SWEET_POTATO(48, "Sweet Potato"),
    EXPLODE_O_NUT(49, "Explode-o-nut"),
    PUMPKIN(50, "Pumpkin"),
    SUN_BEAN(51, "Sun Bean"),
    TORCHWOOD(52, "Torchwood"),
    MAGNET_SHROOM(53, "Magnet-shroom"),
    HYPNO_SHROOM(54, "Hypno-shroom"),
    CAT_TAIL(55, "Cat-tail"),
    IMITATER(56, "Imitater"),
    ICE_SHROOM(57, "Ice-shroom"),
    LILY_PAD(58, "Lily Pad"),
    HOT_POTATO(59, "Hot Potato"),
    GRAVE_BUSTER(60, "Grave Buster"),

    ENLIGHTEN_MINT(61, "Enlighten-mint"),
    APPEASE_MINT(62, "Appease-mint"),
    ARMA_MINT(63, "Arma-mint"),
    BOMBARD_MINT(64, "Bombard-mint"),
    ENFORCE_MINT(65, "Enforce-mint"),
    REINFORCE_MINT(66, "Reinforce-mint"),
    ENCHANT_MINT(67, "Enchant-mint"),
    PIERCE_MINT(68, "Pierce-mint"),
    CAT_TAIL_MINT(69, "catTail-mint");

    private static final Map<Integer, PlantType> BY_ID;
    private static final Map<String, PlantType> BY_NAME;

    static {
        Map<Integer, PlantType> byId = new HashMap<>();
        Map<String, PlantType> byName = new HashMap<>();

        for (PlantType type : values()) {
            byId.put(type.legacyId, type);
            byName.put(normalize(type.displayName), type);
            byName.put(normalize(type.name()), type);
        }

        BY_ID = Collections.unmodifiableMap(byId);
        BY_NAME = Collections.unmodifiableMap(byName);
    }

    private final int legacyId;
    private final String displayName;

    PlantType(int legacyId, String displayName) {
        this.legacyId = legacyId;
        this.displayName = displayName;
    }

    public int getLegacyId() {
        return legacyId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PlantDefinition getDefinition() {
        return PlantLibrary.findByType(this)
                .or(() -> PlantLibrary.findByName(displayName))
                .orElse(null);
    }

    public Plant create(int userLevel) {
        return PlantFactory.createPlant(this, userLevel);
    }

    public static PlantType fromId(int id) {
        PlantType type = BY_ID.get(id);
        if (type == null) {
            throw new IllegalArgumentException("Unknown plant id: " + id);
        }
        return type;
    }

    public static PlantType fromName(String name) {
        PlantType type = BY_NAME.get(normalize(name));
        if (type == null) {
            throw new IllegalArgumentException("Unknown plant name: " + name);
        }
        return type;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}