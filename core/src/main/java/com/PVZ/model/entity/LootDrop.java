package com.PVZ.model.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.EnumMap;
import java.util.Map;


public class LootDrop {

    public enum LootType {
        COIN(50, "Loot/Coin.png"),
        DIAMOND(1, "Loot/Diamond.png"),
        POT(1, "Loot/Pot.png");

        private final int amount;
        private final String texturePath;

        LootType(int amount, String texturePath) {
            this.amount = amount;
            this.texturePath = texturePath;
        }

        public int getAmount() {
            return amount;
        }

        public String getTexturePath() {
            return texturePath;
        }
    }

    private static final float SIZE = 48f;
    private static final float LIFETIME = 12f;
    private static final Map<LootType, Texture> SHARED_TEXTURES = new EnumMap<>(LootType.class);
    private static final Map<LootType, Boolean> TRIED_LOAD = new EnumMap<>(LootType.class);

    private final double x;
    private final double y;
    private final LootType type;
    private float timer = 0f;
    private boolean collected = false;
    private final Rectangle hitbox = new Rectangle();

    public LootDrop(double x, double y, LootType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        hitbox.set((float) x, (float) y, SIZE, SIZE);
    }

    public void update(float delta) {
        if (collected) {
            return;
        }
        timer += delta;
        if (timer >= LIFETIME) {
            collected = true;
        }
    }

    public void draw(SpriteBatch batch) {
        if (collected) {
            return;
        }
        boolean drawn = com.PVZ.view.renderer.EntityRenderer.getInstance()
            .renderLoot(batch, type, timer, (float) x, (float) y);
        if (!drawn) {
            batch.draw(getOrLoadTexture(type), (float) x, (float) y, hitbox.width, hitbox.height);
        }
    }

    private static Texture getOrLoadTexture(LootType type) {
        Texture cached = SHARED_TEXTURES.get(type);
        if (cached != null) {
            return cached;
        }
        if (!Boolean.TRUE.equals(TRIED_LOAD.get(type))) {
            TRIED_LOAD.put(type, true);
            try {
                if (Gdx.files.internal(type.getTexturePath()).exists()) {
                    Texture loaded = new Texture(Gdx.files.internal(type.getTexturePath()));
                    SHARED_TEXTURES.put(type, loaded);
                    return loaded;
                }
                System.out.println("[LootDrop] no icon found at assets/" + type.getTexturePath()
                    + " -> falling back to placeholder");
            } catch (RuntimeException ex) {
                System.out.println("[LootDrop] failed loading texture at assets/" + type.getTexturePath()
                    + " -> " + ex.getMessage());
            }
        }
        Texture placeholder = buildPlaceholderTexture(type);
        SHARED_TEXTURES.put(type, placeholder);
        return placeholder;
    }

    private static Texture buildPlaceholderTexture(LootType type) {
        int size = (int) SIZE;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        Color base = switch (type) {
            case COIN -> Color.GOLD;
            case DIAMOND -> Color.SKY;
            case POT -> Color.BROWN;
        };
        pixmap.setColor(base);
        if (type == LootType.DIAMOND) {
            pixmap.fillTriangle(size / 2, 2, 2, size / 2, size - 2, size / 2);
            pixmap.fillTriangle(size / 2, size - 2, 2, size / 2, size - 2, size / 2);
        } else {
            pixmap.fillCircle(size / 2, size / 2, size / 2 - 1);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public float getAnimationTime() {
        return timer;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public LootType getType() {
        return type;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
