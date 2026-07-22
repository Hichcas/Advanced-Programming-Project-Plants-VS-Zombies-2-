package com.PVZ.model.entity.zombies.base;

public class ZombieArmor {
    public enum ArmorType {
        CONE, BUCKET, BRICK, ICE_BLOCK, SARCOPHAGUS, SHOULDER_ARMOR, CROWN
    }

    private ArmorType type;
    private double baseHealth;
    private double currentHealth;
    private boolean droppable;
    private boolean metallic;
    private boolean helm;

    public ZombieArmor(ArmorType type, double baseHealth, boolean droppable,
                       boolean metallic, boolean helm) {
        this.type = type;
        this.baseHealth = baseHealth;
        this.currentHealth = baseHealth;
        this.droppable = droppable;
        this.metallic = metallic;
        this.helm = helm;
    }

    public double getHealth() { return currentHealth; }

    public void scaleHealth(double factor) {
        baseHealth *= factor;
        currentHealth *= factor;
    }

    public void takeDamage(double amount) {
        currentHealth = Math.max(0, currentHealth - amount);
    }

    public boolean isDestroyed() { return currentHealth <= 0; }

    public ArmorType getType() { return type; }
    public double getBaseHealth() { return baseHealth; }
    public boolean isDroppable() { return droppable; }
    public boolean isMetallic() { return metallic; }
    public boolean isHelm() { return helm; }
}
