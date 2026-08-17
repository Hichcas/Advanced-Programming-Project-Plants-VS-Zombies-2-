package com.PVZ.view.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders seamless, full-resolution stitched world backgrounds for all 4 chapters.
 * Eliminates all edge-to-edge slicing seams and aligns perfectly with the tile grid.
 */
public class WorldBackgroundRenderer {

    private static WorldBackgroundRenderer instance;

    public static WorldBackgroundRenderer getInstance() {
        if (instance == null) {
            instance = new WorldBackgroundRenderer();
        }
        return instance;
    }

    private static class WorldConfig {
        final String imagePath;
        final float leftMarginPx;  // horizontal alignment from left edge to lawn grid start
        final float lawnOffsetYPx; // vertical alignment offset

        WorldConfig(String imagePath, float leftMarginPx, float lawnOffsetYPx) {
            this.imagePath = imagePath;
            this.leftMarginPx = leftMarginPx;
            this.lawnOffsetYPx = lawnOffsetYPx;
        }
    }

    private final Map<String, WorldConfig> worldConfigs = new HashMap<>();
    private final Map<String, Texture> loadedTextures = new HashMap<>();

    private WorldBackgroundRenderer() {
        // Left margin: 278px (House) + 110px (Lawn left margin) = 388px in 768p coordinates
        worldConfigs.put("ANCIENT_EGYPT", new WorldConfig("maps/stitched_egypt.png", 390f, 130f));
        worldConfigs.put("EGYPT", worldConfigs.get("ANCIENT_EGYPT"));

        worldConfigs.put("FROSTBITE_CAVES", new WorldConfig("maps/stitched_iceage.png", 393f, 130f));
        worldConfigs.put("ICEAGE", worldConfigs.get("FROSTBITE_CAVES"));

        worldConfigs.put("BIG_WAVE_BEACH", new WorldConfig("maps/stitched_beach.png", 390f, 130f));
        worldConfigs.put("BEACH", worldConfigs.get("BIG_WAVE_BEACH"));

        worldConfigs.put("DARK_AGES", new WorldConfig("maps/stitched_dark.png", 390f, 130f));
        worldConfigs.put("DARK", worldConfigs.get("DARK_AGES"));
    }

    private Texture getOrLoadTexture(String chapterKey) {
        if (chapterKey == null) return null;
        String key = chapterKey.toUpperCase();
        WorldConfig cfg = worldConfigs.get(key);
        if (cfg == null) return null;

        Texture tex = loadedTextures.get(cfg.imagePath);
        if (tex == null) {
            if (!Gdx.files.internal(cfg.imagePath).exists()) {
                System.err.println("WorldBackgroundRenderer: file not found: " + cfg.imagePath);
                return null;
            }
            tex = new Texture(Gdx.files.internal(cfg.imagePath));
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            loadedTextures.put(cfg.imagePath, tex);
        }
        return tex;
    }

    /**
     * Renders the single unified, seamless world background perfectly aligned with the game map.
     */
    public boolean render(SpriteBatch batch, String chapterName, float startX, float startY,
                          float mapWidth, float mapHeight) {
        if (chapterName == null) return false;
        String key = chapterName.toUpperCase();
        WorldConfig cfg = worldConfigs.get(key);
        if (cfg == null) return false;

        Texture tex = getOrLoadTexture(key);
        if (tex == null) return false;

        float scale = 1.95f;
        float totalW = tex.getWidth() * scale;
        float totalH = tex.getHeight() * scale;

        float drawX = startX - (cfg.leftMarginPx * scale);
        float drawY = startY - totalH + (cfg.lawnOffsetYPx * scale);

        batch.draw(tex, drawX, drawY, totalW, totalH);
        return true;
    }

    public void dispose() {
        for (Texture tex : loadedTextures.values()) {
            if (tex != null) {
                tex.dispose();
            }
        }
        loadedTextures.clear();
    }
}
