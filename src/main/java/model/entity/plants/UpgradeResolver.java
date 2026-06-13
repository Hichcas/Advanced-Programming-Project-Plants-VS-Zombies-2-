package model.entity.plants;

import model.entity.plants.UpgradeRule.UpgradeKind;
import model.entity.plants.UpgradeRule.UpgradeOperation;
import model.enums.PlantFlag;
import model.enums.PlantStatType;
import model.enums.SpecialUpgradeType;

import java.util.Locale;

public final class UpgradeResolver {
    private UpgradeResolver() {
    }

    public static PlantStats resolveStats(PlantDefinition definition, int targetLevel) {
        if (definition == null) {
            throw new IllegalArgumentException("Plant definition cannot be null");
        }

        PlantStats stats = baseStats(definition);

        if (targetLevel <= 1) {
            return stats;
        }

        for (int level = 2; level <= targetLevel; level++) {
            UpgradeRule upgrade = definition.getUpgradeForLevel(level);
            if (upgrade != null) {
                applyUpgrade(stats, upgrade);
            }
        }

        stats.putExtra("resolvedLevel", targetLevel);
        return stats;
    }

    public static PlantStats baseStats(PlantDefinition definition) {
        PlantStats stats = new PlantStats();
        stats.setCost(definition.getCost());
        stats.setMaxHp(definition.getBaseHp());
        stats.setDamage(definition.getEffectiveDamage());
        stats.setActionIntervalSeconds(definition.getActionIntervalSeconds());
        stats.setRechargeSeconds(definition.getRechargeSeconds());
        stats.putExtra("plantId", definition.getId());
        stats.putExtra("plantKey", definition.getPlantKey());
        stats.putExtra("plantName", definition.getName());
        stats.putExtra("category", definition.getCategory());
        stats.putExtra("tags", definition.getTags());
        stats.putExtra("tagEnums", definition.getTagEnums());

        applyAbilityMetadata(stats, definition.getBaseAbility(), "baseAbility");
        applyAbilityMetadata(stats, definition.getPlantFoodEffect(), "plantFood");
        return stats;
    }

    public static void applyUpgrade(PlantStats stats, UpgradeRule rule) {
        if (stats == null || rule == null) {
            return;
        }

        UpgradeKind kind = rule.getKindEnum();
        switch (kind) {
            case STAT -> applyStatUpgrade(stats, rule);
            case FLAG -> applyFlagUpgrade(stats, rule);
            case SPECIAL -> applySpecialUpgrade(stats, rule);
            case FAMILY, BEHAVIOR -> applyBehaviorUpgrade(stats, rule);
            case UNKNOWN -> stats.putExtra("unknown_upgrade_" + rule.getLevel(), rule.getRaw());
        }

        stats.putExtra("lastAppliedUpgradeLevel", rule.getLevel());
    }

