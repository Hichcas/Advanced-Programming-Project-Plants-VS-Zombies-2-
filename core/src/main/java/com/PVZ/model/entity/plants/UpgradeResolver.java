package com.PVZ.model.entity.plants;

import com.PVZ.model.entity.plants.UpgradeRule.UpgradeKind;
import com.PVZ.model.entity.plants.UpgradeRule.UpgradeOperation;
import com.PVZ.model.enums.PlantFlag;
import com.PVZ.model.enums.PlantStatType;
import com.PVZ.model.enums.SpecialUpgradeType;

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

        return stats;
    }

    public static PlantStats baseStats(PlantDefinition definition) {
        PlantStats stats = new PlantStats();
        stats.setCost(definition.getCost());
        stats.setMaxHp(definition.getBaseHp());
        stats.setDamage(definition.getEffectiveDamage());
        stats.setActionIntervalSeconds(definition.getActionIntervalSeconds());
        stats.setRechargeSeconds(definition.getRechargeSeconds());

        AbilitySpec baseAbility = definition.getBaseAbility();
        if (baseAbility != null) {
            stats.putExtra("baseAbilityId", baseAbility.getResolvedBehaviorId());
            applyBaseAbilityParams(stats, baseAbility);
        }
        if (definition.getPlantFoodEffect() != null) {
            stats.putExtra("plantFoodAbilityId", definition.getPlantFoodEffect().getResolvedBehaviorId());
        }

        return stats;
    }

    /**
     * Populates base production stats (sun amount and production interval) directly from the
     * plant's base-ability params. Without this, data-driven sun producers fall back to the
     * generic default of 50 sun: Twin Sunflower must produce 100, Primal Sunflower 75 and
     * Gold Bloom 375. Sun-shroom is unaffected because it stores a {@code sunAmounts} list
     * (handled by the growth-stage path) rather than a scalar {@code sunAmount}.
     */
    private static void applyBaseAbilityParams(PlantStats stats, AbilitySpec baseAbility) {
        int sunAmount = baseAbility.getIntParam("sunAmount", 0);
        if (sunAmount > 0) {
            stats.setSunAmount(sunAmount);
        }
        double intervalSeconds = baseAbility.getDoubleParam("intervalSeconds", 0.0);
        if (intervalSeconds > 0) {
            stats.setProductionTimeSeconds(intervalSeconds);
        }

        // --- Explosive / Mine params ---
        double armTime = baseAbility.getDoubleParam("armTimeSeconds", -1.0);
        if (armTime >= 0.0) {
            stats.setArmTimeSeconds(armTime);
        }
        int explodeDamage = baseAbility.getIntParam("explodeDamage", 0);
        if (explodeDamage > 0) {
            stats.setExplodeDamage(explodeDamage);
        }

        // --- AoE / Splash params ---
        int aoeDamage = baseAbility.getIntParam("aoeDamage", 0);
        if (aoeDamage > 0) {
            stats.setAoeDamage(aoeDamage);
        }

        // --- Elemental attack flags (stored as extras for behavior classes) ---
        if (baseAbility.getBooleanParam("freezeAttack", false)) {
            stats.putExtra("freezeAttack", true);
            double slowPct = baseAbility.getDoubleParam("slowPercent", 0.0);
            if (slowPct > 0) {
                stats.putExtra("slowPercent", slowPct);
            }
            double slowDur = baseAbility.getDoubleParam("slowDurationSeconds", 0.0);
            if (slowDur > 0) {
                stats.putExtra("slowDurationSeconds", slowDur);
            }
        }
        if (baseAbility.getBooleanParam("fireAttack", false)) {
            stats.putExtra("fireAttack", true);
        }
        int warmthRadius = baseAbility.getIntParam("warmthRadius", 0);
        if (warmthRadius > 0) {
            stats.setWarmthRadius(warmthRadius);
        }

        // --- Kernel-pult butter mechanic ---
        int butterChance = baseAbility.getIntParam("butterChancePercent", 0);
        if (butterChance > 0) {
            stats.setButter(butterChance);
        }

        // --- Contact-triggered flag ---
        if (baseAbility.getBooleanParam("contactTriggered", false)) {
            stats.putExtra("contactTriggered", true);
        }

        // --- Lane-clear flag (Jalapeno) ---
        if (baseAbility.getBooleanParam("meltsIce", false)) {
            stats.putExtra("meltsIce", true);
        }
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
    }

    private static void applyStatUpgrade(PlantStats stats, UpgradeRule rule) {
        PlantStatType statType = rule.getStatEnum();
        UpgradeOperation operation = rule.getOperationEnum();
        double value = rule.getValueOrDefault(0.0);

        switch (statType) {
            case COST -> stats.setCost(applyInt(stats.getCost(), operation, value));
            case HP -> stats.setMaxHp(applyInt(stats.getMaxHp(), operation, value));
            case DAMAGE -> stats.setDamage(applyInt(stats.getDamage(), operation, value));
            case COOLDOWN, RECHARGE -> stats.setRechargeSeconds(applyDouble(stats.getRechargeSeconds(), operation,
                    value));
            case PRODUCTION_TIME -> stats.setProductionTimeSeconds(applyDouble(stats.getProductionTimeSeconds(),
                    operation, value));
            case GROW_TIME -> stats.setGrowthTimeSeconds(applyDouble(stats.getGrowthTimeSeconds(), operation, value));
            case CHARGE_TIME -> stats.setChargeTimeSeconds(applyDouble(stats.getChargeTimeSeconds(), operation, value));
            case PLANT_FOOD_CHANCE -> stats.setPlantFoodChancePercent(applyDouble(stats.getPlantFoodChancePercent(),
                    operation, value));
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
}
