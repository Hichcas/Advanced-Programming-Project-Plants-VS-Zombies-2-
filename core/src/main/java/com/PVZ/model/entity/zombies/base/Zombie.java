package com.PVZ.model.entity.zombies.base;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.enums.DamageType;
import com.PVZ.model.enums.DeathType;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.Map;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.SunManager;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.renderer.EntityRenderer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public abstract class Zombie {
    protected double x, y, row, col;
    protected String alias;
    protected double hitpoints;
    protected double maxHitpoints;
    protected double eatDPS;
    protected double speed, currentSpeed;
    protected int wavePointCost;
    protected int weight;
    protected List<ScaledProperty> scaledProps;
    protected ZombieArmor armor;
    protected float attackCooldownTimer;
    protected boolean isGlowing;
    protected List<StatusEffect> activeEffects;
    private float poisonDps = 10.0f;
    private float chillDuration = 3.0f;
    protected Rectangle hitbox;
    protected boolean moving = true;
    // True for zombies that must hold their tile permanently (e.g. the I,Zombie
    // sun-producing zombie): they never walk toward the brain and never attack a
    // plant, they just idle in place until killed. Distinct from isFrozen(), which
    // is the temporary ice-plant effect and carries its own visual/HP side effects.
    private boolean stationary = false;

    public boolean isStationary() {
        return stationary;
    }

    public void setStationary(boolean stationary) {
        this.stationary = stationary;
    }
    protected boolean hypnotized = false;
    protected int icingLevel = 0;
    protected int iceHp = 0;
    protected float freezeTimer = 0f;
    protected float butterTimer = 0f;
    protected float animStateTime = 0f;
    protected float damageFlashTimer = 0f;
    private final java.util.Map<String, Object> runtimeState = new java.util.HashMap<>();

    public Object getRuntimeState(String key) { return runtimeState.get(key); }
    public void putRuntimeState(String key, Object val) { runtimeState.put(key, val); }

    public boolean hasStatusEffect(DamageType type) {
        if (activeEffects == null) return false;
        for (StatusEffect e : activeEffects) {
            if (e != null && e.getType() == type) {
                return true;
            }
        }
        return false;
    }

    private StatusEffect findEffect(DamageType type) {
        if (activeEffects == null) return null;
        for (StatusEffect e : activeEffects) {
            if (e != null && e.getType() == type) {
                return e;
            }
        }
        return null;
    }

    public Zombie(String alias, double hitpoints, double eatDPS, double speed,
                  int wavePointCost, int weight, List<ScaledProperty> scaledProps) {
        this.alias = alias;
        this.hitpoints = hitpoints;
        this.maxHitpoints = hitpoints;
        this.eatDPS = eatDPS;
        this.speed = speed;
        this.currentSpeed = speed;
        this.wavePointCost = wavePointCost;
        this.weight = weight;
        this.scaledProps = scaledProps;
        this.armor = null;
        this.attackCooldownTimer = 0;
        this.isGlowing = false;
        this.activeEffects = new ArrayList<>();
        this.poisonDps = 10.0f;
        this.hitbox = new Rectangle();
    }

    public void initPosition(double x, double y, double row) {
        this.x = x;
        this.y = y;
        this.row = row;
        this.col = 8;
        hitbox.set((float) x, (float) y, 40, 60);
        System.out.println("Zombie spawned: alias=" + alias + " x=" + String.format("%.1f", x) + " row=" + (int) row);
    }

    private boolean dying = false;

    public void update(float delta, BattleController controller) {
        updateEffects(delta);
        ZombieAnimation.tick(this, delta);

        if (dying) {
            animStateTime += delta;
            if (!ZombieAnimation.isActive(this)) {
                finishDeath(controller);
            }
            return;
        }

        if (!isFrozen()) {
            animStateTime += delta;
        }

        if (hitpoints <= 0 && (armor == null || armor.isDestroyed())) {
            startDeath(controller);
            return;
        }
        if (isFrozen() || isButtered()) {
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, controller);
            return;
        }
        if (hypnotized) {
            updateHypnotized(delta, controller);
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, controller);
            return;
        }
        if (controller == null) {
            return;
        }
        if (stationary) {
            moving = false;
            ZombieAnimation.trigger(this, "idle", 1.0);
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, controller);
            return;
        }
        int tileCol = controller.getTileColumn((float) x);
        col = tileCol;
        Plant plant = controller.getPlantAt((int) row, tileCol);
        if (plant != null && !plant.isDead()) {
            moving = false;
            attack(plant, delta, controller);
        } else {
            moving = true;
            move(delta, controller);
        }
        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta, controller);
    }

    protected void move(float delta, BattleController controller) {
        if (isFrozen()) return;
        ZombieAnimation.trigger(this, "walk", 1.0);
        if (hypnotized) {
            x += currentSpeed * delta * 100;
        } else {
            x -= currentSpeed * delta * 100;
            int hereCol = controller.getTileColumn((float) x);
            TileType tileType = controller.getTileTypeAt((int) row, hereCol);
            if (tileType == TileType.SLIPPERY_UP || tileType == TileType.SLIPPERY_DOWN) {
                x -= currentSpeed * delta * 100;
            }
            if (tileType == TileType.SLIPPERY_UP && row > 0) {
                row -= 1;
                y = y + controller.getMap().getTileHeight();
            } else if (tileType == TileType.SLIPPERY_DOWN && row < 4) {
                row += 1;
                y = y - controller.getMap().getTileHeight();
            }
        }
    }

    protected void attack(Plant targetPlant, float delta, BattleController controller) {
        if (isFrozen()) return;
        if (hypnotized) return;
        ZombieAnimation.trigger(this, "eat", 1.0);
        attackCooldownTimer += delta;
        if (attackCooldownTimer >= 1.0f) {
            targetPlant.takeDamage((int) eatDPS, this, controller);
            if (targetPlant.isDead()) {
            }
            attackCooldownTimer = 0;
        }
    }

    public boolean isHypnotized() {
        return hypnotized;
    }

    protected void updateHypnotized(float delta, BattleController controller) {
        if (isFrozen()) {
            return;
        }
        Zombie target = findEnemyZombieTarget(controller);
        if (target != null) {
            moving = false;
            attackZombie(target, delta);
            return;
        }
        moving = true;
        x += currentSpeed * delta * 100;
        if (controller.getMap() != null) {
            float rightEdge = controller.getMap().getStartX() + controller.getMap().getTotalWidth();
            if (x > rightEdge + 120) {
                controller.removeZombie(this);
            }
        }
    }

    protected void attackZombie(Zombie target, float delta) {
        if (isFrozen() || target == null || target.isDead()) {
            return;
        }
        attackCooldownTimer += delta;
        if (attackCooldownTimer >= 1.0f) {
            target.takeDamage((int) eatDPS, DamageType.NORMAL);
            attackCooldownTimer = 0;
        }
    }

    private Zombie findEnemyZombieTarget(BattleController controller) {
        Zombie best = null;
        double bestDx = Double.MAX_VALUE;
        for (Zombie other : controller.getAllZombies()) {
            if (other == null || other == this || other.isDead() || other.isHypnotized()) {
                continue;
            }
            if ((int) other.getRow() != (int) this.row) {
                continue;
            }
            double dx = other.getX() - this.x;
            if (dx < -20) {
                continue;
            }
            if (Math.abs(dx) <= 70 && Math.abs(other.getY() - this.y) <= 50 && dx < bestDx) {
                bestDx = dx;
                best = other;
            }
        }
        return best;
    }

    public void applyEffect(StatusEffect e) {
        if (e.getType() == DamageType.ICE) {
            StatusEffect existing = findEffect(DamageType.ICE);
            if (existing != null) {
                existing.refresh(e.getDuration());
            } else {
                activeEffects.add(e);
            }
            if (!"FROSTBITE_CAVES".equals(com.PVZ.model.status.AppStatus.currentChapterName)) {
                currentSpeed = speed * 0.5;
            }
            return;
        }
        activeEffects.add(e);
    }

    public void freeze(float duration) {
        this.freezeTimer = Math.max(this.freezeTimer, duration);
        this.icingLevel = 3;
        this.iceHp = Math.max(this.iceHp, 600);
        this.stopMoving();
        applyEffect(new StatusEffect(DamageType.ICE, duration));
    }

    public void butter(float duration) {
        this.butterTimer = Math.max(this.butterTimer, duration);
        this.moving = false;
        this.stopMoving();
    }

    public boolean isButtered() {
        return butterTimer > 0f;
    }

    public float getButterTimer() {
        return butterTimer;
    }

    protected float hitFlashTimer = 0f;

    public boolean isHitFlashing() {
        return hitFlashTimer > 0f;
    }

    public void triggerHitFlash() {
        this.hitFlashTimer = 0.18f;
    }

    public void poison(float duration, float dps) {
        poisonDps = dps;
        activeEffects.add(new StatusEffect(DamageType.POISON, duration));
    }

    public void hypnotize(float duration) {
        hypnotized = true;
        activeEffects.add(new StatusEffect(DamageType.HYPNOTIZE, duration));
    }

    public String getStatusString() {
        String armorStr = (armor != null && !armor.isDestroyed())
            ? String.format(" Armor=%s(%.0f)", armor.getType(), armor.getHealth())
            : "";
        String eatStr = moving ? "" : " EATING";
        return String.format("<%s> x=%.1f row=%d col=%d HP=%.1f%s speed=%.3f%s",
            alias, x, (int) row, (int) col, hitpoints, armorStr, currentSpeed, eatStr);
    }

    public boolean isMoving() {
        return moving;
    }

    public void takeDamage(int amount, DamageType type) {
        if (type == DamageType.POISON) {
            hitpoints -= amount;
            return;
        }
        if (type == DamageType.FIRE) {
            if (isFireImmune()) {
                return;
            }
            if (isFrozen()) {
                thaw();
            }
            if (hitpoints - amount <= 0) {
                deathType = DeathType.ASH;
            }
        }
        if (type == DamageType.ICE) {
            StatusEffect existing = findEffect(DamageType.ICE);
            if (existing != null) {
                // Re-hitting an already-slowed zombie should refresh the slow,
                // not leave the earlier (possibly near-expiry) timer running -
                // otherwise the zombie can pop back to full speed moments after
                // being hit a second time.
                existing.refresh(chillDuration);
            } else {
                activeEffects.add(new StatusEffect(DamageType.ICE, chillDuration));
            }
            if (!"FROSTBITE_CAVES".equals(com.PVZ.model.status.AppStatus.currentChapterName)) {
                currentSpeed = speed * 0.5;
            }
        }
        hitFlashTimer = 0.18f;
        if (armor != null && !armor.isDestroyed()) {
            armor.takeDamage(amount);
            if (armor.isDestroyed() && armor.isDroppable()) {
                com.PVZ.view.renderer.EntityRenderer.getInstance().spawnFallingArmor((float) x, (float) y, armor.getType(), alias);
                armor = null;
            }
        } else {
            hitpoints -= amount;
            if (hitpoints <= 0) {
                if (deathType == DeathType.ASH || deathType == DeathType.ELECTRIC) {
                    ZombieAnimation.trigger(this, "die", 0.05);
                } else {
                    ZombieAnimation.trigger(this, "die", 2.8333);
                }
            }
        }
    }

    public void takeDamage(double damage) {
        takeDamage((int) damage, DamageType.NORMAL);
    }

    protected DeathType deathType = DeathType.NORMAL;

    public DeathType getDeathType() {
        return deathType;
    }

    public void setDeathType(DeathType deathType) {
        this.deathType = deathType;
    }

    public String getAshPamPath(boolean isElectric) {
        String lower = alias == null ? "" : alias.toLowerCase();
        boolean isImp = lower.contains("imp");
        boolean isGarg = lower.contains("gargantuar");
        boolean isBalloon = lower.contains("balloon");

        if (isElectric) {
            if (isImp) return "768/INITIAL/EFFECTS/ZOMBIE_IMP_SHOCK/ZOMBIE_IMP_SHOCK.PAM";
            if (isGarg) return "768/INITIAL/EFFECTS/ZOMBIE_GARGANTUAR_SHOCK/ZOMBIE_GARGANTUAR_SHOCK.PAM";
            if (isBalloon) return "768/INITIAL/EFFECTS/ZOMBIE_MODERN_BALLOON_SHOCK/ZOMBIE_MODERN_BALLOON_SHOCK.PAM";
            return "768/INITIAL/EFFECTS/ZOMBIE_SHOCK/ZOMBIE_SHOCK.PAM";
        } else {
            if (isImp) return "768/INITIAL/EFFECTS/ZOMBIE_IMP_ASH/ZOMBIE_IMP_ASH.PAM";
            if (isGarg) return "768/INITIAL/EFFECTS/ZOMBIE_GARGANTUAR_ASH/ZOMBIE_GARGANTUAR_ASH.PAM";
            if (isBalloon) return "768/INITIAL/EFFECTS/ZOMBIE_MODERN_BALLOON_ASH/ZOMBIE_MODERN_BALLOON_ASH.PAM";
            return "768/INITIAL/EFFECTS/ZOMBIE_ASH/ZOMBIE_ASH.PAM";
        }
    }

    public void updateEffects(float delta) {
        Iterator<StatusEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            StatusEffect e = it.next();
            if (e.update(delta)) {
                it.remove();
            }
        }
        if (freezeTimer > 0f) {
            freezeTimer -= delta;
            if (freezeTimer <= 0f) {
                freezeTimer = 0f;
                if (icingLevel >= 3) {
                    thaw();
                }
            }
        }
        if (butterTimer > 0f) {
            butterTimer -= delta;
            if (butterTimer < 0f) {
                butterTimer = 0f;
            }
        }
        if (hitFlashTimer > 0f) {
            hitFlashTimer -= delta;
            if (hitFlashTimer < 0f) {
                hitFlashTimer = 0f;
            }
        }
        // Only clear the slow/hypnosis once nothing of that type remains active -
        // dedupe+refresh above means there's normally at most one ICE entry, but
        // this stays correct even if something else ever stacks a second one.
        if (!hasStatusEffect(DamageType.ICE)) {
            currentSpeed = speed;
        }
        if (!hasStatusEffect(DamageType.HYPNOTIZE)) {
            hypnotized = false;
        }
        for (StatusEffect e : activeEffects) {
            if (e.getType() == DamageType.POISON) {
                hitpoints -= poisonDps * delta;
            }
        }
        if (isFrozen()) {
            if (iceHp > 0) {
                iceHp -= (int) (delta * 60);
                if (iceHp <= 0 && freezeTimer <= 0f) {
                    thaw();
                }
            }
        }
    }

    public void startDeath(BattleController controller) {
        if (dying) return;
        dying = true;
        hitpoints = 0;
        armor = null;
        animStateTime = 0.0f;

        if (deathType == DeathType.ASH || deathType == DeathType.ELECTRIC) {
            boolean isElectric = (deathType == DeathType.ELECTRIC);
            String ashPam = getAshPamPath(isElectric);
            float duration = isElectric ? 1.33f : 3.5f;
            RegularGameEngine reg = AppStatus.getGameEngine() instanceof RegularGameEngine re ? re : null;
            if (reg != null && ashPam != null) {
                reg.addTimedPamEffect(ashPam, "animation", duration, 1.0f, (float) x, (float) y);
            }
            ZombieAnimation.trigger(this, "die", 0.05);
        } else {
            ZombieAnimation.trigger(this, "die", 2.8333);
        }

        if (isGlowing) {
            controller.grantPlantFoodDrop(x, y);
        }
        controller.rollLootDrop(x, y);
    }

    public void finishDeath(BattleController controller) {
        System.out.println("Zombie [" + alias + "] died at x=" + String.format("%.1f", x) + " row=" + (int) row);
        onDestroy();
        controller.removeZombie(this);
    }

    public void die(BattleController controller) {
        startDeath(controller);
        finishDeath(controller);
    }

    public boolean isGlowing() {
        return isGlowing;
    }

    public void setGlowing(boolean glowing) {
        this.isGlowing = glowing;
    }

    public void draw(SpriteBatch batch) {
        EntityRenderer.getInstance().renderZombie(batch, this, animStateTime);
    }

    public double getEffectiveHitpoints() {
        double base = hitpoints;
        if (armor != null) {
            base += armor.getHealth();
        }
        return base;
    }

    public String getDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HP:").append((int) hitpoints);
        sb.append(" (").append((int) x).append(",").append((int) y).append(")");
        sb.append(" R:").append((int) row).append(" C:").append((int) col);
        sb.append("\n");
        sb.append("SPD:").append(String.format("%.2f", currentSpeed));
        if (isFrozen()) {
            sb.append(" [FROZEN iceHp:").append(iceHp).append("]");
        }
        for (StatusEffect e : activeEffects) {
            sb.append(" [").append(e.getType()).append(":").append(String.format("%.1f", e.getDuration())).append("s]");
        }
        return sb.toString();
    }

    public void stopMoving() {
        this.moving = false;
    }

    public void startMoving() {
        this.moving = true;
    }

    public boolean isDying() {
        return dying;
    }

    public boolean isDead() {
        return dying || (hitpoints <= 0 && (armor == null || armor.isDestroyed()));
    }

    public boolean isProjectileImmune() {
        return false;
    }

    public boolean isFireImmune() {
        return alias != null && (alias.toLowerCase().contains("dragon") || alias.toLowerCase().contains("impdragon"));
    }

    public void onProjectileHit(Plant target) {
    }

    /**
     * Scales both current and max HP by a flat multiplier, independent of the
     * difficulty-level scaling table (used e.g. to make the I,Zombie sun-producing
     * zombie a tanky, hard-to-kill fixture rather than a normal disposable unit).
     */
    public void buffHitpoints(double multiplier) {
        if (multiplier <= 0) return;
        hitpoints *= multiplier;
        maxHitpoints *= multiplier;
    }

    public void applyDifficultyScaling(int level) {
        if (level < 1) level = 1;
        if (level > 5) level = 5;
        double armorScale = 1.0;
        for (ScaledProperty prop : scaledProps) {
            double scale = prop.computeScale(level);
            switch (prop.getKey()) {
                case "Hitpoints" -> {
                    hitpoints *= scale;
                    maxHitpoints *= scale;
                    armorScale = scale;
                }
                case "EatDPS" -> eatDPS *= scale;
                case "Speed" -> {
                    speed *= scale;
                    currentSpeed *= scale;
                }
                case "WavePointCost" -> wavePointCost = (int) (wavePointCost * scale);
                default -> applyCustomScaledProperty(prop.getKey(), scale);
            }
        }
        if (armor != null) armor.scaleHealth(armorScale);
    }

    protected void applyCustomScaledProperty(String key, double scale) {
    }

    public abstract void onSpawn();

    public void onUpdate(float delta, BattleController controller) {
    }

    public abstract void onDestroy();

    public void setArmor(ZombieArmor armor) {
        this.armor = armor;
    }

    public ZombieArmor getArmor() {
        return armor;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public double getHitpoints() {
        return hitpoints;
    }

    public void setHitpoints(double hp) {
        this.hitpoints = hp;
    }

    public double getMaxHitpoints() {
        return maxHitpoints;
    }

    public double getEatDPS() {
        return eatDPS;
    }

    public double getSpeed() {
        return speed;
    }

    public double getCurrentSpeed() {
        if (isFrozen()) {
            return 0.0;
        }
        return currentSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public int getWavePointCost() {
        return wavePointCost;
    }

    public int getWeight() {
        return weight;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRow() {
        return row;
    }

    public double getCol() {
        return col;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setRow(double row) {
        this.row = row;
    }

    public void setCol(double col) {
        this.col = col;
    }

    public void stealNearbySun(SunManager sunManager) {
    }

    public void burnPlantsAhead(Map map, BattleController controller) {
    }

    public void maybeSpawnGraves(Map map, Random random) {
    }

    public int getIcingLevel() {
        return icingLevel;
    }

    public void setIcingLevel(int l) {
        this.icingLevel = l;
    }

    public int getIceHp() {
        return iceHp;
    }

    public void setIceHp(int hp) {
        this.iceHp = hp;
    }

    public boolean isFrozen() {
        return freezeTimer > 0f || icingLevel >= 3;
    }

    public void freezeSolid() {
        this.freezeTimer = 5.0f;
        this.icingLevel = 3;
        this.iceHp = 600;
        this.stopMoving();
    }

    public void thaw() {
        this.freezeTimer = 0f;
        this.icingLevel = 0;
        this.iceHp = 0;
        this.startMoving();
    }

    public void stunOnHit() {
        this.freezeSolid();
    }

    public void setChillDuration(float chillDuration) {
        this.chillDuration = chillDuration;
    }
}
