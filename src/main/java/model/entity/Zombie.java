package model.entity;

import model.enums.ZombieType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Abstract base class for all zombies.
 * Defines immutable attributes, movement, attack, armor, effects, and death.
 */
public abstract class Zombie {

    // ---------- Immutable core attributes ----------
    protected final ZombieType zombieType;
    protected final double maxHealth;
    protected final double speed;              // cells per second (base, before modifiers)
    protected final double damage;             // damage per bite
    protected final int attackInterval;        // ticks between bites
    protected final int waveCost;              // cost for wave spawning

    // ---------- Mutable state ----------
    protected double health;                   // current health
    protected double xPosition;                // continuous horizontal position
    protected int lane;                        // row index (0-4)
    protected boolean dead;
    protected int currentAttackCooldown;       // internal cooldown for next attack

    // Glowing zombie (5% chance) → drops plant food on death
    protected final boolean glowing;

    // Armor & Effects
    protected final List<Armor> armors;
    protected final List<Effect> activeEffects;

    // Special behavior tags (e.g., "FLYING", "FIRE_RESISTANT")
    protected final Set<String> tags;

    /**
     * Constructor called by subclasses with immutable stats.
     * @param zombieType    type enum value
     * @param maxHealth     base HP (without armor)
     * @param speed         movement speed in cells per second
     * @param damage        damage per bite
     * @param attackInterval ticks between consecutive bites
     * @param waveCost      cost used in wave difficulty calculation
     */
    public Zombie(ZombieType zombieType, double maxHealth, double speed, double damage,
                  int attackInterval, int waveCost) {
        this.zombieType = zombieType;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.speed = speed;
        this.damage = damage;
        this.attackInterval = attackInterval;
        this.waveCost = waveCost;
        this.currentAttackCooldown = 0;
        this.dead = false;
        this.glowing = Math.random() < 0.05;   // 5% glowing
        this.armors = new ArrayList<>();
        this.activeEffects = new ArrayList<>();
        this.tags = new HashSet<>();
    }

    // ---------- Abstract factory method ----------
    /**
     * Creates a new instance of the same zombie type with stats possibly scaled by level.
     * The concrete subclass must call its own constructor (which calls super).
     * @param level  level (e.g., 1, 2, ...) to influence stats
     * @return a new Zombie instance of the same type
     */
    public abstract Zombie makeAnInstance(int level);

    // ====================== Update Loop ======================
    /**
     * Called every tick by the game engine.
     */
    public void update(GameMap map) {
        if (dead) return;

        // 1. Update effects
        updateEffects();

        // 2. Attack or Move
        Plant target = getTargetPlant(map);
        if (target != null && !target.isDead()) {
            currentAttackCooldown--;
            if (currentAttackCooldown <= 0) {
                attack(target);
                currentAttackCooldown = attackInterval;
            }
        } else {
            move();
            currentAttackCooldown = 0;
        }

        // 3. Subclass-specific action (e.g., steal sun, explode dynamite)
        specialAction(map);

        // 4. Check if zombie reached the house
        if (hasReachedHouse()) {
            map.triggerLawnMowerOrLose(this);
        }
    }

    // ----------- Movement -----------
    protected void move() {
        if (canMove()) {
            double effectiveSpeed = speed * getSpeedMultiplier();
            xPosition -= effectiveSpeed / 10.0;   // per tick (10 ticks = 1 sec)
        }
    }

    protected boolean canMove() {
        for (Effect e : activeEffects) {
            if (e.getType() == EffectType.FROZEN) return false;
        }
        return true;
    }

    // ----------- Attack -----------
    protected void attack(Plant plant) {
        plant.takeDamage(damage, DamageType.NORMAL);
    }

    protected abstract Plant getTargetPlant(GameMap map);

    // ----------- Effects -----------
    public void addEffect(Effect effect) {
        if (effect.getType() == EffectType.CHILLED) {
            for (Effect e : activeEffects) {
                if (e.getType() == EffectType.CHILLED) {
                    e.increaseDuration(effect.getRemainingDuration());
                    return;
                }
            }
        }
        activeEffects.removeIf(e -> e.getType() == effect.getType());
        activeEffects.add(effect);
    }

