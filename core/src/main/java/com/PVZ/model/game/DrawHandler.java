package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.HealthBarRenderer;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.enums.TileType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class DrawHandler {

    private static Texture whiteTexture;

    private static Texture whiteTexture() {
        if (whiteTexture == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.fill();
            whiteTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return whiteTexture;
    }

    public static void draw(RegularGameEngine engine, SpriteBatch batch) {
        EntityRenderer.getInstance().update();
        batch.begin();
        drawTileOverlays(engine, batch);
        drawJalapenoLaneEffect(engine, batch);
        drawGlobalIceEffect(engine, batch);
        drawIceShroomTileEffect(engine, batch);
        drawTimedPamEffects(engine, batch);
        drawTombstonesWithHealthBars(engine, batch);
        drawIceBlocksWithHealthBars(engine, batch);
        drawPlantsWithLabels(engine, batch);
        batch.end();

        if (engine.zombieEngine != null) engine.zombieEngine.draw(batch);

        batch.begin();
        drawBattleProjectiles(engine, batch);
        drawSuns(engine, batch);
        drawLootDrops(engine, batch);
        drawLawnMowers(engine, batch);
        drawZombiesWithHealthBars(engine, batch);
        batch.end();
    }

    private static float iceBlockStateTime = 0.0f;

    private static void drawIceBlocksWithHealthBars(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.map == null) return;
        iceBlockStateTime += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        BitmapFont font = FontManager.getInstance().getEnglishTinyFont();
        font.setColor(Color.WHITE);
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Tile tile = engine.map.getTile(row, col);
                if (tile != null && tile.getType() == TileType.ICE) {
                    float tileX = tile.getX();
                    float tileY = tile.getY();
                    float width = tile.getWidth();
                    float height = tile.getHeight();

                    // Render DANGER_NODE_ICEAGE.PAM (locked_idle) scaled to tile size
                    float centerX = tileX + width / 2f;
                    float centerY = tileY + height / 2f;
                    float scale = 0.28f;
                    boolean rendered = EntityRenderer.getInstance().renderPam(
                        batch,
                        "768/FULL/WORLDMAP/DANGER_NODE_ICEAGE/DANGER_NODE_ICEAGE.PAM",
                        "locked_idle",
                        iceBlockStateTime,
                        centerX,
                        centerY,
                        scale
                    );

                    if (!rendered) {
                        // Fallback overlay
                        Color c = batch.getColor();
                        batch.setColor(0.35f, 0.75f, 1f, 0.75f);
                        batch.draw(whiteTexture(), tileX + 4f, tileY + 4f, width - 8f, height - 8f);
                        batch.setColor(c);
                    }

                    // Draw Health Bar (Slider Bar)
                    int currentHp = Math.max(0, tile.getHp() > 0 ? tile.getHp() : 1800);
                    float hpPercent = Math.max(0f, Math.min(1.0f, (float) currentHp / 1800f));
                    HealthBarRenderer.draw(batch, tileX + 10f, tileY + height - 15f, width - 20f, hpPercent, false);

                    String label = "Ice (" + currentHp + "hp)";
                    font.draw(batch, label, tileX + 10f, tileY + height - 2f);
                }
            }
        }
    }

    private static void drawJalapenoLaneEffect(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.jalapenoLaneEffectTimer <= 0.0 || engine.map == null || engine.jalapenoLaneEffectRow < 0) return;
        int row = engine.jalapenoLaneEffectRow;
        for (int col = 0; col < engine.map.getCols(); col++) {
            Tile tile = engine.map.getTile(row, col);
            if (tile == null) continue;
            EntityRenderer.getInstance().renderPam(batch,
                "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM",
                "idle2", (float)(1.35 - engine.jalapenoLaneEffectTimer),
                tile.getX() + tile.getWidth()/2f, tile.getY() + tile.getHeight()/2f, 0.9f);
        }
    }

    private static void drawGlobalIceEffect(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.getGlobalIceEffectTimer() <= 0.0 || engine.map == null) return;
        Color c = batch.getColor();
        batch.setColor(0.45f, 0.75f, 1f, 0.24f);
        batch.draw(engine.iceOverlayTexture(), engine.map.getStartX(),
            engine.map.getStartY() - engine.map.getTotalHeight(),
            engine.map.getTotalWidth(), engine.map.getTotalHeight());
        batch.setColor(c);
        float cx = engine.getMapCenterX();
        float cy = engine.getMapCenterY();
        EntityRenderer.getInstance().renderPam(batch,
            "768/FULL/EFFECTS/ICESHROOM_FX/ICESHROOM_FX.PAM",
            "animation", (float)(1.1333 - engine.getGlobalIceEffectTimer()), cx, cy, 1.55f);
        for (int row = 0; row < engine.map.getRows(); row++) {
            for (int col = 0; col < engine.map.getCols(); col++) {
                Tile tile = engine.map.getTile(row, col);
                if (tile == null) continue;
                Color cell = batch.getColor();
                batch.setColor(0.55f, 0.82f, 1f, 0.18f);
                batch.draw(engine.iceOverlayTexture(), tile.getX()+2f, tile.getY()+2f, tile.getWidth()-4f, tile.getHeight()-4f);
                batch.setColor(cell);
            }
        }
    }

    private static void drawIceShroomTileEffect(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.iceShroomEffectTimer <= 0.0 || engine.map == null || engine.iceShroomEffectRow < 0) return;
        Tile tile = engine.map.getTile(engine.iceShroomEffectRow, engine.iceShroomEffectCol);
        if (tile == null) return;
        float x = tile.getX() + tile.getWidth()/2f;
        float y = tile.getY() + tile.getHeight()/2f;
        Color c = batch.getColor();
        batch.setColor(0.55f, 0.85f, 1f, 0.65f);
        batch.draw(engine.iceOverlayTexture(), tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
        batch.setColor(c);
        EntityRenderer.getInstance().renderPam(batch,
            "768/FULL/EFFECTS/ICESHROOM_FX/ICESHROOM_FX.PAM", "animation",
            (float)(1.1333 - engine.iceShroomEffectTimer), x, y, 1.15f);
    }

    private static void drawTimedPamEffects(RegularGameEngine engine, SpriteBatch batch) {
        for (RegularGameEngine.TimedPamEffect fx : engine.timedPamEffects) {
            float elapsed = 1.0f;
            EntityRenderer.getInstance().renderPam(batch, fx.path, fx.clip, elapsed, fx.x, fx.y, fx.scale);
        }
    }

    private static void drawBattleProjectiles(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.battleController != null) engine.battleController.drawProjectiles(batch);
        for (Projectile p : engine.projectiles) p.draw(batch);
    }

    private static void drawSuns(RegularGameEngine engine, SpriteBatch batch) {
        for (com.PVZ.model.entity.Sun sun : engine.sunManager.getSuns()) sun.draw(batch);
    }

    private static void drawLootDrops(RegularGameEngine engine, SpriteBatch batch) {
        for (com.PVZ.model.entity.LootDrop drop : engine.lootManager.getDrops()) drop.draw(batch);
    }

    private static void drawLawnMowers(RegularGameEngine engine, SpriteBatch batch) {
        for (com.PVZ.model.entity.LawnMower mower : engine.lawnMowers) {
            if (mower != null) mower.draw(batch);
        }
    }

    private static void drawTileOverlays(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.map == null) return;
        Color orig = batch.getColor();

        // 1. Render chapter-wide Big Wave Beach Ocean layers (UNDER plants and zombies)
        if ("BIG_WAVE_BEACH".equalsIgnoreCase(com.PVZ.model.status.AppStatus.currentChapterName)
            || "BEACH".equalsIgnoreCase(com.PVZ.model.status.AppStatus.currentChapterName)) {

            int minWaterCol = 9;
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 9; c++) {
                    Tile t = engine.map.getTile(r, c);
                    if (t != null && (t.getType() == TileType.WATER || t.getType() == TileType.TIDE || t.getType() == TileType.LOW_COAST)) {
                        if (c < minWaterCol) minWaterCol = c;
                    }
                }
            }

            if (minWaterCol < 9) {
                Tile shoreTile = engine.map.getTile(2, minWaterCol);
                if (shoreTile != null) {
                    float waterX = shoreTile.getX();
                    float tileW = shoreTile.getWidth();
                    float lawnCenterY = (engine.map.getTile(0, 0).getY() + engine.map.getTile(4, 0).getY() + engine.map.getTile(0, 0).getHeight()) / 2f;

                    // 1. Draw WATER_UNDERLAYER (Aligned so the water edge starts right at the L column / 3rd from right)
                    EntityRenderer.getInstance().renderPam(
                        batch,
                        "768/FULL/BACKGROUNDS/WATER_UNDERLAYER/WATER_UNDERLAYER.PAM",
                        "Water",
                        iceBlockStateTime,
                        waterX + 250f + 2f * tileW,
                        lawnCenterY,
                        1.0f
                    );

                    // 2. Draw WAVE_UPPERLAYER (Animated ocean waves)
                    EntityRenderer.getInstance().renderPam(
                        batch,
                        "768/FULL/BACKGROUNDS/WAVE_UPPERLAYER/WAVE_UPPERLAYER.PAM",
                        "water",
                        iceBlockStateTime,
                        waterX + 250f + 2f * tileW,
                        lawnCenterY,
                        1.0f
                    );

                    // 3. Draw WATER_TIDE_LINE (White foam wave aligned exactly onto the L column)
                    EntityRenderer.getInstance().renderPam(
                        batch,
                        "768/FULL/BACKGROUNDS/WATER_TIDE_LINE/WATER_TIDE_LINE.PAM",
                        "idle",
                        iceBlockStateTime,
                        waterX + 410f + 2f * tileW,
                        lawnCenterY,
                        1.0f
                    );
                }
            }
        }

        // 2. Per-tile overlays (Craters, etc.)
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Tile tile = engine.map.getTile(row, col);
                if (tile == null) continue;
                TileType type = tile.getType();
                if (type == TileType.CRATER) {
                    Color c = batch.getColor();
                    batch.setColor(0.10f, 0.08f, 0.06f, 0.92f);
                    batch.draw(engine.iceOverlayTexture(), tile.getX() + 5f, tile.getY() + 5f,
                        tile.getWidth() - 10f, tile.getHeight() - 10f);
                    batch.setColor(c);
                    EntityRenderer.getInstance().renderPam(batch,
                        "768/FULL/EFFECTS/CRATER/CRATER.PAM", "animation", 0f,
                        tile.getX()+tile.getWidth()/2f, tile.getY()+tile.getHeight()/2f, 0.9f);
                }
            }
        }
        batch.setColor(orig);
    }

    private static void drawTombstonesWithHealthBars(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.map == null) return;
        BitmapFont font = FontManager.getInstance().getEnglishTinyFont();
        font.setColor(Color.WHITE);
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Tile tile = engine.map.getTile(row, col);
                if (tile != null && tile.getType() == TileType.TOMBSTONE) {
                    float tileX = tile.getX();
                    float tileY = tile.getY();
                    float width = tile.getWidth();
                    float height = tile.getHeight();

                    float hpPercent = Math.max(0f, (float) tile.getHp() / 700f);
                    HealthBarRenderer.draw(batch, tileX + 10f, tileY + height - 15f, width - 20f, hpPercent, true);

                    String label = "BIG_WAVE_BEACH".equals(com.PVZ.model.status.AppStatus.currentChapterName)
                        ? "Surfboard (" + tile.getHp() + "hp)"
                        : "Tomb (" + tile.getHp() + "hp)";
                    font.draw(batch, label, tileX + 10f, tileY + height - 2f);
                }
            }
        }
    }

    private static void drawPlantsWithLabels(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.map == null) return;
        BitmapFont font = FontManager.getInstance().getEnglishTinyFont();
        font.setColor(Color.WHITE);
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Plant base = engine.map.getBasePlantAt(row, col);
                if (base != null && !base.isDead()) {
                    base.draw(batch);
                }
                Plant plant = engine.map.getPlantAt(row, col);
                if (plant == null || plant.isDead()) {
                    if (plant != null && plant.isDead()) {
                        Object fx = plant.getRuntimeState("deathFxTimer");
                        if (fx instanceof Number n && n.doubleValue() > 0.0) {
                            plant.draw(batch);
                        }
                    }
                    if (base != null && !base.isDead()) {
                        Rectangle box = base.getHitbox();
                        HealthBarRenderer.draw(batch, box.x, box.y + box.height + 2, box.width,
                            (float) base.getCurrentHp() / Math.max(1, base.getMaxHp()), true);
                        String label = base.getType() + " (" + base.getCurrentHp() + "hp)";
                        font.draw(batch, label, box.x, box.y + box.height + 4);
                        drawPlantFreezeOverlay(engine, batch, base, box);
                    }
                    continue;
                }
                plant.draw(batch);
                Rectangle box = plant.getHitbox();
                HealthBarRenderer.draw(batch, box.x, box.y + box.height + 2, box.width,
                    (float) plant.getCurrentHp() / Math.max(1, plant.getMaxHp()), true);
                String label = plant.getType() + " (" + plant.getCurrentHp() + "hp)";
                drawPlantFreezeOverlay(engine, batch, plant, box);
                drawPlantOctopusOverlay(engine, batch, row, col, box);
            }
        }
    }

    private static void drawPlantOctopusOverlay(RegularGameEngine engine, SpriteBatch batch,
                                                int row, int col, Rectangle box) {
        if (engine.map == null) return;
        Tile tile = engine.map.getTile(row, col);
        if (tile != null && tile.getOctopusHp() > 0) {
            float centerX = box.x + box.width / 2f;
            float centerY = box.y + box.height / 2f;
            boolean rendered = EntityRenderer.getInstance().renderPam(
                batch,
                "768/FULL/EFFECTS/ZOMBIE_OCTOPUS_PROJECTILE/ZOMBIE_OCTOPUS_PROJECTILE.PAM",
                "animation3",
                iceBlockStateTime,
                centerX,
                centerY,
                1.0f
            );
            if (!rendered) {
                Color c = batch.getColor();
                batch.setColor(0.9f, 0.4f, 0.1f, 0.75f);
                batch.draw(whiteTexture(), box.x + 6f, box.y + 6f, box.width - 12f, box.height - 12f);
                batch.setColor(c);
            }

            // Draw Octopus Health Bar
            float hpPercent = Math.max(0f, Math.min(1.0f, (float) tile.getOctopusHp() / 200f));
            HealthBarRenderer.draw(batch, box.x, box.y + box.height + 14f, box.width, hpPercent, false);
            BitmapFont font = FontManager.getInstance().getEnglishTinyFont();
            font.setColor(Color.ORANGE);
            font.draw(batch, "Octopus (" + tile.getOctopusHp() + "hp)", box.x, box.y + box.height + 26f);
            font.setColor(Color.WHITE);
        }
    }

    private static void drawPlantFreezeOverlay(RegularGameEngine engine,
                        SpriteBatch batch, Plant plant, Rectangle box) {
        Object freezeLv = plant.getRuntimeState("freezeLevel");
        if (freezeLv instanceof Number) {
            int lv = ((Number) freezeLv).intValue();
            if (lv >= 3) {
                float centerX = box.x + box.width / 2f;
                float centerY = box.y + box.height / 2f;
                boolean rendered = EntityRenderer.getInstance().renderPam(
                    batch,
                    "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM",
                    "freeze_idle",
                    iceBlockStateTime,
                    centerX,
                    centerY,
                    0.28f
                );
                if (!rendered) {
                    Color c = batch.getColor();
                    batch.setColor(0.3f, 0.6f, 1f, 0.65f);
                    batch.draw(engine.iceOverlayTexture(), box.x, box.y, box.width, box.height);
                    batch.setColor(c);
                }
            } else if (lv > 0) {
                Color c = batch.getColor();
                batch.setColor(0.3f, 0.6f, 1f, 0.25f * lv);
                batch.draw(engine.iceOverlayTexture(), box.x, box.y, box.width, box.height);
                batch.setColor(c);
            }
        }
    }

    private static void drawZombiesWithHealthBars(RegularGameEngine engine, SpriteBatch batch) {
        BitmapFont font = FontManager.getInstance().getEnglishTinyFont();
        font.setColor(Color.BLACK);
        for (Zombie z : engine.getZombieList()) {
            if (z == null || z.isDead()) continue;
            HealthBarRenderer.draw(batch, (float) z.getX(), (float) z.getY() + 120 + 2, 100,
                (float) z.getHitpoints() / (float) Math.max(1.0, z.getMaxHitpoints()), false);
            if (z.isFrozen()) {
                Color c = batch.getColor();
                batch.setColor(0.3f, 0.6f, 1f, 0.45f);
                batch.draw(engine.iceOverlayTexture(), (float) z.getX(), (float) z.getY(), 100, 120);
                batch.setColor(c);
            }
        }
        font.setColor(Color.WHITE);
    }
}
