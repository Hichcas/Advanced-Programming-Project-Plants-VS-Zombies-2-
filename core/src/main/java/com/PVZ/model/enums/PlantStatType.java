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
            case "prodtime", "productiontime", "production_time" -> PRODUCTION_TIME;
            case "growtime", "growthtime", "growth_time" -> GROW_TIME;
            case "chargetime", "charge_time" -> CHARGE_TIME;
            case "attackspeed", "atkspeed", "attack_speed" -> ATTACK_SPEED;
            case "plantfoodchance", "plant_food_chance" -> PLANT_FOOD_CHANCE;
            case "freezetime", "freeze_time" -> FREEZE_TIME;
            case "chilltime", "chill_time" -> CHILL_TIME;
            case "sunamount", "sun_amount" -> SUN_AMOUNT;
            case "duration" -> DURATION;
            case "armtime", "arm_time" -> ARM_TIME;
            case "bounces" -> BOUNCES;
            case "butter" -> BUTTER;
            case "digest", "digesttime" -> DIGEST_TIME;
            case "eattime", "eat_time" -> EAT_TIME;
            case "explodedamage", "explode_damage" -> EXPLODE_DAMAGE;
            case "lifespan" -> LIFESPAN;
            case "maxsize", "max_size" -> MAX_SIZE;
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
