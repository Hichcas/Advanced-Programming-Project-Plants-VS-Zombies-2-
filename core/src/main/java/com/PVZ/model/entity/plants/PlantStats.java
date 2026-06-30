package com.PVZ.model.entity.plants;

import com.PVZ.model.enums.PlantFlag;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PlantStats {
    private int cost;
    private int maxHp;
    private int damage;

    private double actionIntervalSeconds;
    private double rechargeSeconds;
    private double productionTimeSeconds;
    private double growthTimeSeconds;
    private double chargeTimeSeconds;
    private double plantFoodChancePercent;
    private double freezeTimeSeconds;
    private double chillTimeSeconds;
    private int sunAmount;
    private double durationSeconds;
    private double armTimeSeconds;
    private int bounces;
    private int butter;
    private double digestTimeSeconds;
    private double eatTimeSeconds;
    private int explodeDamage;
    private double lifespanSeconds;
    private double maxSize;
    private int meltAreaRadius;
    private int pierce;
    private int range;
    private int reflectDamage;
    private double regenPerSecond;
    private int sunDropAmount;
    private int targetCount;
    private int warmthRadius;
    private int aoeDamage;

    private final EnumSet<PlantFlag> flags = EnumSet.noneOf(PlantFlag.class);
    private final Map<String, Object> extras = new HashMap<>();

    public PlantStats() {
    }

    public PlantStats copy() {
        PlantStats copy = new PlantStats();
        copy.cost = cost;
        copy.maxHp = maxHp;
        copy.damage = damage;
        copy.actionIntervalSeconds = actionIntervalSeconds;
        copy.rechargeSeconds = rechargeSeconds;
        copy.productionTimeSeconds = productionTimeSeconds;
        copy.growthTimeSeconds = growthTimeSeconds;
        copy.chargeTimeSeconds = chargeTimeSeconds;
        copy.plantFoodChancePercent = plantFoodChancePercent;
        copy.freezeTimeSeconds = freezeTimeSeconds;
        copy.chillTimeSeconds = chillTimeSeconds;
        copy.sunAmount = sunAmount;
        copy.durationSeconds = durationSeconds;
        copy.armTimeSeconds = armTimeSeconds;
        copy.bounces = bounces;
        copy.butter = butter;
        copy.digestTimeSeconds = digestTimeSeconds;
        copy.eatTimeSeconds = eatTimeSeconds;
        copy.explodeDamage = explodeDamage;
        copy.lifespanSeconds = lifespanSeconds;
        copy.maxSize = maxSize;
        copy.meltAreaRadius = meltAreaRadius;
        copy.pierce = pierce;
        copy.range = range;
        copy.reflectDamage = reflectDamage;
        copy.regenPerSecond = regenPerSecond;
        copy.sunDropAmount = sunDropAmount;
        copy.targetCount = targetCount;
        copy.warmthRadius = warmthRadius;
        copy.aoeDamage = aoeDamage;
        copy.flags.addAll(flags);
        copy.extras.putAll(extras);
        return copy;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public double getActionIntervalSeconds() {
        return actionIntervalSeconds;
    }

    public void setActionIntervalSeconds(double actionIntervalSeconds) {
        this.actionIntervalSeconds = actionIntervalSeconds;
    }

    public double getRechargeSeconds() {
        return rechargeSeconds;
    }

    public void setRechargeSeconds(double rechargeSeconds) {
        this.rechargeSeconds = rechargeSeconds;
    }

    public double getProductionTimeSeconds() {
        return productionTimeSeconds;
    }

    public void setProductionTimeSeconds(double productionTimeSeconds) {
        this.productionTimeSeconds = productionTimeSeconds;
    }

    public double getGrowthTimeSeconds() {
        return growthTimeSeconds;
    }

    public void setGrowthTimeSeconds(double growthTimeSeconds) {
        this.growthTimeSeconds = growthTimeSeconds;
    }

    public double getChargeTimeSeconds() {
        return chargeTimeSeconds;
    }

    public void setChargeTimeSeconds(double chargeTimeSeconds) {
        this.chargeTimeSeconds = chargeTimeSeconds;
    }

    public double getPlantFoodChancePercent() {
        return plantFoodChancePercent;
    }

    public void setPlantFoodChancePercent(double plantFoodChancePercent) {
        this.plantFoodChancePercent = plantFoodChancePercent;
    }

    public double getFreezeTimeSeconds() {
        return freezeTimeSeconds;
    }

    public void setFreezeTimeSeconds(double freezeTimeSeconds) {
        this.freezeTimeSeconds = freezeTimeSeconds;
    }

    public double getChillTimeSeconds() {
        return chillTimeSeconds;
    }

    public void setChillTimeSeconds(double chillTimeSeconds) {
        this.chillTimeSeconds = chillTimeSeconds;
    }

    public int getSunAmount() {
        return sunAmount;
    }

    public void setSunAmount(int sunAmount) {
        this.sunAmount = sunAmount;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public double getArmTimeSeconds() {
        return armTimeSeconds;
    }

    public void setArmTimeSeconds(double armTimeSeconds) {
        this.armTimeSeconds = armTimeSeconds;
    }

    public int getBounces() {
        return bounces;
    }

    public void setBounces(int bounces) {
        this.bounces = bounces;
    }

    public int getButter() {
        return butter;
    }

    public void setButter(int butter) {
        this.butter = butter;
    }

    public double getDigestTimeSeconds() {
        return digestTimeSeconds;
    }

    public void setDigestTimeSeconds(double digestTimeSeconds) {
        this.digestTimeSeconds = digestTimeSeconds;
    }

    public double getEatTimeSeconds() {
        return eatTimeSeconds;
    }

    public void setEatTimeSeconds(double eatTimeSeconds) {
        this.eatTimeSeconds = eatTimeSeconds;
    }

    public int getExplodeDamage() {
        return explodeDamage;
    }

    public void setExplodeDamage(int explodeDamage) {
        this.explodeDamage = explodeDamage;
    }

    public double getLifespanSeconds() {
        return lifespanSeconds;
    }

    public void setLifespanSeconds(double lifespanSeconds) {
        this.lifespanSeconds = lifespanSeconds;
    }

    public double getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(double maxSize) {
        this.maxSize = maxSize;
    }

    public int getMeltAreaRadius() {
        return meltAreaRadius;
    }

    public void setMeltAreaRadius(int meltAreaRadius) {
        this.meltAreaRadius = meltAreaRadius;
    }

    public int getPierce() {
        return pierce;
    }

    public void setPierce(int pierce) {
        this.pierce = pierce;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getReflectDamage() {
        return reflectDamage;
    }

    public void setReflectDamage(int reflectDamage) {
        this.reflectDamage = reflectDamage;
    }

    public double getRegenPerSecond() {
        return regenPerSecond;
    }

    public void setRegenPerSecond(double regenPerSecond) {
        this.regenPerSecond = regenPerSecond;
    }

    public int getSunDropAmount() {
        return sunDropAmount;
    }

    public void setSunDropAmount(int sunDropAmount) {
        this.sunDropAmount = sunDropAmount;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public int getWarmthRadius() {
        return warmthRadius;
    }

    public void setWarmthRadius(int warmthRadius) {
        this.warmthRadius = warmthRadius;
    }

    public int getAoeDamage() {
        return aoeDamage;
    }

    public void setAoeDamage(int aoeDamage) {
        this.aoeDamage = aoeDamage;
    }

    public EnumSet<PlantFlag> getFlags() {
        return EnumSet.copyOf(flags);
    }

    public boolean hasFlag(PlantFlag flag) {
        return flags.contains(flag);
    }

    public void addFlag(PlantFlag flag) {
        if (flag != null && flag != PlantFlag.UNKNOWN) {
            flags.add(flag);
        }
    }

    public void removeFlag(PlantFlag flag) {
        flags.remove(flag);
    }

    public Map<String, Object> getExtras() {
        return Collections.unmodifiableMap(extras);
    }

    public void putExtra(String key, Object value) {
        extras.put(key, value);
    }

    public Object getExtra(String key) {
        return extras.get(key);
    }

    public <T> T getExtra(String key, Class<T> type) {
        Object value = extras.get(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public boolean getBooleanExtra(String key, boolean defaultValue) {
        Object value = extras.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value == null ? defaultValue : Boolean.parseBoolean(String.valueOf(value));
    }

    public int getIntExtra(String key, int defaultValue) {
        Object value = extras.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public double getDoubleExtra(String key, double defaultValue) {
        Object value = extras.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null ? defaultValue : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public void mergeFlags(Iterable<PlantFlag> otherFlags) {
        if (otherFlags == null) {
            return;
        }
        for (PlantFlag flag : otherFlags) {
            addFlag(flag);
        }
    }

    public void mergeExtras(Map<String, Object> otherExtras) {
        if (otherExtras == null) {
            return;
        }
        extras.putAll(otherExtras);
    }

    @Override
    public String toString() {
        return "PlantStats{"
                + "cost=" + cost
                + ", maxHp=" + maxHp
                + ", damage=" + damage
                + ", flags=" + flags
                + ", extras=" + extras.keySet()
                + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(cost, maxHp, damage, actionIntervalSeconds, rechargeSeconds);
    }
}
