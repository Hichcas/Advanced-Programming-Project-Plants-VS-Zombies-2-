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
    private boolean arcMotion = false;
    private double baseY;
    private double arcElapsed;
    private double arcDuration = 0.9;
    private double arcHeight = 150.0;
    private boolean freeMotion = false;
    private double velX = 0.0;
    private double velY = 0.0;
    private boolean homing = false;
    private boolean bouncing = false;
    private double fuse = -1.0;
    private double boundMinX = 0, boundMaxX = 0, boundMinY = 0, boundMaxY = 0;
    private boolean fuseExploded = false;

    private final Map<String, Object> extras = new HashMap<>();
    private float animTime = 0f;

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

    public void initFreePosition(float worldX, float worldY, float vx, float vy) {
        this.positionX = worldX;
        this.positionY = worldY;
        this.baseY = worldY;
        this.velX = vx;
        this.velY = vy;
        this.speed = 0.0;
        this.verticalSpeed = 0.0;
        this.freeMotion = true;
        this.arcMotion = false;
        this.worldPositioned = true;
        this.hitbox.set(worldX, worldY, SIZE, SIZE);
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
            return;
        }
        animTime += delta;
        if (freeMotion) {
            updateFreeMotion(delta);
            return;
        }
        positionX += speed * delta;
        if (arcMotion) {
            arcElapsed += delta;
            double t = Math.min(1.0, arcElapsed / arcDuration);
            positionY = baseY + arcHeight * Math.sin(Math.PI * t);
        } else {
            positionY += verticalSpeed * delta;
        }
        hitbox.setPosition((float) positionX, (float) positionY);
    }

    private void updateFreeMotion(float delta) {
        positionX += velX * delta;
        positionY += velY * delta;
        if (bouncing) {
            if (boundMaxX > boundMinX) {
                if (positionX < boundMinX) {
                    positionX = boundMinX;
                    velX = Math.abs(velX);
                } else if (positionX > boundMaxX) {
                    positionX = boundMaxX;
                    velX = -Math.abs(velX);
                }
            }
            if (boundMaxY > boundMinY) {
                if (positionY < boundMinY) {
                    positionY = boundMinY;
                    velY = Math.abs(velY);
                } else if (positionY > boundMaxY) {
                    positionY = boundMaxY;
                    velY = -Math.abs(velY);
                }
            }
        }

        if (fuse >= 0.0) {
            fuse -= delta;
            if (fuse <= 0.0) {
                fuseExploded = true;
            }
        }
        hitbox.setPosition((float) positionX, (float) positionY);
    }

    public void draw(SpriteBatch batch) {
        if (destroyed || !worldPositioned) {
            return;
        }
        Object visualKey = extras.get("visualKey");
        if (visualKey instanceof String key) {
            boolean drewAnimated = com.PVZ.view.renderer.EntityRenderer.getInstance()
                .renderProjectile(batch, key, animTime, (float) positionX, (float) positionY);
            if (drewAnimated) {
                return;
            }
        }
        if (texture == null) {
            texture = buildTexture();
        }
        float sz = drawSize();
        batch.draw(texture, (float) positionX, (float) positionY, sz, sz);
    }

    private float drawSize() {
        if (type == null) {
            return SIZE;
        }
        return switch (type) {
            case FUME -> 48f;
            case LIGHTNING -> 44f;
            case WHIP -> 40f;
            case GRAPE -> 14f;
            default -> SIZE;
        };
    }

    private Texture buildTexture() {
        int sz = (int) drawSize();
        Pixmap pixmap = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        Color color = colorForType();
        int c = sz / 2;
        if (type == ProjectileType.FUME) {
            pixmap.setColor(0.55f, 0.55f, 0.55f, 0.35f);
            pixmap.fillCircle(c, c, c - 1);
            pixmap.setColor(0.65f, 0.65f, 0.65f, 0.30f);
            pixmap.fillCircle((int) (sz * 0.35f), (int) (sz * 0.40f), (int) (sz * 0.28f));
            pixmap.fillCircle((int) (sz * 0.65f), (int) (sz * 0.55f), (int) (sz * 0.28f));
            pixmap.setColor(0.75f, 0.75f, 0.75f, 0.30f);
            pixmap.fillCircle(c, (int) (sz * 0.35f), (int) (sz * 0.22f));
        } else if (type == ProjectileType.LIGHTNING) {

            pixmap.setColor(color);
            int w = Math.max(3, sz / 7);
            pixmap.fillRectangle((int) (sz * 0.55f), 0, w, (int) (sz * 0.35f));
            pixmap.fillRectangle((int) (sz * 0.40f), (int) (sz * 0.30f), w, (int) (sz * 0.30f));
            pixmap.fillRectangle((int) (sz * 0.55f), (int) (sz * 0.55f), w, (int) (sz * 0.45f));
            pixmap.setColor(1f, 1f, 0.85f, 0.9f);
            pixmap.fillRectangle((int) (sz * 0.55f) + 1, 0, Math.max(1, w - 2), (int) (sz * 0.35f));
        } else if (type == ProjectileType.WHIP) {
            pixmap.setColor(color);
            pixmap.fillRectangle(0, c - 2, sz, 4);
            pixmap.setColor(0.85f, 1f, 0.6f, 0.9f);
            pixmap.fillRectangle((int) (sz * 0.15f), c - 1, (int) (sz * 0.7f), 2);
        } else {
            pixmap.setColor(color);
            pixmap.fillCircle(c, c, c - 1);
            pixmap.setColor(1f, 1f, 1f, 0.6f);
            pixmap.fillCircle(c, c, sz / 4);
        }
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
            case FUME -> new Color(0.6f, 0.6f, 0.6f, 0.5f);
            case LIGHTNING -> new Color(1f, 1f, 0.35f, 1f);
            case GRAPE -> new Color(0.55f, 0.2f, 0.65f, 1f);
            case WHIP -> new Color(0.65f, 0.95f, 0.35f, 1f);
            default -> new Color(0.4f, 0.8f, 0.3f, 1f);
        };
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public void hit() {
        destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }

    public boolean isFreeMotion() {
        return freeMotion;
    }

    public void setFreeMotion(boolean freeMotion) {
        this.freeMotion = freeMotion;
    }

    public void setVelocity(double vx, double vy) {
        this.velX = vx;
        this.velY = vy;
    }

    public double getVelX() {
        return velX;
    }

    public double getVelY() {
        return velY;
    }

    public boolean isHoming() {
        return homing;
    }

    public void setHoming(boolean homing) {
        this.homing = homing;
    }

    public boolean isBouncing() {
        return bouncing;
    }

    public void setBouncing(boolean bouncing) {
        this.bouncing = bouncing;
    }

    public double getFuse() {
        return fuse;
    }

    public void setFuse(double fuse) {
        this.fuse = fuse;
    }

    public void setBounds(double minX, double maxX, double minY, double maxY) {
        this.boundMinX = minX;
        this.boundMaxX = maxX;
        this.boundMinY = minY;
        this.boundMaxY = maxY;
    }

    public boolean isFuseExploded() {
        return fuseExploded;
    }

    public void consumeFuseExplosion() {
        this.fuseExploded = false;
        this.destroyed = true;
    }
}
