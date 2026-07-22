package com.PVZ.model.entity.plants.behavior.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.HashMap;
import java.util.Map;

public class Projectile {
    private static final float SIZE = 22f;

    private ProjectileType type;
    private int damage;
    private int pierce;
    private int lane;
    private int row;
    private int col;
    private double positionX;
    private double positionY;
    private double speed;
    private double verticalSpeed;
    private boolean fromPlantFood;
    private boolean destroyed = false;
    private boolean worldPositioned = false;
    private Rectangle hitbox = new Rectangle();
    private Texture texture;

    // Arc (lobbed) flight state: pult-type plants (Cabbage-pult, Melon-pult, Kernel-pult,
    // Winter Melon, Pepper-pult...) throw their payload up and over obstacles instead of
    // shooting it flat, so it needs to rise then come back down rather than moving in a
    // straight line like a peashooter pea.
    private boolean arcMotion = false;
    private double baseY;
    private double arcElapsed;
    private double arcDuration = 0.9;
    private double arcHeight = 150.0;

    private final Map<String, Object> extras = new HashMap<>();

    public Projectile() {
    }

    public void initWorldPosition(float worldX, float worldY, float worldSpeedPxPerSec) {
        initWorldPosition(worldX, worldY, worldSpeedPxPerSec, 0.0f);
    }

    public void initWorldPosition(float worldX, float worldY, float worldSpeedPxPerSec,
            float worldVerticalSpeedPxPerSec) {
        this.positionX = worldX;
        this.positionY = worldY;
        this.speed = worldSpeedPxPerSec;
        this.verticalSpeed = worldVerticalSpeedPxPerSec;
        this.worldPositioned = true;
        this.arcMotion = false;
        this.hitbox.set((float) positionX, (float) positionY, SIZE, SIZE);
    }

    /**
     * Positions a lobbed projectile that should visibly arc up then back down onto its
     * landing row (a parabola in screen space) instead of flying in a straight line, while
     * still moving toward the target lane at {@code worldSpeedPxPerSec}.
     */
    public void initArcPosition(float worldX, float worldY, float worldSpeedPxPerSec) {
        this.positionX = worldX;
        this.positionY = worldY;
        this.baseY = worldY;
        this.speed = worldSpeedPxPerSec;
        this.verticalSpeed = 0.0;
        this.worldPositioned = true;
        this.arcMotion = true;
        this.arcElapsed = 0.0;
        this.hitbox.set((float) positionX, (float) positionY, SIZE, SIZE);
    }

    public boolean isWorldPositioned() {
        return worldPositioned;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
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

    public double getVerticalSpeed() {
        return verticalSpeed;
    }

    public void setVerticalSpeed(double verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
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
        if (destroyed || !worldPositioned) {
            // not placed on the map yet (engine hasn't called initWorldPosition) — do nothing
            return;
        }
        positionX += speed * delta;
        if (arcMotion) {
            arcElapsed += delta;
            // A simple sine arc: 0 at launch, peaks at arcHeight halfway through the flight,
            // back to baseY (ground level) at the end — so a lobbed shot visibly rises and
            // then comes back down instead of drifting upward forever or flying flat.
            double t = Math.min(1.0, arcElapsed / arcDuration);
            positionY = baseY + arcHeight * Math.sin(Math.PI * t);
        } else {
            positionY += verticalSpeed * delta;
        }
        hitbox.setPosition((float) positionX, (float) positionY);
    }

    public void draw(SpriteBatch batch) {
        if (destroyed || !worldPositioned) {
            return;
        }
        if (texture == null) {
            texture = buildTexture();
        }
        batch.draw(texture, (float) positionX, (float) positionY, SIZE, SIZE);
    }

    private Texture buildTexture() {
        Pixmap pixmap = new Pixmap((int) SIZE, (int) SIZE, Pixmap.Format.RGBA8888);
        Color color = colorForType();
        pixmap.setColor(color);
        pixmap.fillCircle((int) (SIZE / 2), (int) (SIZE / 2), (int) (SIZE / 2) - 1);
        pixmap.setColor(1f, 1f, 1f, 0.6f);
        pixmap.fillCircle((int) (SIZE / 2), (int) (SIZE / 2), (int) (SIZE / 4));
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Color colorForType() {
        if (type == null) {
            return new Color(0.4f, 0.8f, 0.3f, 1f);
        }
        return switch (type) {
            case FIRE_PEA -> new Color(1f, 0.4f, 0.1f, 1f);
            case ICE_PEA -> new Color(0.3f, 0.7f, 1f, 1f);
            case LOB -> new Color(0.8f, 0.6f, 0.2f, 1f);
            case BOMB -> new Color(0.2f, 0.2f, 0.2f, 1f);
            case BEAM -> new Color(1f, 1f, 0.3f, 1f);
            case SUN -> new Color(1f, 0.85f, 0.1f, 1f);
            case SEED -> new Color(0.6f, 0.4f, 0.2f, 1f);
            default -> new Color(0.4f, 0.8f, 0.3f, 1f);
        };
    }

    public Rectangle getHitbox() { return hitbox; }

    public void hit() { destroyed = true; }

    public boolean isDestroyed() { return destroyed; }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }
}
