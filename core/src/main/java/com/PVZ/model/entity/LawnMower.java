package com.PVZ.model.entity;

import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.game.ZombieEngine;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class LawnMower {
    private double x;
    private int row;
    private boolean triggered = false;
    private boolean used = false;
    private Rectangle hitbox = new Rectangle();
    private static final double SPEED = 300;

    public void init(int row, double x) {
        this.row = row;
        this.x = x;
        hitbox.set((float) x, row * 100f + 20, 60, 60);
    }

    public void update(float delta, ZombieEngine engine) {
        if (used) return;

        if (!triggered) {

            for (Zombie z : engine.getZombiesInLane(row)) {
                if (z.getX() < 150) {
                    triggered = true;
                    break;
                }
            }
        } else {
            x += SPEED * delta;
            hitbox.setPosition((float) x, row * 100f + 20);

            // kill zombies in this row
            for (Zombie z : engine.getZombiesInLane(row)) {
                if (hitbox.overlaps(z.getHitbox())) {
                    engine.kill(z);
                }
            }

            if (x > 1000) used = true;
        }
    }

    public void draw(SpriteBatch batch) {}

    public Rectangle getHitbox() { return hitbox; }
    public boolean isUsed() { return used; }
    public boolean isTriggered() { return triggered; }
}
