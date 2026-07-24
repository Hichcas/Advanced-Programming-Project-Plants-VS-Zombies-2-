package com.PVZ.model.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Sun {
    private static final String TEXTURE_PATH = "Sun/Sun.png";
    private static Texture sharedTexture;
    private static boolean triedLoad = false;

    private static final float SIZE = 70f;
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

    public enum SunType {NORMAL, SPECIAL, RADIOACTIVE}

    private SunType type = SunType.NORMAL;

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
                if (type == SunType.RADIOACTIVE) {
                    type = SunType.NORMAL;
                    amount = 25;
                }
            }
            updateHitbox();
        }
    }

    public void draw(SpriteBatch batch) {
        if (collected) {
            return;
        }
        Texture texture = getOrLoadTexture();
        batch.draw(texture, (float) x, (float) y, hitbox.width, hitbox.height);
    }

    private static Texture getOrLoadTexture() {
        if (sharedTexture != null) {
            return sharedTexture;
        }
        if (!triedLoad) {
            triedLoad = true;
            try {
                if (Gdx.files.internal(TEXTURE_PATH).exists()) {
                    sharedTexture = new Texture(Gdx.files.internal(TEXTURE_PATH));
                    return sharedTexture;
                }
                System.out.println("[Sun] no icon found at assets/" + TEXTURE_PATH
                    + " -> falling back to placeholder circle");
            } catch (RuntimeException ex) {
                System.out.println("[Sun] failed loading texture at assets/" + TEXTURE_PATH + " -> " + ex.getMessage());
            }
        }
        sharedTexture = buildPlaceholderTexture();
        return sharedTexture;
    }

    private static Texture buildPlaceholderTexture() {
        int size = (int) SIZE;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.GOLD);
        pixmap.fillCircle(size / 2, size / 2, size / 2 - 1);
        pixmap.setColor(1f, 1f, 0.6f, 0.8f);
        pixmap.fillCircle(size / 2, size / 2, size / 4);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void updateHitbox() {
        hitbox.set((float) x, (float) y, SIZE, SIZE);
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

    public SunType getType() {
        return type;
    }

    public void setType(SunType type) {
        this.type = type;
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
