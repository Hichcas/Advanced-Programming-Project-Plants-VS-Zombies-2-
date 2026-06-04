package model.entity;

// import model.map.GameMap;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract base class for all zombies.
 * Defines movement, attack, armor, effects, and death behavior.
 */
public abstract class Zombie {
    // // --- Basic attributes ---
    // protected String name;              // نوع زامبی (مثلاً "Basic", "ConeHead")
    // protected double maxHealth;         // حداکثر سلامتی
    // protected double health;            // سلامتی فعلی
    // protected double speed;             // سرعت پایه (تعداد خانه در ثانیه)
    // protected double damage;            // آسیب هر لقمه به گیاه
    // protected int attackInterval;       // فاصلهٔ بین حملات (بر حسب tick)
    // protected int currentAttackCooldown;// شمارندهٔ داخلی برای حمله

    // // موقعیت: x مختصات افقی (ممکن است اعشاری باشد) و lane شمارهٔ ردیف (۰ تا ۴)
    // protected double xPosition;
    // protected int lane;

    // protected boolean dead;
    // protected int waveCost;             // هزینهٔ ظاهر شدن در موج

    // // جلوهٔ درخشان (۵٪ احتمال موقع spawn) → غذای گیاه می‌اندازد
    // protected boolean glowing;

    // // --- Armor & Effects ---
    // protected List<Armor> armors;
    // protected List<Effect> activeEffects;

    // // --- Tags for special behaviors ---
    // protected Set<String> tags;         // e.g., "FLYING", "FIRE_RESISTANT", "MAGNETIC"

    // /**
    //  * Constructor used by subclasses.
    //  * @param name           zombie type name
    //  * @param maxHealth      base HP (without armor)
    //  * @param speed          movement speed in cells per second (used per tick)
    //  * @param damage         damage per bite
    //  * @param attackInterval ticks between consecutive bites
    //  * @param waveCost       cost for wave spawning formula
    //  */
    // public Zombie(String name, double maxHealth, double speed, double damage,
    //               int attackInterval, int waveCost) {
    //     this.name = name;
    //     this.maxHealth = maxHealth;
    //     this.health = maxHealth;
    //     this.speed = speed;
    //     this.damage = damage;
    //     this.attackInterval = attackInterval;
    //     this.currentAttackCooldown = 0;
    //     this.waveCost = waveCost;
    //     this.dead = false;
    //     this.glowing = Math.random() < 0.05; // 5% chance to glow
    //     this.armors = new ArrayList<>();
    //     this.activeEffects = new ArrayList<>();
    //     this.tags = new HashSet<>();
    // }

    // // ====================== Update Loop ======================
    // /**
    //  * Called every tick by the game engine.
    //  * 1) Update effects (tick down durations, remove expired).
    //  * 2) Find a valid plant target. If found → attack (if cooldown ready).
    //  *    Otherwise → move left.
    //  * 3) Perform any special action (to be overridden).
    //  */
    // public void update(GameMap map) {
    //     if (dead) return;

    //     // 1. Effects
    //     updateEffects();

    //     // 2. Attack or Move
    //     Plant target = getTargetPlant(map);   // abstract, depends on zombie type
    //     if (target != null && !target.isDead()) {
    //         // stop moving and try to attack
    //         currentAttackCooldown--;
    //         if (currentAttackCooldown <= 0) {
    //             attack(target);
    //             currentAttackCooldown = attackInterval;
    //         }
    //     } else {
    //         // No target → move left (unless frozen)
    //         move();
    //         currentAttackCooldown = 0; // reset cooldown if walking
    //     }

    //     // 3. Special subclass behavior (e.g., Prospector dynamite, Ra steal sun)
    //     specialAction(map);

    //     // 4. Check for reaching the house
    //     if (hasReachedHouse()) {
    //         map.triggerLawnMowerOrLose(this);  // will be called by game logic
    //     }
    // }

    // // ----------- Movement -----------
    // protected void move() {
    //     if (canMove()) {
    //         double effectiveSpeed = speed * getSpeedMultiplier();
    //         xPosition -= effectiveSpeed / 10.0; // because 10 ticks = 1 second, move per tick
    //     }
    // }

    // protected boolean canMove() {
    //     for (Effect e : activeEffects) {
    //         if (e.getType() == EffectType.FROZEN) return false;
    //     }
    //     return true;
    // }

    // // ----------- Attack -----------
    // /**
    //  * Performs a bite on the given plant. Default: deal damage.
    //  * Can be overridden for zombies with special attack styles.
    //  */
    // protected void attack(Plant plant) {
    //     plant.takeDamage(damage, DamageType.NORMAL);
    // }

    // /**
    //  * Returns the plant this zombie would currently attack.
    //  * In most cases: the leftmost plant in the same lane within reach.
    //  * Override for zombies that target differently.
    //  */
    // protected abstract Plant getTargetPlant(GameMap map);

    // // ----------- Effects -----------
    // public void addEffect(Effect effect) {
    //     // Stacking rule: chilled extends duration, frozen replaces, others simply add
    //     if (effect.getType() == EffectType.CHILLED) {
    //         for (Effect e : activeEffects) {
    //             if (e.getType() == EffectType.CHILLED) {
    //                 e.increaseDuration(effect.getRemainingDuration());
    //                 return;
    //             }
    //         }
    //     }
    //     // For other types, remove existing of same type before adding
    //     activeEffects.removeIf(e -> e.getType() == effect.getType());
    //     activeEffects.add(effect);
    // }