    public void removeEffect(EffectType type) {
        activeEffects.removeIf(e -> e.getType() == type);
    }

    protected void updateEffects() {
        activeEffects.removeIf(e -> {
            e.tick();
            return e.isExpired();
        });
    }

    public double getSpeedMultiplier() {
        double mult = 1.0;
        for (Effect e : activeEffects) {
            if (e.getType() == EffectType.CHILLED) mult *= 0.5;
        }
        return mult;
    }

    // ----------- Taking Damage -----------
    public void takeDamage(double amount, DamageType type) {
        if (dead) return;

        if (type == DamageType.POISON || type == DamageType.MAGIC) {
            health -= amount;
        } else {
            double remaining = amount;
            for (Armor armor : armors) {
                if (armor.isBroken()) continue;
                double absorbed = Math.min(armor.getHealth(), remaining);
                armor.damage(absorbed);
                remaining -= absorbed;
                if (remaining <= 0) break;
            }
            if (remaining > 0) {
                health -= remaining;
            }
        }

        if (health <= 0) {
            die();
        }
    }

    // ----------- Death -----------
    protected void die() {
        dead = true;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public void onDeath(GameMap map) {
        // default: nothing extra
    }

    // ----------- Special action placeholder -----------
    protected void specialAction(GameMap map) {
        // Override in subclasses for custom behaviors
    }

    // ----------- Helper methods -----------
    public int getColumn() {
        return (int) Math.floor(xPosition);
    }

    public boolean hasReachedHouse() {
        return xPosition <= -1;
    }

    public boolean isFrozen() {
        return activeEffects.stream().anyMatch(e -> e.getType() == EffectType.FROZEN);
    }

    // ----------- Getters (no setters for immutable fields) -----------
    public ZombieType getZombieType() { return zombieType; }
    public double getHealth() { return health; }
    public double getMaxHealth() { return maxHealth; }
    public double getSpeed() { return speed; }
    public double getDamage() { return damage; }
    public int getAttackInterval() { return attackInterval; }
    public int getWaveCost() { return waveCost; }
    public int getLane() { return lane; }
    public void setLane(int lane) { this.lane = lane; }
    public double getX() { return xPosition; }
    public void setX(double x) { this.xPosition = x; }
    public boolean isDead() { return dead; }
    public List<Armor> getArmors() { return Collections.unmodifiableList(armors); }
    public List<Effect> getActiveEffects() { return Collections.unmodifiableList(activeEffects); }
    public Set<String> getTags() { return tags; }
    public void addTag(String tag) { tags.add(tag); }
    public boolean hasTag(String tag) { return tags.contains(tag); }

    // ====================== Inner Classes ======================
    public static class Armor {
        private final String name;
        private final double maxHealth;
        private double currentHealth;
        private final boolean metallic;

        public Armor(String name, double health, boolean metallic) {
            this.name = name;
            this.maxHealth = health;
            this.currentHealth = health;
            this.metallic = metallic;
        }

        public void damage(double amount) {
            currentHealth = Math.max(0, currentHealth - amount);
        }

        public boolean isBroken() { return currentHealth <= 0; }
        public double getHealth() { return currentHealth; }
        public boolean isMetallic() { return metallic; }
        public String getName() { return name; }
    }

    public static class Effect {
        private final EffectType type;
        private final double duration;         // total ticks
        private double remainingDuration;

        public Effect(EffectType type, double durationInTicks) {
            this.type = type;
            this.duration = durationInTicks;
            this.remainingDuration = durationInTicks;
        }

        public void tick() {
            remainingDuration = Math.max(0, remainingDuration - 1);
        }

        public boolean isExpired() { return remainingDuration <= 0; }
        public EffectType getType() { return type; }
        public double getRemainingDuration() { return remainingDuration; }

        public void increaseDuration(double extraTicks) {
            remainingDuration += extraTicks;
        }
    }

    public enum EffectType {
        CHILLED,    // half speed
        FROZEN,     // cannot move/attack
        HYPNOTIZED  // attacks other zombies
    }

    public enum DamageType {
        NORMAL,
        FIRE,
        ICE,
        POISON,
        MAGIC,
        EXPLOSIVE
    }
}