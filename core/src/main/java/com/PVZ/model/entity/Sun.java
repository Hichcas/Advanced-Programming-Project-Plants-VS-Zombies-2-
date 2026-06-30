package com.PVZ.model.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Sun {
    private double x, y;
    private int amount;
    private float timer = 0;
    private static final float LIFETIME = 10f;
    private boolean collected = false;
    private Rectangle hitbox = new Rectangle();

    public Sun(double x, double y, int amount) {
        this.x = x;
        this.y = y;
        this.amount = amount;
        hitbox.set((float) x, (float) y, 40, 40);
    }

    public void update(float delta) {
        timer += delta;
        if (timer >= LIFETIME) collected = true;
    }

    public void draw(SpriteBatch batch) {}

    public Rectangle getHitbox() { return hitbox; }
    public int getAmount() { return amount; }
    public boolean isCollected() { return collected; }
    public void collect() { collected = true; }
}