    // public void removeEffect(EffectType type) {
    //     activeEffects.removeIf(e -> e.getType() == type);
    // }

    // protected void updateEffects() {
    //     activeEffects.removeIf(e -> {
    //         e.tick();
    //         return e.isExpired();
    //     });
    // }

    // public double getSpeedMultiplier() {
    //     double mult = 1.0;
    //     for (Effect e : activeEffects) {
    //         if (e.getType() == EffectType.CHILLED) mult *= 0.5;
    //         // other modifiers possible
    //     }
    //     return mult;
    // }

    // // ----------- Taking Damage -----------
    // /**
    //  * Apply damage considering armor and damage type.
    //  * @param amount raw damage
    //  * @param type   damage category (NORMAL, FIRE, ICE, POISON, MAGIC, EXPLOSIVE)
    //  */
    // public void takeDamage(double amount, DamageType type) {
    //     if (dead) return;

    //     // Poison/Magic bypasses armor
    //     if (type == DamageType.POISON || type == DamageType.MAGIC) {
    //         health -= amount;
    //     } else {
    //         double remaining = amount;
    //         for (Armor armor : armors) {
    //             if (armor.isBroken()) continue;
    //             double absorbed = Math.min(armor.getHealth(), remaining);
    //             armor.damage(absorbed);
    //             remaining -= absorbed;
    //             if (remaining <= 0) break;
    //         }
    //         if (remaining > 0) {
    //             health -= remaining;
    //         }
    //     }

    //     if (health <= 0) {
    //         die();
    //     }
    // }

    // // ----------- Death -----------
    // protected void die() {
    //     dead = true;
    //     // Drop loot will be handled by the game controller after calling this.
    //     // The zombie can store information about potential drops.
    // }

    // /**
    //  * Whether this zombie is a glowing one (drops plant food).
    //  */
    // public boolean isGlowing() {
    //     return glowing;
    // }

    // /**
    //  * Override in subclasses that need custom death behavior (e.g., Gargantuar throws Imp).
    //  */
    // public void onDeath(GameMap map) {
    //     // default: nothing extra
    // }

    // // ----------- Helper methods -----------
    // public int getColumn() {
    //     return (int) Math.floor(xPosition);   // ستون فعلی
    // }

    // public boolean hasReachedHouse() {
    //     return xPosition <= -1;   // خانهٔ چپ‌ترین نقطه
    // }

    // public boolean isFrozen() {
    //     return activeEffects.stream().anyMatch(e -> e.getType() == EffectType.FROZEN);
    // }

    // // ----------- Getters / Setters -----------
    // public String getName() { return name; }
    // public double getHealth() { return health; }
    // public double getMaxHealth() { return maxHealth; }
    // public int getLane() { return lane; }
    // public void setLane(int lane) { this.lane = lane; }
    // public double getX() { return xPosition; }
    // public void setX(double x) { this.xPosition = x; }
    // public boolean isDead() { return dead; }
    // public int getWaveCost() { return waveCost; }
    // public List<Armor> getArmors() { return Collections.unmodifiableList(armors); }
    // public List<Effect> getActiveEffects() { return Collections.unmodifiableList(activeEffects); }
    // public Set<String> getTags() { return tags; }
    // public void addTag(String tag) { tags.add(tag); }
    // public boolean hasTag(String tag) { return tags.contains(tag); }

    // // ====================== Inner Classes ======================
    // public static class Armor {
    //     private String name;
    //     private double maxHealth;
    //     private double currentHealth;
    //     private boolean metallic;   // true = can be removed by Magnet-shroom

    //     public Armor(String name, double health, boolean metallic) {
    //         this.name = name;
    //         this.maxHealth = health;
    //         this.currentHealth = health;
    //         this.metallic = metallic;
    //     }

    //     public void damage(double amount) {
    //         currentHealth = Math.max(0, currentHealth - amount);
    //     }

    //     public boolean isBroken() { return currentHealth <= 0; }
    //     public double getHealth() { return currentHealth; }
    //     public boolean isMetallic() { return metallic; }
    //     public String getName() { return name; }
    // }

    // public static class Effect {
    //     private EffectType type;
    //     private double duration;         // کل مدت (بر حسب tick)
    //     private double remainingDuration;

    //     public Effect(EffectType type, double durationInTicks) {
    //         this.type = type;
    //         this.duration = durationInTicks;
    //         this.remainingDuration = durationInTicks;
    //     }

    //     public void tick() {
    //         remainingDuration = Math.max(0, remainingDuration - 1);
    //     }

    //     public boolean isExpired() { return remainingDuration <= 0; }
    //     public EffectType getType() { return type; }
    //     public double getRemainingDuration() { return remainingDuration; }

    //     public void increaseDuration(double extraTicks) {
    //         remainingDuration += extraTicks;
    //     }
    // }

    // public enum EffectType {
    //     CHILLED,    // نصف سرعت
    //     FROZEN,     // نمی‌تواند حرکت کند / حمله کند
    //     HYPNOTIZED, // به زامبی‌های دیگر حمله می‌کند (جزئیات در کنترلر)
    //     // سایر موارد در صورت نیاز اضافه شود
    // }

    // public enum DamageType {
    //     NORMAL,
    //     FIRE,
    //     ICE,
    //     POISON,
    //     MAGIC,
    //     EXPLOSIVE
    // }
}