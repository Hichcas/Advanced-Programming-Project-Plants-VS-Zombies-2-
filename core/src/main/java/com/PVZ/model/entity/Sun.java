package com.PVZ.model.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Sun {
    private double x;
    private double y;
    private int amount;
    private float timer = 0f;
    private static final float LIFETIME = 10f;
    private boolean collected = false;
    private boolean falling = false;
    private boolean reachedGround = false;
    private boolean groundNotified = false;
    private double fallSpeed = 180.0;
    private double groundY = 0.0;
    private final Rectangle hitbox = new Rectangle();

    public Sun(double x, double y, int amount) {
        this.x = x;
        this.y = y;
        this.amount = amount;
        updateHitbox();
    }

    public void configureFalling(double fallSpeed, double groundY) {
        this.falling = true;
        this.fallSpeed = fallSpeed;
        this.groundY = groundY;
    }

    public void update(float delta) {
        if (collected) {
            return;
        }

        timer += delta;
        if (timer >= LIFETIME) {
            collected = true;
            return;
        }

        if (falling && !reachedGround) {
            y -= fallSpeed * delta;
            if (y <= groundY) {
                y = groundY;
                reachedGround = true;
            }
            updateHitbox();
        }
    }

    public void draw(SpriteBatch batch) {
    }

    private void updateHitbox() {
        hitbox.set((float) x, (float) y, 40, 40);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }

    public boolean isFalling() {
        return falling;
    }

    public boolean hasReachedGround() {
        return reachedGround;
    }

    public boolean isGroundNotified() {
        return groundNotified;
    }

    public void setGroundNotified(boolean groundNotified) {
        this.groundNotified = groundNotified;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        updateHitbox();
    }
}