    private static void applyStatUpgrade(PlantStats stats, UpgradeRule rule) {
        PlantStatType statType = rule.getStatEnum();
        UpgradeOperation operation = rule.getOperationEnum();
        double value = rule.getValueOrDefault(0.0);

        switch (statType) {
            case COST -> stats.setCost(applyInt(stats.getCost(), operation, value));
            case HP -> stats.setMaxHp(applyInt(stats.getMaxHp(), operation, value));
            case DAMAGE -> stats.setDamage(applyInt(stats.getDamage(), operation, value));
            case DAMAGE_PER_TICK -> stats.setDamagePerTick(applyInt(stats.getDamagePerTick(), operation, value));
            case COOLDOWN, RECHARGE -> stats.setRechargeSeconds(applyDouble(stats.getRechargeSeconds(), operation, value));
            case PRODUCTION_TIME -> stats.setProductionTimeSeconds(applyDouble(stats.getProductionTimeSeconds(), operation, value));
            case GROW_TIME, GROWTH_TIME -> stats.setGrowthTimeSeconds(applyDouble(stats.getGrowthTimeSeconds(), operation, value));
            case CHARGE_TIME -> stats.setChargeTimeSeconds(applyDouble(stats.getChargeTimeSeconds(), operation, value));
            case ATTACK_SPEED -> stats.setActionIntervalSeconds(applyDouble(stats.getActionIntervalSeconds(), operation, value));
            case PLANT_FOOD_CHANCE -> stats.setPlantFoodChancePercent(applyDouble(stats.getPlantFoodChancePercent(), operation, value));
            case FREEZE_TIME -> stats.setFreezeTimeSeconds(applyDouble(stats.getFreezeTimeSeconds(), operation, value));
            case CHILL_TIME -> stats.setChillTimeSeconds(applyDouble(stats.getChillTimeSeconds(), operation, value));
            case SUN_AMOUNT -> stats.setSunAmount(applyInt(stats.getSunAmount(), operation, value));
            case DURATION -> stats.setDurationSeconds(applyDouble(stats.getDurationSeconds(), operation, value));
            case ARM_TIME -> stats.setArmTimeSeconds(applyDouble(stats.getArmTimeSeconds(), operation, value));
            case BOUNCES -> stats.setBounces(applyInt(stats.getBounces(), operation, value));
            case BUTTER -> stats.setButter(applyInt(stats.getButter(), operation, value));
            case DIGEST_TIME -> stats.setDigestTimeSeconds(applyDouble(stats.getDigestTimeSeconds(), operation, value));
            case EAT_TIME -> stats.setEatTimeSeconds(applyDouble(stats.getEatTimeSeconds(), operation, value));
            case EXPLODE_DAMAGE -> stats.setExplodeDamage(applyInt(stats.getExplodeDamage(), operation, value));
            case LIFESPAN -> stats.setLifespanSeconds(applyDouble(stats.getLifespanSeconds(), operation, value));
            case MAX_SIZE -> stats.setMaxSize(applyDouble(stats.getMaxSize(), operation, value));
            case MELT_AREA -> stats.setMeltAreaRadius(applyInt(stats.getMeltAreaRadius(), operation, value));
            case PIERCE -> stats.setPierce(applyInt(stats.getPierce(), operation, value));
            case RANGE -> stats.setRange(applyInt(stats.getRange(), operation, value));
            case REFLECT_DAMAGE -> stats.setReflectDamage(applyInt(stats.getReflectDamage(), operation, value));
            case REGEN -> stats.setRegenPerSecond(applyDouble(stats.getRegenPerSecond(), operation, value));
            case SUN_DROP -> stats.setSunDropAmount(applyInt(stats.getSunDropAmount(), operation, value));
            case TARGETS -> stats.setTargetCount(applyInt(stats.getTargetCount(), operation, value));
            case WARMTH_RADIUS -> stats.setWarmthRadius(applyInt(stats.getWarmthRadius(), operation, value));
            case AOE_DAMAGE -> stats.setAoeDamage(applyInt(stats.getAoeDamage(), operation, value));
            case UNKNOWN -> stats.putExtra(rule.getRaw() != null ? rule.getRaw() : "stat_unknown", value);
        }
    }

    private static void applyFlagUpgrade(PlantStats stats, UpgradeRule rule) {
        PlantFlag flag = rule.getFlagEnum();
        if (flag != PlantFlag.UNKNOWN) {
            stats.addFlag(flag);
        } else if (rule.getRaw() != null) {
            stats.putExtra("flag:" + rule.getRaw(), Boolean.TRUE);
        }
    }

