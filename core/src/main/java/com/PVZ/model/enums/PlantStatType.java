package com.PVZ.model.enums;

import java.util.Locale;

public enum PlantStatType {
    COST,
    HP,
    DAMAGE,
    DAMAGE_PER_TICK,
    COOLDOWN,
    RECHARGE,
    PRODUCTION_TIME,
    GROW_TIME,
    GROWTH_TIME,
    CHARGE_TIME,
    ATTACK_SPEED,
    PLANT_FOOD_CHANCE,
    FREEZE_TIME,
    CHILL_TIME,
    SUN_AMOUNT,
    DURATION,
    ARM_TIME,
    BOUNCES,
    BUTTER,
    DIGEST_TIME,
    EAT_TIME,
    EXPLODE_DAMAGE,
    LIFESPAN,
    MAX_SIZE,
    MELT_AREA,
    PIERCE,
    RANGE,
    REFLECT_DAMAGE,
    REGEN,
    SUN_DROP,
    TARGETS,
    WARMTH_RADIUS,
    AOE_DAMAGE,
    UNKNOWN;

    public static PlantStatType fromRaw(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return UNKNOWN;
        }

        String value = raw.trim()
                .toLowerCase(Locale.ROOT)
                .replace(".", "")
                .replace("/", "_")
                .replace(" ", "")
                .replace("-", "_");

        return switch (value) {
            case "cost" -> COST;
            case "hp", "health" -> HP;
            case "dmg", "damage" -> DAMAGE;
            case "dmg_tick", "dmgpertick", "damagepertick", "damage_per_tick", "damage_per_second",
                    "dps" -> DAMAGE_PER_TICK;
            case "cooldown" -> COOLDOWN;
            case "recharge" -> RECHARGE;
            case "prodtime", "productiontime" -> PRODUCTION_TIME;
            case "growtime", "growthtime" -> GROW_TIME;
            case "chargetime" -> CHARGE_TIME;
            case "attackspeed", "atkspeed" -> ATTACK_SPEED;
            case "plantfoodchance" -> PLANT_FOOD_CHANCE;
            case "freezetime" -> FREEZE_TIME;
            case "chilltime" -> CHILL_TIME;
            case "sunamount" -> SUN_AMOUNT;
            case "duration" -> DURATION;
            case "armtime" -> ARM_TIME;
            case "bounces" -> BOUNCES;
            case "butter" -> BUTTER;
            case "digest", "digesttime" -> DIGEST_TIME;
            case "eattime" -> EAT_TIME;
            case "explodedamage", "explode_damage" -> EXPLODE_DAMAGE;
            case "lifespan" -> LIFESPAN;
            case "maxsize" -> MAX_SIZE;
            case "meltarea", "melt_area" -> MELT_AREA;
            case "pierce" -> PIERCE;
            case "range" -> RANGE;
            case "reflectdamage", "reflect_damage" -> REFLECT_DAMAGE;
            case "regen" -> REGEN;
            case "sundrop", "sun_drop" -> SUN_DROP;
            case "targets" -> TARGETS;
            case "warmthradius", "warmth_radius" -> WARMTH_RADIUS;
            case "aoedamage", "aoe_damage" -> AOE_DAMAGE;
            default -> UNKNOWN;
        };
    }
}
