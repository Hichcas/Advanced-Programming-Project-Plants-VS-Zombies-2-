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

        // Normalize hyphens/spaces/underscores to a single separator so raw JSON values like
        // "SUN_PRODUCER" or "WALL_NUT" match the same way "sun producer" / "wall-nut" would.
        String value = raw.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');

        return switch (value) {
            case "sun_producer", "producers_sun", "producer", "sun" -> SUN_PRODUCER;
            case "shooter", "shooters" -> SHOOTER;
            case "lobber", "lobbers" -> LOBBER;
            case "explosive", "explosives" -> EXPLOSIVE;
            case "melee", "attackers_melee", "melee_attackers" -> MELEE;
            case "wall", "wall_nut", "nuts_wall", "wallnut" -> WALL;
            case "modifier" -> MODIFIER;
            case "through_strike", "strike_through" -> THROUGH_STRIKE;
            case "homing" -> HOMING;
            case "mint", "mints" -> MINT;
            default -> UNKNOWN;
        };
    }
}
