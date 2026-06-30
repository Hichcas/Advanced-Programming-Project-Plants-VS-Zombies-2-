package com.PVZ.model.entity.plants;

import com.PVZ.model.enums.PlantType;

import java.util.HashMap;
import java.util.Map;

public class PlantInstance {
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

    public void setPlantFoodTicksRemaining(int plantFoodTicksRemaining) {
        this.plantFoodTicksRemaining = Math.max(0, plantFoodTicksRemaining);
        this.plantFoodActive = this.plantFoodTicksRemaining > 0;
    }

    public void tickPlantFood() {
        if (plantFoodTicksRemaining > 0) {
            plantFoodTicksRemaining--;
            if (plantFoodTicksRemaining == 0) {
                plantFoodActive = false;
            }
        }
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
