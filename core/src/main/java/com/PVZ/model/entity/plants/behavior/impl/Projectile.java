package com.PVZ.model.entity.plants.behavior.impl;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;
import java.util.Map;

public class Projectile {
    private ProjectileType type;
    private int damage;
    private int pierce;
    private int lane;
    private int row;
    private int col;
    private double positionX;
    private double speed;
    private boolean fromPlantFood;
    private boolean destroyed = false;
    private Rectangle hitbox = new Rectangle();

    private final Map<String, Object> extras = new HashMap<>();

    public Projectile() {
    }

    public ProjectileType getType() {
        return type;
    }

    public void setType(ProjectileType type) {
        this.type = type;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getPierce() {
        return pierce;
    }

    public void setPierce(int pierce) {
        this.pierce = pierce;
    }

    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isFromPlantFood() {
        return fromPlantFood;
    }

    public void setFromPlantFood(boolean fromPlantFood) {
        this.fromPlantFood = fromPlantFood;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public Object getExtra(String key) {
        return extras.get(key);
    }

    public void putExtra(String key, Object value) {
        extras.put(key, value);
    }

    public void update(float delta) {
        if (destroyed) return;
        positionX += speed * delta;
        hitbox.set((float)positionX, (float)row * 100, 20, 20);
    }

    public void draw(SpriteBatch batch) {}

    public Rectangle getHitbox() { return hitbox; }

    public void hit() { destroyed = true; }

    public boolean isDestroyed() { return destroyed; }
}
