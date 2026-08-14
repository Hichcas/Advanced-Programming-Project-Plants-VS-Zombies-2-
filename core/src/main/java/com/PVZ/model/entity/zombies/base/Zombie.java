package com.PVZ.model.entity.zombies.base;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.enums.DamageType;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.Map;
import com.PVZ.model.game.SunManager;
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
    protected boolean hypnotized = false;
    protected int icingLevel = 0;
    protected int iceHp = 0;
    protected float animStateTime = 0f;
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

    public void update(float delta, BattleController controller) {
        updateEffects(delta);
        ZombieAnimation.tick(this, delta);
        if (!isFrozen()) {
            animStateTime += delta;
        }
        if (hitpoints <= 0 && (armor == null || armor.isDestroyed())) {
            die(controller);
            return;
        }
        if (isFrozen()) {
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
        activeEffects.add(e);
        if (e.getType() == DamageType.ICE) {
            if (!"FROSTBITE_CAVES".equals(com.PVZ.model.status.AppStatus.currentChapterName)) {
                currentSpeed = speed * 0.5;
            }
        }
    }

    public void freeze(float duration) {
        applyEffect(new StatusEffect(DamageType.ICE, duration));
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

    public void takeDamage(int amount, DamageType type) {
        if (type == DamageType.POISON) {
            hitpoints -= amount;
            return;
        }
        if (type == DamageType.ICE) {
            boolean hasSlow = false;
            for (StatusEffect e : activeEffects) {
                if (e.getType() == DamageType.ICE) {
                    hasSlow = true;
                    break;
                }
            }
            if (!hasSlow) activeEffects.add(new StatusEffect(DamageType.ICE, chillDuration));
            if (!"FROSTBITE_CAVES".equals(com.PVZ.model.status.AppStatus.currentChapterName)) {
                currentSpeed = speed * 0.5;
            }
        }
        if (armor != null && !armor.isDestroyed()) {
            armor.takeDamage(amount);
            if (armor.isDestroyed() && armor.isDroppable()) {
                armor = null;
            }
        } else {
            hitpoints -= amount;
        }
    }

    public void takeDamage(double damage) {
        takeDamage((int) damage, DamageType.NORMAL);
    }

    public void updateEffects(float delta) {
        Iterator<StatusEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            StatusEffect e = it.next();
            if (e.update(delta)) {
                it.remove();
                if (e.getType() == DamageType.ICE) {
                    currentSpeed = speed;
                }
                if (e.getType() == DamageType.HYPNOTIZE) {
                    hypnotized = false;
                }
            }
        }
        for (StatusEffect e : activeEffects) {
            if (e.getType() == DamageType.POISON) {
                hitpoints -= poisonDps * delta;
            }
        }
        if (isFrozen()) {
            iceHp -= (int) (delta * 60);
            if (iceHp <= 0) {
                thaw();
            }
        }
    }

    public void die(BattleController controller) {
        hitpoints = 0;
        armor = null;
        System.out.println("Zombie [" + alias + "] died at x=" + String.format("%.1f", x) + " row=" + (int) row);
        onDestroy();
        if (isGlowing) {
            controller.grantPlantFoodDrop();
        }
        controller.rollLootDrop(x, y);
        controller.removeZombie(this);
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

    public boolean isDead() {
        return hitpoints <= 0 && (armor == null || armor.isDestroyed());
    }

    public boolean isProjectileImmune() {
        return false;
    }

    public void onProjectileHit(Plant target) {
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
        return icingLevel >= 3;
    }

    public void freezeSolid() {
        this.icingLevel = 3;
        this.iceHp = 600;
        this.stopMoving();
    }

    public void thaw() {
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
