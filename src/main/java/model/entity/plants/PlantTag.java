package model.entity.plants;

import java.util.Locale;

public enum PlantTag {
    DAY,
    NIGHT,
    SHROOM,
    UPRAMP,
    PEA,
    ICE,
    FIRE,
    STACK,
    CHARGE,
    MAGIC,
    POISON,
    WATER,
    AOE,
    TRAP,
    MOVE_ZOMBIES,
    SUN,
    EXPLOSIVE,
    UNKNOWN;


    public static PlantTag fromRaw(String raw) {
        if (raw == null) {
            return UNKNOWN;
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);

        return switch (value) {
            case "day" -> DAY;
            case "night" -> NIGHT;
            case "shroom" -> SHROOM;
            case "wramp-up" -> UPRAMP;
            case "pea" -> PEA;
            case "ice" -> ICE;
            case "fire" -> FIRE;
            case "stack" -> STACK;
            case "charge" -> CHARGE;
            case "magic" -> MAGIC;
            case "poison" -> POISON;
            case "water" -> WATER;
            case "aoe" -> AOE;
            case "trap" -> TRAP;
            case "movezombies" -> MOVE_ZOMBIES;
            case "sun" -> SUN;
            case "explosive" -> EXPLOSIVE;
            default -> UNKNOWN;
        };
    }
}