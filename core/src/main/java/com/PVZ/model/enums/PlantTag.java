package com.PVZ.model.enums;

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
        if (raw == null || raw.trim().isEmpty() || raw.equals("-")) {
            return UNKNOWN;
        }

        String value = raw.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (value) {
            case "day" -> DAY;
            case "night" -> NIGHT;
            case "shroom" -> SHROOM;
            case "up_wramp", "upramp", "wramp_up" -> UPRAMP;
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
            case "movezombies", "move_zombies" -> MOVE_ZOMBIES;
            case "sun" -> SUN;
            case "explosive" -> EXPLOSIVE;
            default -> UNKNOWN;
        };
    }
}
