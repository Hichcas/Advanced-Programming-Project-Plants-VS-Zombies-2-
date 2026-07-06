package com.PVZ.model.entity.zombies.base;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.enums.DamageType;
import com.PVZ.model.game.BattleController;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Zombie {
    protected double x, y, row, col;
    protected String alias;
    protected double hitpoints;
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
    private Rectangle hitbox;
    protected boolean moving = true;
    private static Texture defaultTexture;
    private static boolean textureLoaded = false;

    public Zombie(String alias, double hitpoints, double eatDPS, double speed,
                  int wavePointCost, int weight, List<ScaledProperty> scaledProps) {
        this.alias = alias;
        this.hitpoints = hitpoints;
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
        System.out.println("Zombie spawned: alias=" + alias + " x=" + String.format("%.1f", x) + " row=" + (int)row);
    }

    public void update(float delta, BattleController controller) {
        updateEffects(delta);
        if (hitpoints <= 0 && (armor == null || armor.isDestroyed())) {
            die(controller);
            return;
        }
        int tileCol = controller.getTileColumn((float) x);
        col = tileCol;
        Plant plant = controller.getPlantAt((int) row, tileCol);
        if (plant != null && !plant.isDead()) {
            moving = false;
            attack(plant, delta);
        } else {
            moving = true;
            move(delta, controller);
        }
        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta);
    }

    protected void move(float delta, BattleController controller) {
        x -= currentSpeed * delta * 100;
        if (x <= 0) {
            controller.triggerGameOver();
        }
    }

    protected void attack(Plant targetPlant, float delta) {
        attackCooldownTimer += delta;
        if (attackCooldownTimer >= 1.0f) {
            targetPlant.takeDamage((int) eatDPS);
            if (targetPlant.isDead()) {
                // controller will clean up dead plants
            }
            attackCooldownTimer = 0;
        }
    }

    public void applyEffect(StatusEffect e) {
        activeEffects.add(e);
        if (e.getType() == DamageType.ICE) {
            currentSpeed = speed * 0.5;
        }
    }

    public void freeze(float duration) {
        applyEffect(new StatusEffect(DamageType.ICE, duration));
    }

    public void poison(float duration, float dps) {
        poisonDps = dps;
        activeEffects.add(new StatusEffect(DamageType.POISON, duration));
    }

    public String getStatusString() {
        String armorStr = (armor != null && !armor.isDestroyed())
            ? String.format(" Armor=%s(%.0f)", armor.getType(), armor.getHealth())
            : "";
        String eatStr = moving ? "" : " EATING";
        return String.format("<%s> x=%.1f row=%d col=%d HP=%.1f%s speed=%.3f%s",
            alias, x, (int)row, (int)col, hitpoints, armorStr, currentSpeed, eatStr);
    }

    public void takeDamage(int amount, DamageType type) {
        if (type == DamageType.POISON) {
            hitpoints -= amount;
            return;
        }
        if (type == DamageType.ICE) {
            boolean hasSlow = false;
            for (StatusEffect e : activeEffects) {
                if (e.getType() == DamageType.ICE) { hasSlow = true; break; }
            }
            if (!hasSlow) activeEffects.add(new StatusEffect(DamageType.ICE, 3.0f));
            currentSpeed = speed * 0.5;
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

    private void updateEffects(float delta) {
        Iterator<StatusEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            StatusEffect e = it.next();
            if (e.update(delta)) {
                it.remove();
                if (e.getType() == DamageType.ICE) {
                    currentSpeed = speed;
                }
            }
        }
        for (StatusEffect e : activeEffects) {
            if (e.getType() == DamageType.POISON) {
                hitpoints -= poisonDps * delta;
            }
        }
    }

    public void die(BattleController controller) {
        System.out.println("Zombie [" + alias + "] died at x=" + String.format("%.1f", x) + " row=" + (int)row);
        onDestroy();
        if (isGlowing) {
            controller.addSun(50);
        }
        controller.removeZombie(this);
    }

    public void draw(SpriteBatch batch){
        if(!textureLoaded){
            defaultTexture = new Texture("Zombies/Zombie.png");
            textureLoaded = true;
        }
        batch.draw(defaultTexture, (float) x, (float) y, 50, 70);
    }

    public double getEffectiveHitpoints() {
        double base = hitpoints;
        if (armor != null) {
            base += armor.getHealth();
        }
        return base;
    }

    public void stopMoving() { this.moving = false; }
    public void startMoving() { this.moving = true; }

    public boolean isDead() { return hitpoints <= 0 && (armor == null || armor.isDestroyed()); }
    public abstract void onSpawn();
    public abstract void onUpdate(double deltaTime);
    public abstract void onDestroy();

    public void setArmor(ZombieArmor armor) {
        this.armor = armor;
    }
    public ZombieArmor getArmor() {
        return armor;
    }

    public String getAlias() { return alias; }
    public double getHitpoints() { return hitpoints; }
    public double getEatDPS() { return eatDPS; }
    public double getSpeed() { return speed; }
    public double getCurrentSpeed() { return currentSpeed; }
    public int getWavePointCost() { return wavePointCost; }
    public int getWeight() { return weight; }
    public Rectangle getHitbox() { return hitbox; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRow() { return row; }
    public double getCol() { return col; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setRow(double row) { this.row = row; }
    public void setCol(double col) { this.col = col; }
}
