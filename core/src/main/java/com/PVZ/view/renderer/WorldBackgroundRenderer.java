package com.PVZ.view.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders seamless, full-resolution stitched world backgrounds for all 4 chapters.
 * Automatically stitches the official atlas slices in-memory if pre-generated stitched files
 * are not present, ensuring 100% portability across all team members' computers.
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
        final String preStitchedPath;
        final String atlasPath;
        final int[] leftRect;   // [x, y, w, h]
        final int[] centerRect; // [x, y, w, h]
        final int[] rightRect;  // [x, y, w, h]
        final float leftMarginPx;
        final float lawnOffsetYPx;

        WorldConfig(String preStitchedPath, String atlasPath,
                    int[] leftRect, int[] centerRect, int[] rightRect,
                    float leftMarginPx, float lawnOffsetYPx) {
            this.preStitchedPath = preStitchedPath;
            this.atlasPath = atlasPath;
            this.leftRect = leftRect;
            this.centerRect = centerRect;
            this.rightRect = rightRect;
            this.leftMarginPx = leftMarginPx;
            this.lawnOffsetYPx = lawnOffsetYPx;
        }
    }

    private final Map<String, WorldConfig> worldConfigs = new HashMap<>();
    private final Map<String, Texture> loadedTextures = new HashMap<>();

    private WorldBackgroundRenderer() {
        // 1. ANCIENT EGYPT
        worldConfigs.put("ANCIENT_EGYPT", new WorldConfig(
            "maps/stitched_egypt.png",
            "pvz-assets/ATLASES/DELAYLOAD_BACKGROUND_EGYPT_COMPRESSED_768_00.PNG",
            new int[]{676, 771, 278, 768},
            new int[]{0, 1, 1024, 768},
            new int[]{1, 771, 673, 768},
            390f, 130f
        ));
        worldConfigs.put("EGYPT", worldConfigs.get("ANCIENT_EGYPT"));

        // 2. FROSTBITE CAVES / ICEAGE
        worldConfigs.put("FROSTBITE_CAVES", new WorldConfig(
            "maps/stitched_iceage.png",
            "pvz-assets/ATLASES/DELAYLOAD_BACKGROUND_ICEAGE_COMPRESSED_768_00.PNG",
            new int[]{676, 788, 281, 768},
            new int[]{1, 1, 1022, 785},
            new int[]{1, 788, 673, 768},
            393f, 130f
        ));
        worldConfigs.put("ICEAGE", worldConfigs.get("FROSTBITE_CAVES"));

        // 3. BIG WAVE BEACH
        worldConfigs.put("BIG_WAVE_BEACH", new WorldConfig(
            "maps/stitched_beach.png",
            "pvz-assets/ATLASES/DELAYLOAD_BACKGROUND_BEACH_COMPRESSED_768_00.PNG",
            new int[]{1702, 1, 278, 768},
            new int[]{1, 1, 1024, 768},
            new int[]{1027, 1, 673, 768},
            390f, 130f
        ));
        worldConfigs.put("BEACH", worldConfigs.get("BIG_WAVE_BEACH"));

        // 4. DARK AGES
        worldConfigs.put("DARK_AGES", new WorldConfig(
            "maps/stitched_dark.png",
            "pvz-assets/ATLASES/DELAYLOAD_BACKGROUND_DARK_COMPRESSED_768_00.PNG",
            new int[]{676, 771, 278, 768},
            new int[]{0, 1, 1024, 768},
            new int[]{1, 771, 673, 768},
            390f, 130f
        ));
        worldConfigs.put("DARK", worldConfigs.get("DARK_AGES"));
    }

    private Texture getOrLoadTexture(String chapterKey) {
        if (chapterKey == null) return null;
        String key = chapterKey.toUpperCase();
        WorldConfig cfg = worldConfigs.get(key);
        if (cfg == null) return null;
        if (loadedTextures.containsKey(key)) {
            return loadedTextures.get(key);}
        if (cfg.preStitchedPath != null && Gdx.files.internal(cfg.preStitchedPath).exists()) {
            try {
                Texture tex = new Texture(Gdx.files.internal(cfg.preStitchedPath));
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                loadedTextures.put(key, tex);return tex;
            } catch (Exception ex) {
                System.err.println("WorldBackgroundRenderer: failed to load pre-stitched "
                    + cfg.preStitchedPath + ": " + ex.getMessage());}}
        if (cfg.atlasPath != null && Gdx.files.internal(cfg.atlasPath).exists()) {
            try {
                Pixmap atlasPixmap = new Pixmap(Gdx.files.internal(cfg.atlasPath));
                int totalW = cfg.leftRect[2] + cfg.centerRect[2] + cfg.rightRect[2];
                int totalH = Math.max(cfg.leftRect[3], Math.max(cfg.centerRect[3], cfg.rightRect[3]));
                Pixmap stitchedPixmap = new Pixmap(totalW, totalH, Pixmap.Format.RGBA8888);
                stitchedPixmap.drawPixmap(atlasPixmap, 0, 0,
                    cfg.leftRect[0], cfg.leftRect[1], cfg.leftRect[2], cfg.leftRect[3]);
                stitchedPixmap.drawPixmap(atlasPixmap, cfg.leftRect[2], 0,
                    cfg.centerRect[0], cfg.centerRect[1], cfg.centerRect[2], cfg.centerRect[3]);
                stitchedPixmap.drawPixmap(atlasPixmap, cfg.leftRect[2] + cfg.centerRect[2], 0,
                    cfg.rightRect[0], cfg.rightRect[1], cfg.rightRect[2], cfg.rightRect[3]);
                Texture tex = new Texture(stitchedPixmap);
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                atlasPixmap.dispose();stitchedPixmap.dispose();
                loadedTextures.put(key, tex);
                System.out.println("WorldBackgroundRenderer: dynamically stitched "
                    + key + " in-memory (" + totalW + "x" + totalH + ").");
                return tex;
            } catch (Exception ex) {
                System.err.println("WorldBackgroundRenderer: failed to dynamic-stitch "
                    + cfg.atlasPath + ": " + ex.getMessage());}}return null;
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
