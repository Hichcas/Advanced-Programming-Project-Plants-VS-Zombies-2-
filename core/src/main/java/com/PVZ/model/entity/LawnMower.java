package com.PVZ.model.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class LawnMower {
    private static final float SIZE = 140f;
    private static final double SPEED = 500;
    private static final String TEXTURE_PATH = "LawnMower/LawnMower.png";
    private static Texture sharedTexture;
    private static boolean triedLoad = false;
    private double x;
    private double y;
    private double triggerX;
    private double travelLimitX;
    private int row;
    private boolean triggered = false;
    private boolean used = false;
    private final Rectangle hitbox = new Rectangle();
    private final Rectangle parkedZone = new Rectangle();

    public void init(int row, double parkY, double triggerX, double travelLimitX) {
        this.row = row;
        this.y = parkY;
        this.triggerX = triggerX;
        this.travelLimitX = travelLimitX;
        this.x = triggerX;
        hitbox.set((float) x, (float) y, SIZE, SIZE);
        parkedZone.set((float) x, (float) y, SIZE, SIZE);
    }

    public Rectangle getParkedZone() {
        return parkedZone;
    }

    public double getFrontX() {
        return triggerX + SIZE;
    }

    public int getRow() {
        return row;
    }

    public double getTriggerX() {
        return triggerX;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public boolean isUsed() {
        return used;
    }

    public void trigger() {
        triggered = true;
    }

    public void advance(float delta) {
        if (!triggered || used) {
            return;
        }
        x += SPEED * delta;
        hitbox.setPosition((float) x, (float) y);
        if (x > travelLimitX) {
            used = true;
        }
    }

    public void draw(SpriteBatch batch) {
        if (used) {
            return;
        }
        batch.draw(getOrLoadTexture(), (float) x, (float) y, SIZE, SIZE);
    }


    private static Texture getOrLoadTexture() {
        ensureLoaded();
        return sharedTexture;
    }

    private static void ensureLoaded() {
        if (sharedTexture != null) {
            return;
        }
        if (!triedLoad) {
            triedLoad = true;
            try {
                if (Gdx.files.internal(TEXTURE_PATH).exists()) {
                    sharedTexture = new Texture(Gdx.files.internal(TEXTURE_PATH));
                } else {
                    System.out.println("[LawnMower] no icon found at assets/" + TEXTURE_PATH
                        + " -> falling back to placeholder block");
                }
            } catch (RuntimeException ex) {
                System.out.println("[LawnMower] failed loading texture -> " + ex.getMessage());
            }
        }
        if (sharedTexture == null) {
            sharedTexture = buildPlaceholderTexture(new Color(0.85f, 0.15f, 0.15f, 1f));
        }
    }

    private static Texture buildPlaceholderTexture(Color bodyColor) {
        int s = (int) SIZE;
        Pixmap pixmap = new Pixmap(s, s, Pixmap.Format.RGBA8888);
        pixmap.setColor(bodyColor);
        pixmap.fillRectangle(0, s / 3, s, s * 2 / 3);
        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fillCircle(s / 4, s - 4, 6);
        pixmap.fillCircle(s * 3 / 4, s - 4, 6);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public static void disposeSharedTextures() {
        if (sharedTexture != null) {
            sharedTexture.dispose();
            sharedTexture = null;
        }
        triedLoad = false;
    }
}
