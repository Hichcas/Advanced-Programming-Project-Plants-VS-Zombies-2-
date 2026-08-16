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
        if (engine.zombieEngine != null) engine.zombieEngine.draw(batch);
        batch.begin();
        drawTileOverlays(engine, batch);
        drawTombstonesWithHealthBars(engine, batch);
        drawIceBlocksWithHealthBars(engine, batch);
        drawBattleProjectiles(engine, batch);
        drawSuns(engine, batch);
        drawLootDrops(engine, batch);
        drawLawnMowers(engine, batch);
        drawPlantsWithLabels(engine, batch);
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
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Tile tile = engine.map.getTile(row, col);
                if (tile == null) continue;
                TileType type = tile.getType();
                if (type == TileType.WATER) {
                    batch.setColor(0f, 0.3f, 0.8f, 0.35f);
                    batch.draw(whiteTexture(), tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
                } else if (type == TileType.TIDE) {
                    batch.setColor(0f, 0.5f, 1f, 0.5f);
                    batch.draw(whiteTexture(), tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight());
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

                    String label = "Tomb (" + tile.getHp() + "hp)";
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
                font.draw(batch, label, box.x, box.y + box.height + 4);
                drawPlantFreezeOverlay(engine, batch, plant, box);
            }
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
