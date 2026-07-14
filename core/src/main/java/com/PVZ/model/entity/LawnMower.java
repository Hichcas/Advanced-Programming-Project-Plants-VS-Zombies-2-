package com.PVZ.model.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * One per row, parked just to the left of the leftmost tile. Per the design doc: the
 * first zombie to reach it triggers it, it then sweeps across the row killing every
 * zombie in its path, and after that it's spent — if a second zombie reaches this same
 * spot, the game is lost. Coordinates here are real world (screen) pixels, matching
 * Zombie/Plant/Projectile, so its hitbox actually overlaps zombie hitboxes.
 */
public class LawnMower {
    private static final float SIZE = 140f; // doubled from the original 70f so the mower icon reads clearly on screen
    private static final double SPEED = 500;
    private static final String TEXTURE_PATH = "LawnMower/LawnMower.png";

    // shared across all 5 mowers — they all look the same, no need to load/build 5 copies
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
    // fixed footprint of the mower's parked spot — used to detect "a zombie reached the
    // mower" (whether to trigger it, or, if it's already used, to know the player lost).
    // Unlike hitbox, this never moves once the mower starts sweeping across the lawn.
    private final Rectangle parkedZone = new Rectangle();

    /**
     * @param triggerX world x of the mower's parked spot (just left of column 0) —
     *                 also the "zombie reached the house" line once the mower is used.
     * @param travelLimitX world x the mower sweeps to once triggered (right edge of lawn).
     */
    public void init(int row, double parkY, double triggerX, double travelLimitX) {
        this.row = row;
        this.y = parkY;
        this.triggerX = triggerX;
        this.travelLimitX = travelLimitX;
        this.x = triggerX;
        hitbox.set((float) x, (float) y, SIZE, SIZE);
        parkedZone.set((float) x, (float) y, SIZE, SIZE);
    }

    /** Fixed footprint of the mower's parked spot; stays put even after the mower sweeps
     *  off screen, so "did a zombie reach the mower" can always be checked against it. */
    public Rectangle getParkedZone() { return parkedZone; }

    /** X of the mower's front (lawn-facing) edge — the line a zombie coming from the
     *  right crosses FIRST. Triggering (and, once used, losing) is checked against this
     *  line directly instead of a 2D rectangle overlap, so it fires reliably right as the
     *  zombie's leading edge arrives, regardless of any row/height rounding. */
    public double getFrontX() { return triggerX + SIZE; }

    public int getRow() { return row; }
    public double getTriggerX() { return triggerX; }
    public boolean isTriggered() { return triggered; }
    public boolean isUsed() { return used; }

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
            return; // swept all the way across and is gone — the spot just stays empty
        }
        batch.draw(getOrLoadTexture(), (float) x, (float) y, SIZE, SIZE);
    }

    /**
     * Tries assets/LawnMower/LawnMower.png first (put your art there); if it's not found
     * yet, falls back to a generated red-block-with-wheels placeholder so the game still
     * runs and shows *something* in that spot.
     */
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

    public Rectangle getHitbox() { return hitbox; }

    /** Disposes the shared texture — call once when the whole game/level is torn down,
     *  not per-mower (they all share the same Texture instance). */
    public static void disposeSharedTextures() {
        if (sharedTexture != null) {
            sharedTexture.dispose();
            sharedTexture = null;
        }
        triedLoad = false;
    }
}