    private static void applySpecialUpgrade(PlantStats stats, UpgradeRule rule) {
        SpecialUpgradeType special = rule.getSpecialEnum();

        switch (special) {
            case DOUBLE_SUN_CHANCE -> stats.addFlag(PlantFlag.DOUBLE_SUN_CHANCE);
            case RESET_FAMILY_COOLDOWNS -> stats.addFlag(PlantFlag.RESET_FAMILY_COOLDOWN);
            case PLANT_FOOD_ON_ENTRANCE, PLANT_FOOD_ON_SPAWN -> stats.addFlag(PlantFlag.PLANT_FOOD_ON_SPAWN);
            case TARGET_PRIORITY_UP -> stats.addFlag(PlantFlag.TARGET_PRIORITY_UP);
            case AOE_ON_DEATH -> stats.addFlag(PlantFlag.AOE_ON_DEATH);
            case BURST_SHOT -> stats.addFlag(PlantFlag.BURST_SHOT);
            case DOUBLE_PROJECTILE -> stats.addFlag(PlantFlag.DOUBLE_PROJECTILE);
            case HIGH_PIERCE -> stats.addFlag(PlantFlag.HIGH_PIERCE);
            case IGNORE_ARMOR -> stats.addFlag(PlantFlag.IGNORE_ARMOR);
            case CAN_TARGET_FLYING -> stats.addFlag(PlantFlag.CAN_TARGET_FLYING);
            case PASS_THROUGH_ZOMBIES -> stats.addFlag(PlantFlag.PASS_THROUGH_ZOMBIES);
            case EXPLODE_ON_FINISH -> stats.addFlag(PlantFlag.EXPLODE_ON_FINISH);
            case SUN_ON_DEATH -> stats.addFlag(PlantFlag.SUN_ON_DEATH);
            case FREEZE_ATTACKERS -> stats.addFlag(PlantFlag.FREEZE_ATTACKERS);
            case CHILL_ON_HIT -> stats.addFlag(PlantFlag.CHILL_ON_HIT);
            case POISON_ON_HIT -> stats.addFlag(PlantFlag.POISON_ON_HIT);
            case RETARGET_FLYING -> stats.addFlag(PlantFlag.RETARGET_FLYING);
            case WARMTH_RADIUS_UP -> stats.putExtra("warmthRadiusBoost", Boolean.TRUE);
            case SUMMON_ALLY -> stats.putExtra("summonAlly", Boolean.TRUE);
            case TRANSFORM_TARGET -> stats.putExtra("transformTarget", Boolean.TRUE);
            case CAN_CRUSH_2X -> stats.putExtra("canCrush2x", Boolean.TRUE);
            case ZOMBIE_HP_BUFF -> stats.putExtra("zombieHpBuff", Boolean.TRUE);
            case ZOMBIE_DAMAGE_BUFF -> stats.putExtra("zombieDamageBuff", Boolean.TRUE);
            case UNKNOWN -> stats.putExtra("special:" + rule.getRaw(), rule.getParams());
        }
    }

    private static void applyBehaviorUpgrade(PlantStats stats, UpgradeRule rule) {
        if (rule.getBehaviorId() != null && !rule.getBehaviorId().isEmpty()) {
            stats.putExtra("behaviorOverride", rule.getBehaviorId());
        }
        if (rule.getFamily() != null && !rule.getFamily().isEmpty()) {
            stats.putExtra("family", rule.getFamily());
        }
        if (rule.getRaw() != null) {
            stats.putExtra("behavior:" + rule.getRaw(), rule.getParams());
        }
    }

