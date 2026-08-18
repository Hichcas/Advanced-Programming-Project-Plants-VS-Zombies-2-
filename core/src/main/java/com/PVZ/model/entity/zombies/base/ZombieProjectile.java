package com.PVZ.model.entity.zombies.base;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class ZombieProjectile {
    protected float x, y;
    protected float speed;
    protected int damage;
    protected int row;
    protected boolean destroyed;
    protected Rectangle hitbox;
    protected Texture texture;
    protected Zombie owner;
    protected boolean landsToTomb = false;
    protected int targetCol = -1;

    public ZombieProjectile(float x, float y, int damage, float speed, int row, Zombie owner) {
        this(x, y, damage, speed, row, owner, -1, false);
    }

    public ZombieProjectile(float x, float y, int damage, float speed, int row, Zombie owner,
                            int targetCol, boolean landsToTomb) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.speed = speed;
        this.row = row;
        this.destroyed = false;
        this.hitbox = new Rectangle(x, y, 24, 24);
        this.owner = owner;
        this.targetCol = targetCol;
        this.landsToTomb = landsToTomb;
    }

    public boolean isLandsToTomb() {
        return landsToTomb;
    }

    public int getTargetCol() {
        return targetCol;
    }

    public void update(float delta) {
        x -= speed * delta;
        hitbox.setPosition(x, y);
        if (x < -50) destroyed = true;
    }

    public void draw(SpriteBatch batch) {
        if (texture == null && com.badlogic.gdx.Gdx.graphics != null && com.badlogic.gdx.Gdx.gl != null) {
            try {
                Pixmap pixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
                if (landsToTomb) {
                    pixmap.setColor(0.95f, 0.95f, 0.85f, 1);
                    pixmap.fillCircle(12, 12, 10);
                    pixmap.setColor(0.7f, 0.7f, 0.6f, 1);
                    pixmap.fillCircle(12, 12, 5);
                } else {
                    pixmap.setColor(1, 0.2f, 0.2f, 1);
                    pixmap.fillCircle(12, 12, 10);
                    pixmap.setColor(1, 0.6f, 0.6f, 1);
                    pixmap.fillCircle(12, 12, 6);
                }
                texture = new Texture(pixmap);
                pixmap.dispose();
            } catch (Exception ignored) { }
        }
        if (texture != null && batch != null) {
            batch.draw(texture, x, y, 24, 24);
        }
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void destroy() {
        destroyed = true;
    }

    public int getDamage() {
        return damage;
    }

    public int getRow() {
        return row;
    }

    public float getX() {
        return x;
    }

    public Zombie getOwner() {
        return owner;
    }
}
