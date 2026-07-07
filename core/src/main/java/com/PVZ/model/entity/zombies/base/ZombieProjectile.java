package com.PVZ.model.entity.zombies.base;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class ZombieProjectile {
    private float x, y;
    private float speed;
    private int damage;
    private int row;
    private boolean destroyed;
    private Rectangle hitbox;
    private Texture texture;
    private Zombie owner;

    public ZombieProjectile(float x, float y, int damage, float speed, int row, Zombie owner) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.speed = speed;
        this.row = row;
        this.destroyed = false;
        this.hitbox = new Rectangle(x, y, 24, 24);
        this.owner = owner;

        Pixmap pixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);
        pixmap.setColor(1, 0.2f, 0.2f, 1);
        pixmap.fillCircle(12, 12, 10);
        pixmap.setColor(1, 0.6f, 0.6f, 1);
        pixmap.fillCircle(12, 12, 6);
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta) {
        x -= speed * delta;
        hitbox.setPosition(x, y);
        if (x < -50) destroyed = true;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, 24, 24);
    }

    public Rectangle getHitbox() { return hitbox; }
    public boolean isDestroyed() { return destroyed; }
    public void destroy() { destroyed = true; }
    public int getDamage() { return damage; }
    public int getRow() { return row; }
    public float getX() { return x; }
    public Zombie getOwner() { return owner; }
}
