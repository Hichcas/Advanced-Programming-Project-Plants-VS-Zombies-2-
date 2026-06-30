package com.PVZ.model.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class Projectile {
    private double positionX,  positionY, row, col;
    private boolean destroyed = false;
    private Rectangle hitbox = new Rectangle();
    private double speed;

    public void update(float delta) {
        if (destroyed) return;
        positionX += speed * delta;
        hitbox.set((float)positionX, (float)row * 100, 20, 20);
    }

    public void draw(SpriteBatch batch) { }

    public Rectangle getHitbox() { return hitbox; }
    public void hit() { destroyed = true; }
    public boolean isDestroyed() { return destroyed; }

}
