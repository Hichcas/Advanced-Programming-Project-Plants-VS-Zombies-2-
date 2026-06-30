package com.PVZ.model.entity;

import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.PlantStats;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.BehaviorFactory;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.plants.behavior.PlantFoodBehavior;
import com.PVZ.model.enums.PlantType;

import java.util.Map;

public class Plant {
    private final PlantInstance instance;
    private final PlantBehavior mainBehavior;
    private final PlantFoodBehavior plantFoodBehavior;

    public Plant(PlantInstance instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Plant instance cannot be null");
        }

        this.instance = instance;
        this.mainBehavior = BehaviorFactory.createMainBehavior(instance.getDefinition());
        this.plantFoodBehavior = BehaviorFactory.createPlantFoodBehavior(instance.getDefinition());
    }

    public static Plant of(PlantDefinition definition, int level) {
        return new Plant(PlantFactory.create(definition, level));
    }

    public PlantInstance getInstance() {
        return instance;
    }

    public PlantDefinition getDefinition() {
        return instance.getDefinition();
    }

    public PlantType getType() {
        return instance.getType();
    }

    public int getLevel() {
        return instance.getLevel();
    }

    public PlantStats getStats() {
        return instance.getStats();
    }

    public int getCurrentHp() {
        return instance.getCurrentHp();
    }

    public void setCurrentHp(int currentHp) {
        instance.setCurrentHp(currentHp);
    }

    public boolean isDead() {
        return instance.isDead();
    }

    public void takeDamage(int amount) {
        instance.takeDamage(amount);
    }

    public void heal(int amount) {
        instance.heal(amount);
    }

    public boolean isPlanted() {
        return instance.isPlanted();
    }

    public void setPlanted(boolean planted) {
        instance.setPlanted(planted);
    }

    public boolean isPlantFoodActive() {
        return instance.isPlantFoodActive();
    }

    public void setPlantFoodActive(boolean plantFoodActive) {
        instance.setPlantFoodActive(plantFoodActive);
    }

    public int getPlantFoodTicksRemaining() {
        return instance.getPlantFoodTicksRemaining();
    }

    public void setPlantFoodTicksRemaining(int ticks) {
        instance.setPlantFoodTicksRemaining(ticks);
    }

    public Map<String, Object> getRuntimeState() {
        return instance.getRuntimeState();
    }

    public void putRuntimeState(String key, Object value) {
        instance.putRuntimeState(key, value);
    }

    public Object getRuntimeState(String key) {
        return instance.getRuntimeState(key);
    }

    public void update(BehaviorContext context, double deltaTimeSeconds) {
        mainBehavior.onUpdate(instance, context, deltaTimeSeconds);
        instance.tickPlantFood();
    }

    public void applyPlantFood(BehaviorContext context) {
        plantFoodBehavior.onPlantFood(instance, context);
    }

    public String getMainBehaviorId() {
        return instance.getMainBehaviorId();
    }

    public String getPlantFoodBehaviorId() {
        return instance.getPlantFoodBehaviorId();
    }
}
