package com.PVZ.model.entity.zombies.base;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.ZombieEngine;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

public abstract class Zombie {
    protected double x, y, row, col;
      protected String alias;
      protected double hitpoints;
      protected double eatDPS;
      protected double speed;
      protected int wavePointCost;
      protected int weight;
      protected List<ScaledProperty> scaledProps;
      protected ZombieArmor armor;
      private Rectangle hitbox;
    protected boolean moving = true;

      public Zombie(String alias, double hitpoints, double eatDPS, double speed,
                    int wavePointCost, int weight, List<ScaledProperty> scaledProps) {
            this.alias = alias;
            this.hitpoints = hitpoints;
            this.eatDPS = eatDPS;
            this.speed = speed;
            this.wavePointCost = wavePointCost;
            this.weight = weight;
            this.scaledProps = scaledProps;
            this.armor = null;
            this.hitbox = new Rectangle();
      }

    public void initPosition(double x, double y, double row) {
        this.x = x;
        this.y = y;
        this.row = row;
        this.col = 8;
        hitbox.set((float) x, (float) y, 40, 60);
    }

    public void update(float delta, ZombieEngine engine) {
        if (!moving) return;

        x -= speed * delta;
        col = 8 - (int) (x / 100);
        hitbox.setPosition((float) x, (float) y);

        int tileCol = (int) (x / 100) + 1;
        Plant plant = engine.getPlantAt((int) row, tileCol);

        if (plant != null) {
            moving = false;
            engine.takeDamage(plant, eatDPS * delta);
        } else {
            moving = true;
        }

        onUpdate(delta);

        if (x <= -50) {
            engine.kill(this);
        }
    }

    public void draw(SpriteBatch batch){}

      public void setArmor(ZombieArmor armor) {
            this.armor = armor;
      }

      public ZombieArmor getArmor() {
            return armor;
      }

      public double getEffectiveHitpoints() {
            double base = hitpoints;
            if (armor != null) {
                  base += armor.getHealth();
            }
            return base;
      }

    public void takeDamage(double damage) {
        if (armor != null && !armor.isDestroyed()) {
            armor.takeDamage(damage);
            if (armor.isDestroyed() && armor.isDroppable()) {
                armor = null;
            }
        } else {
            hitpoints -= damage;
        }
        if (hitpoints <= 0) {
            // mark for removal
        }
    }

    public boolean isDead() { return hitpoints <= 0 && (armor == null || armor.isDestroyed()); }
      public abstract void onSpawn();
      public abstract void onUpdate(double deltaTime);
      public abstract void onDestroy();

      public String getAlias() { return alias; }
      public double getHitpoints() { return hitpoints; }
      public double getEatDPS() { return eatDPS; }
      public double getSpeed() { return speed; }
      public int getWavePointCost() { return wavePointCost; }
      public int getWeight() { return weight; }
    public Rectangle getHitbox() { return hitbox; }

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
}
