package com.PVZ.model.enums;

import java.util.Locale;

public enum PlantCategory {
    SUN_PRODUCER,
    SHOOTER,
    LOBBER,
    EXPLOSIVE,
    MELEE,
    WALL,
    MODIFIER,
    THROUGH_STRIKE,
    HOMING,
    MINT,
    UNKNOWN;

    public static PlantCategory fromRaw(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return UNKNOWN;
        }

        String value = raw.trim().toLowerCase(Locale.ROOT);

        return switch (value) {
            case "sun producer", "producers sun", "producer" -> SUN_PRODUCER;
            case "shooter", "shooters" -> SHOOTER;
            case "lobber", "lobbers" -> LOBBER;
            case "explosive", "explosives" -> EXPLOSIVE;
            case "melee", "attackers melee" -> MELEE;
            case "wall", "wall-nut", "nuts-wall" -> WALL;
            case "modifier" -> MODIFIER;
            case "through-strike" -> THROUGH_STRIKE;
            case "homing" -> HOMING;
            case "mint", "mints" -> MINT;
            default -> UNKNOWN;
        };
    }
}
