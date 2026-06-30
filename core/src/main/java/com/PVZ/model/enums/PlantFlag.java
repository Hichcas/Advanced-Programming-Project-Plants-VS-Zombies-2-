package com.PVZ.model.enums;

import java.util.Locale;

public enum PlantFlag {
    DOUBLE_SUN_CHANCE,
    RESET_FAMILY_COOLDOWN,
    PLANT_FOOD_ON_SPAWN,
    FREEZE_ATTACKERS,
    IGNORE_ARMOR,
    CAN_TARGET_FLYING,
    EXPLODE_ON_FINISH,
    SPLASH_DAMAGE,
    INSTANT_GROW,
    PASS_THROUGH_ZOMBIES,
    AOE_ON_HIT,
    CHILL_ON_HIT,
    POISON_ON_HIT,
    WATER_ONLY,
    STACKABLE,
    CHARGE_BASED,
    MAGIC_ATTACK,
    MOVE_ZOMBIES,
    SUN_ON_DEATH,
    TARGET_PRIORITY_UP,
    AOE_ON_DEATH,
    BURST_SHOT,
    DOUBLE_PROJECTILE,
    HIGH_PIERCE,
    RETARGET_FLYING,
    UNKNOWN;

    public static PlantFlag fromRaw(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return UNKNOWN;
        }

        String value = raw.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (value) {
            case "double_sun_chance" -> DOUBLE_SUN_CHANCE;
            case "reset_family_cooldown", "reset_family_cooldowns" -> RESET_FAMILY_COOLDOWN;
            case "plant_food_on_spawn", "plant_food_on_enterance", "plant_food_on_entrance" -> PLANT_FOOD_ON_SPAWN;
            case "freeze_attackers" -> FREEZE_ATTACKERS;
            case "ignore_armor" -> IGNORE_ARMOR;
            case "can_target_flying", "target_flying" -> CAN_TARGET_FLYING;
            case "explode_on_finish" -> EXPLODE_ON_FINISH;
            case "splash_damage" -> SPLASH_DAMAGE;
            case "instant_grow" -> INSTANT_GROW;
            case "pass_through_zombies" -> PASS_THROUGH_ZOMBIES;
            case "aoe_on_hit" -> AOE_ON_HIT;
            case "chill_on_hit" -> CHILL_ON_HIT;
            case "poison_on_hit" -> POISON_ON_HIT;
            case "water_only" -> WATER_ONLY;
            case "stackable" -> STACKABLE;
            case "charge_based" -> CHARGE_BASED;
            case "magic_attack" -> MAGIC_ATTACK;
            case "move_zombies" -> MOVE_ZOMBIES;
            case "sun_on_death" -> SUN_ON_DEATH;
            case "target_priority_up" -> TARGET_PRIORITY_UP;
            case "aoe_on_death" -> AOE_ON_DEATH;
            case "burst_shot" -> BURST_SHOT;
            case "double_projectile" -> DOUBLE_PROJECTILE;
            case "high_pierce" -> HIGH_PIERCE;
            case "retarget_flying" -> RETARGET_FLYING;
            default -> UNKNOWN;
        };
    }
}