    private static void applyAbilityMetadata(PlantStats stats, AbilitySpec ability, String prefix) {
        if (stats == null || ability == null) {
            return;
        }

        String kind = normalize(ability.getResolvedBehaviorId());
        stats.putExtra(prefix + "Kind", ability.getKind());
        stats.putExtra(prefix + "ResolvedBehaviorId", ability.getResolvedBehaviorId());
        stats.putExtra(prefix + "Raw", ability.getRaw());
        stats.putExtra(prefix + "Params", ability.getParams());

        if (kind == null) {
            return;
        }

        switch (kind) {
            case "sun_production" -> {
                stats.setSunAmount(ability.getIntParam("sunAmount", stats.getSunAmount()));
                stats.setProductionTimeSeconds(ability.getDoubleParam("intervalSeconds", stats.getProductionTimeSeconds()));
            }
            case "growing_sun" -> {
                stats.setSunAmount(ability.getIntParam("sunAmount", stats.getSunAmount()));
                stats.setProductionTimeSeconds(ability.getDoubleParam("intervalSeconds", stats.getProductionTimeSeconds()));
                stats.putExtra(prefix + "SunAmounts", ability.getParam("sunAmounts"));
                stats.putExtra(prefix + "GrowthStageTimes", ability.getParam("growthStageTimes"));
            }
            case "instant_sun" -> {
                stats.setSunAmount(ability.getIntParam("sunAmount", stats.getSunAmount()));
                stats.setDurationSeconds(Math.max(stats.getDurationSeconds(), 1.0));
            }
            case "direct_shot", "burst_shot", "fire_shot", "piercing_shot", "homing_shot", "wall_defense", "hypnotize" -> {
                stats.putExtra("projectileType", ability.getStringParam("projectileType", inferProjectileType(kind)));
                stats.putExtra("projectileEffect", inferProjectileEffect(kind, ability));
                stats.putExtra("projectileCount", ability.getIntParam("projectiles", ability.getIntParam("burstCount", 1)));
                stats.putExtra("targetPriority", ability.getStringParam("targetPriority", null));
                if (ability.hasParam("pierce")) {
                    stats.setPierce(Math.max(stats.getPierce(), ability.getIntParam("pierce", stats.getPierce())));
                }
            }
            case "bounce_shot", "lobber_kernel" -> {
                stats.putExtra("projectileType", ability.getStringParam("projectileType", "LOB"));
                stats.putExtra("projectileEffect", inferProjectileEffect(kind, ability));
            }
            case "explosion" -> {
                stats.setArmTimeSeconds(ability.getDoubleParam("armTimeSeconds", stats.getArmTimeSeconds()));
                stats.setExplodeDamage(ability.getIntParam("damage", stats.getExplodeDamage()));
                stats.setAoeDamage(ability.getIntParam("aoeDamage", stats.getAoeDamage()));
            }
            case "melee_eat" -> stats.putExtra("meleeRange", ability.getIntParam("range", stats.getRange()));
            case "move_zombies" -> stats.putExtra("moveZombies", Boolean.TRUE);
            case "magnet_disarm" -> stats.putExtra("disarmTargets", ability.getStringParam("targets", "METAL_ARMOR"));
            case "copy_plant" -> stats.putExtra("copies", ability.getIntParam("copies", 2));
            case "water_support" -> stats.putExtra("surface", ability.getStringParam("surface", "WATER"));
            case "mint_family_buff" -> stats.putExtra("family", ability.getStringParam("family", "MINT"));
            default -> {
                // keep the raw metadata only
            }
        }
    }

    private static String inferProjectileType(String kind) {
        return switch (kind) {
            case "fire_shot" -> "FIRE_PEA";
            case "hypnotize" -> "SEED";
            case "burst_shot", "direct_shot", "homing_shot", "wall_defense" -> "PEA";
            case "lobber_kernel", "bounce_shot" -> "LOB";
            default -> "PEA";
        };
    }

    private static String inferProjectileEffect(String kind, AbilitySpec ability) {
        if (ability != null) {
            String explicitEffect = ability.getStringParam("effect", null);
            if (explicitEffect != null && !explicitEffect.isBlank()) {
                return explicitEffect;
            }
        }

        return switch (kind) {
            case "fire_shot" -> "FIRE";
            case "piercing_shot" -> "FIRE";
            case "hypnotize" -> "HYPNOTIZE";
            case "burst_shot" -> "BURST";
            default -> "NONE";
        };
    }

    private static int applyInt(int current, UpgradeOperation operation, double value) {
        return switch (operation) {
            case ADD -> (int) Math.round(current + value);
            case SUBTRACT -> (int) Math.round(current - value);
            case SET -> (int) Math.round(value);
            case MULTIPLY -> (int) Math.round(current * value);
            case UNKNOWN -> current;
        };
    }

    private static double applyDouble(double current, UpgradeOperation operation, double value) {
        return switch (operation) {
            case ADD -> current + value;
            case SUBTRACT -> current - value;
            case SET -> value;
            case MULTIPLY -> current * value;
            case UNKNOWN -> current;
        };
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
