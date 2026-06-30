package com.PVZ.model.enums;

import java.util.Locale;

public enum SpecialUpgradeType {
    DOUBLE_SUN_CHANCE,
    RESET_FAMILY_COOLDOWNS,
    PLANT_FOOD_ON_ENTRANCE,
    PLANT_FOOD_ON_SPAWN,
    TARGET_PRIORITY_UP,
    AOE_ON_DEATH,
    BURST_SHOT,
    DOUBLE_PROJECTILE,
    HIGH_PIERCE,
    IGNORE_ARMOR,
    CAN_TARGET_FLYING,
    PASS_THROUGH_ZOMBIES,
    EXPLODE_ON_FINISH,
    SUN_ON_DEATH,
    FREEZE_ATTACKERS,
    CHILL_ON_HIT,
    POISON_ON_HIT,
    SUMMON_ALLY,
    TRANSFORM_TARGET,
    WARMTH_RADIUS_UP,
    RETARGET_FLYING,
    CAN_CRUSH_2X,
    ZOMBIE_HP_BUFF,
    ZOMBIE_DAMAGE_BUFF,
    UNKNOWN;

    public static SpecialUpgradeType fromRaw(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return UNKNOWN;
        }

        String value = normalize(raw);

        return switch (value) {
            case "double_sun_chance" -> DOUBLE_SUN_CHANCE;
            case "reset_family_cooldowns", "reset_family_cooldown" -> RESET_FAMILY_COOLDOWNS;
            case "plant_food_on_entrance", "plant_food_on_enterance" -> PLANT_FOOD_ON_ENTRANCE;
            case "plant_food_on_spawn" -> PLANT_FOOD_ON_SPAWN;
            case "target_priority_up" -> TARGET_PRIORITY_UP;
            case "aoe_on_death" -> AOE_ON_DEATH;
            case "burst_shot", "multi_shot" -> BURST_SHOT;
            case "double_projectile" -> DOUBLE_PROJECTILE;
            case "high_pierce" -> HIGH_PIERCE;
            case "ignore_armor" -> IGNORE_ARMOR;
            case "can_target_flying", "target_flying" -> CAN_TARGET_FLYING;
            case "pass_through_zombies" -> PASS_THROUGH_ZOMBIES;
            case "explode_on_finish" -> EXPLODE_ON_FINISH;
            case "sun_on_death" -> SUN_ON_DEATH;
            case "freeze_attackers" -> FREEZE_ATTACKERS;
            case "chill_on_hit" -> CHILL_ON_HIT;
            case "poison_on_hit" -> POISON_ON_HIT;
            case "summon_ally" -> SUMMON_ALLY;
            case "transform_target" -> TRANSFORM_TARGET;
            case "warmth_radius_up", "warmth_radius" -> WARMTH_RADIUS_UP;
            case "retarget_flying" -> RETARGET_FLYING;
            case "can_crush_2x", "can_crush_2" -> CAN_CRUSH_2X;
            case "zombie_hp_buff" -> ZOMBIE_HP_BUFF;
            case "zombie_damage_buff" -> ZOMBIE_DAMAGE_BUFF;
            default -> UNKNOWN;
        };
    }

    private static String normalize(String raw) {
        return raw.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
