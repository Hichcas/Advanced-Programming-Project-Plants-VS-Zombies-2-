package com.PVZ.model.entity.plants;

import com.PVZ.model.enums.PlantType;

import java.util.HashMap;
import java.util.Map;

public class PlantInstance {
    /** RegularGameEngine advances gameplay in 0.1 second ticks. */
    public static final int PLANT_FOOD_TICKS_PER_SECOND = 10;
    private final PlantDefinition definition;
    private final PlantStats stats;
    private final Map<String, Object> runtimeState = new HashMap<>();

    private int level;
    private int currentHp;
    private boolean planted;
    private boolean plantFoodActive;
    private int plantFoodTicksRemaining;

    private String mainBehaviorId;
    private String plantFoodBehaviorId;

    public PlantInstance(PlantDefinition definition, PlantStats stats, int level) {
        this.definition = definition;
        this.stats = stats;
        this.level = level;
        this.currentHp = stats.getMaxHp();

        if (definition != null && definition.getBaseAbility() != null) {
            this.mainBehaviorId = definition.getBaseAbility().getBehaviorId();
        }
        if (definition != null && definition.getPlantFoodEffect() != null) {
            this.plantFoodBehaviorId = definition.getPlantFoodEffect().getBehaviorId();
        }
    }

    public PlantDefinition getDefinition() {
        return definition;
    }

    public PlantType getType() {
        return definition == null ? null : definition.getType();
    }

    public PlantStats getStats() {
        return stats;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = Math.max(0, currentHp);
    }

    public void takeDamage(int amount) {
        if (amount > 0) {
            currentHp = Math.max(0, currentHp - amount);
        }
    }

    public void heal(int amount) {
        if (amount > 0) {
            currentHp = Math.min(stats.getMaxHp(), currentHp + amount);
        }
    }

    public boolean isDead() {
        return currentHp <= 0;
    }

    public boolean isPlanted() {
        return planted;
    }

    public void setPlanted(boolean planted) {
        this.planted = planted;
    }

    public boolean isPlantFoodActive() {
        return plantFoodActive;
    }

    public void setPlantFoodActive(boolean plantFoodActive) {
        this.plantFoodActive = plantFoodActive;
    }

    public int getPlantFoodTicksRemaining() {
        return plantFoodTicksRemaining;
    }

    public void setPlantFoodSeconds(double seconds) {
        int ticks = (int) Math.ceil(Math.max(0.0, seconds) * PLANT_FOOD_TICKS_PER_SECOND);
        setPlantFoodTicksRemaining(ticks);
    }

    public void setPlantFoodTicksRemaining(int plantFoodTicksRemaining) {
        boolean wasActive = this.plantFoodActive;
        this.plantFoodTicksRemaining = Math.max(0, plantFoodTicksRemaining);
        this.plantFoodActive = this.plantFoodTicksRemaining > 0;
        if (wasActive && !this.plantFoodActive) {
            clearTemporaryPlantFoodExtras();
        }
    }

    public void tickPlantFood() {
        if (plantFoodTicksRemaining > 0) {
            plantFoodTicksRemaining--;
            if (plantFoodTicksRemaining == 0) {
                plantFoodActive = false;
                clearTemporaryPlantFoodExtras();
            }
        }
    }

    /**
     * پاک‌سازی تمام «extra»های موقتی‌ای که Plant Food behaviorها روی
     * {@code stats} می‌گذارند (مثل pfFireAttack، passThrough، ...) درست
     * در لحظه‌ای که اثر Plant Food تمام می‌شود.
     *
     * چرا این متد لازم بود: قبلا این extraها هیچ‌وقت پاک نمی‌شدند - تنها
     * چیزی که مانع دائمی‌شدنشان می‌شد، این بود که هر جای مصرف‌کننده حتما
     * یادش باشد قبل از خواندنشان {@code isPlantFoodActive()} را هم چک
     * کند. این یک طراحی شکننده بود (یک جای فراموش‌شده کافی بود تا اثر
     * Plant Food برای همیشه روی گیاه بماند - دقیقا همون باگی که با
     * پروجکتایل‌های Plant-Food-دار دیده می‌شد). حالا به‌جای تکیه به یادآوری
     * هر مصرف‌کننده، خودِ منبع را در لحظه‌ی انقضا صفر می‌کنیم - قطعی و
     * غیرقابل فراموشی.
     */
    private void clearTemporaryPlantFoodExtras() {
        if (stats == null) return;
        stats.putExtra("pfFireAttack", Boolean.FALSE);
        stats.putExtra("pfIceAttack", Boolean.FALSE);
        stats.putExtra("passThrough", Boolean.FALSE);
        stats.putExtra("pierceBoost", 0);
        stats.putExtra("plantFoodProjectileCount", 0);
        stats.putExtra("plantFoodDamageMultiplier", 1.0);
        stats.putExtra("burstAttack", Boolean.FALSE);
    }

    public String getMainBehaviorId() {
        return mainBehaviorId;
    }

    public void setMainBehaviorId(String mainBehaviorId) {
        this.mainBehaviorId = mainBehaviorId;
    }

    public String getPlantFoodBehaviorId() {
        return plantFoodBehaviorId;
    }

    public void setPlantFoodBehaviorId(String plantFoodBehaviorId) {
        this.plantFoodBehaviorId = plantFoodBehaviorId;
    }

    public Map<String, Object> getRuntimeState() {
        return runtimeState;
    }

    public Object getRuntimeState(String key) {
        return runtimeState.get(key);
    }

    public void putRuntimeState(String key, Object value) {
        runtimeState.put(key, value);
    }

    public int getEffectiveDamage() {
        return stats.getDamage();
    }
}
