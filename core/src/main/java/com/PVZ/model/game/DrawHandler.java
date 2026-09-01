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
        if (com.badlogic.gdx.Gdx.graphics != null) {
            iceBlockStateTime += com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        }
        batch.begin();
        drawTileOverlays(engine, batch);
        drawScorchedTiles(engine, batch);
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
        if (engine.getSandstormManager() != null) engine.getSandstormManager().draw(batch);
        if (engine.getIceWindManager() != null) engine.getIceWindManager().draw(batch, engine);
        batch.setColor(Color.WHITE);
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
                    Color origIceColor = batch.getColor().cpy();

                    // If a zombie is encased inside, render frozen zombie inside the ice block
                    if (tile.getEncasedZombieType() != null) {
                        EntityRenderer.getInstance().renderPam(
                            batch,
                            "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_BASIC/ZOMBIE_ICEAGE_BASIC.PAM",
                            "idle",
                            0f,
                            centerX - 25f,
                            centerY - 25f,
                            0.75f
                        );
                    }

                    if (tile.isHitFlashing()) {
                        batch.setColor(Math.min(2.0f, origIceColor.r * 1.5f + 0.4f),
                                       Math.min(2.0f, origIceColor.g * 1.5f + 0.4f),
                                       Math.min(2.0f, origIceColor.b * 1.5f + 0.4f),
                                       origIceColor.a);
                    }
                    boolean rendered = EntityRenderer.getInstance().renderPam(
                        batch,
                        "768/FULL/WORLDMAP/DANGER_NODE_ICEAGE/DANGER_NODE_ICEAGE.PAM",
                        "locked_idle",
                        iceBlockStateTime,
                        centerX,
                        centerY,
                        scale
                    );
                    if (tile.isHitFlashing()) {
                        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                            com.badlogic.gdx.graphics.GL20.GL_ONE);
                        batch.setColor(1.0f, 1.0f, 1.0f, 0.32f);
                        EntityRenderer.getInstance().renderPam(
                            batch,
                            "768/FULL/WORLDMAP/DANGER_NODE_ICEAGE/DANGER_NODE_ICEAGE.PAM",
                            "locked_idle",
                            iceBlockStateTime,
                            centerX,
                            centerY,
                            scale
                        );
                        batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                            com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
                    }

                    if (!rendered) {
                        // Fallback overlay
                        Color c = batch.getColor();
                        batch.setColor(0.35f, 0.75f, 1f, 0.75f);
                        batch.draw(whiteTexture(), tileX + 4f, tileY + 4f, width - 8f, height - 8f);
                        batch.setColor(c);
                    }
                    batch.setColor(origIceColor);

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
                batch.draw(engine.iceOverlayTexture(), tile.getX()+2f,
                    tile.getY()+2f, tile.getWidth()-4f, tile.getHeight()-4f);
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

    private static void drawScorchedTiles(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.map == null) return;
        for (int r = 0; r < engine.map.getRows(); r++) {
            for (int c = 0; c < engine.map.getCols(); c++) {
                Tile tile = engine.map.getTile(r, c);
                if (tile != null && tile.isScorched()) {
                    float x = tile.getX() + tile.getWidth() / 2f;
                    float y = tile.getY() + tile.getHeight() / 2f;
                    float time = 6.0f - tile.getScorchTimer();
                    EntityRenderer.getInstance().renderPam(batch,
                        "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM",
                        "idle2", time, x, y, 0.85f);
                }
            }
        }
    }

    private static void drawTimedPamEffects(RegularGameEngine engine, SpriteBatch batch) {
        for (RegularGameEngine.TimedPamEffect fx : engine.timedPamEffects) {
            float elapsed = (float) Math.max(0.0, fx.totalDuration - fx.remaining);
            EntityRenderer.getInstance().renderPam(batch, fx.path, fx.clip, elapsed, fx.x, fx.y, fx.scale);
        }
    }

    private static void drawBattleProjectiles(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.battleController != null)
            engine.battleController.drawProjectiles(batch);
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
                    if (t != null && (t.getType() == TileType.WATER ||
                        t.getType() == TileType.TIDE || t.getType() == TileType.LOW_COAST)) {
                        if (c < minWaterCol) minWaterCol = c;
                    }
                }
            }

            if (minWaterCol < 9) {
                Tile shoreTile = engine.map.getTile(2, minWaterCol);
                if (shoreTile != null) {
                    float waterX = shoreTile.getX();
                    float tileW = shoreTile.getWidth();
                    float lawnCenterY = (engine.map.getTile(0, 0).getY() + engine.map.getTile(4, 0).getY()
                        + engine.map.getTile(0, 0).getHeight()) / 2f;
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
                } else if (type == TileType.SLIPPERY_UP) {
                    float cx = tile.getX() + tile.getWidth() / 2f;
                    float cy = tile.getY() + tile.getHeight() / 2f;
                    EntityRenderer.getInstance().renderPam(batch,
                        "768/FULL/EFFECTS/TILESLIDER_ICEAGE_UP/TILESLIDER_ICEAGE_UP.PAM", "idle",
                        iceBlockStateTime, cx, cy, 0.32f);
                } else if (type == TileType.SLIPPERY_DOWN) {
                    float cx = tile.getX() + tile.getWidth() / 2f;
                    float cy = tile.getY() + tile.getHeight() / 2f;
                    EntityRenderer.getInstance().renderPam(batch,
                        "768/FULL/EFFECTS/TILESLIDER_ICEAGE_DOWN/TILESLIDER_ICEAGE_DOWN.PAM", "idle",
                        iceBlockStateTime, cx, cy, 0.32f);
                }
            }
        }
        batch.setColor(orig);
    }

    private static void drawTombstonesWithHealthBars(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.map == null) return;
        BitmapFont font = FontManager.getInstance().getEnglishTinyFont();
        font.setColor(Color.WHITE);
        float delta = com.badlogic.gdx.Gdx.graphics.getDeltaTime();

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Tile tile = engine.map.getTile(row, col);
                if (tile == null) continue;
                if (tile.getType() != TileType.TOMBSTONE && tile.getType() != TileType.NECROMANCY) {
                    continue;
                }
                processTombstoneTile(engine, batch, font, delta, tile);
            }
        }
    }

    private static void processTombstoneTile(RegularGameEngine engine, SpriteBatch batch,
                                             BitmapFont font, float delta, Tile tile) {
        tile.update(delta);

        float tileX = tile.getX();
        float tileY = tile.getY();
        float width = tile.getWidth();
        float height = tile.getHeight();
        float centerX = tileX + width / 2f;
        float centerY = tileY + height / 2f;

        int maxHp = tile.getMaxHp() > 0 ? tile.getMaxHp() : 700;
        int currentHp = Math.max(0, tile.getHp() > 0 ? tile.getHp() : maxHp);
        float hpPercent = Math.max(0f, Math.min(1.0f, (float) currentHp / (float) maxHp));

        com.PVZ.model.enums.GraveVariant variant = resolveGraveVariant(tile);
        renderGraveWithFlashing(batch, variant, hpPercent, centerX, centerY, tile);

        renderGraveBusterDirtIfPresent(batch, tile, centerX, centerY);
        renderHealthBarIfNeeded(batch, font, tile, currentHp, maxHp, hpPercent, variant);
    }

    private static com.PVZ.model.enums.GraveVariant resolveGraveVariant(Tile tile) {
        com.PVZ.model.enums.GraveVariant variant = tile.getGraveVariant();
        if (variant == null) {
            String chap = com.PVZ.model.status.AppStatus.currentChapterName;
            if ("DARK_AGES".equalsIgnoreCase(chap) || tile.getType() == TileType.NECROMANCY) {
                variant = com.PVZ.model.enums.GraveVariant.DARK_NOOP;
            } else {
                variant = com.PVZ.model.enums.GraveVariant.EGYPT;
            }
            tile.setGraveVariant(variant);
        }
        return variant;
    }

    private static void renderGraveWithFlashing(SpriteBatch batch,
                                                com.PVZ.model.enums.GraveVariant variant,
                                                float hpPercent, float centerX, float centerY, Tile tile) {
        String pamPath = variant.getPamPath();
        String clipName = com.PVZ.model.enums.GraveVariant.getClipForHpRatio(hpPercent);

        Color origGraveColor = batch.getColor().cpy();
        if (tile.isHitFlashing()) {
            batch.setColor(Math.min(2.0f, origGraveColor.r * 1.5f + 0.4f),
                Math.min(2.0f, origGraveColor.g * 1.5f + 0.4f),
                Math.min(2.0f, origGraveColor.b * 1.5f + 0.4f),
                origGraveColor.a);
        }

        EntityRenderer.getInstance().renderPam(batch, pamPath, clipName, 0f,
            centerX, centerY, 0.95f);

        if (tile.isHitFlashing()) {
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                com.badlogic.gdx.graphics.GL20.GL_ONE);
            batch.setColor(1.0f, 1.0f, 1.0f, 0.32f);
            EntityRenderer.getInstance().renderPam(batch, pamPath, clipName, 0f,
                centerX, centerY, 0.95f);
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        batch.setColor(origGraveColor);
    }

    private static void renderGraveBusterDirtIfPresent(SpriteBatch batch, Tile tile,
                                                       float centerX, float centerY) {
        Plant plantOnGrave = tile.getPlant();
        if (plantOnGrave != null && plantOnGrave.getType() ==
            com.PVZ.model.enums.PlantType.GRAVE_BUSTER && !plantOnGrave.isDead()) {
            EntityRenderer.getInstance().renderPam(
                batch,
                "768/INITIAL/EFFECTS/GRAVEBUSTER_DIRT/GRAVEBUSTER_DIRT.PAM",
                "gravebuster_dirt_anim",
                tile.getGraveAnimTime(),
                centerX,
                centerY - 10f,
                0.95f
            );
        }
    }

    private static void renderHealthBarIfNeeded(SpriteBatch batch, BitmapFont font,
                                                Tile tile, int currentHp, int maxHp,
                                                float hpPercent,
                                                com.PVZ.model.enums.GraveVariant variant) {
        if (currentHp < maxHp || tile.getType() == TileType.NECROMANCY) {
            float tileX = tile.getX();
            float tileY = tile.getY();
            float width = tile.getWidth();
            float height = tile.getHeight();

            HealthBarRenderer.draw(batch, tileX + 10f, tileY + height - 15f,
                width - 20f, hpPercent, true);

            String label;
            if ("DARK_SUN".equals(variant.name())) {
                label = "Sun Tomb (" + currentHp + "hp)";
            } else if ("DARK_PLANTFOOD".equals(variant.name())) {
                label = "PF Tomb (" + currentHp + "hp)";
            } else {
                label = "Tomb (" + currentHp + "hp)";
            }
            font.draw(batch, label, tileX + 10f, tileY + height - 2f);
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
            Color origOctColor = batch.getColor().cpy();
            if (tile.isHitFlashing()) {
                batch.setColor(Math.min(2.0f, origOctColor.r * 1.5f + 0.4f),
                               Math.min(2.0f, origOctColor.g * 1.5f + 0.4f),
                               Math.min(2.0f, origOctColor.b * 1.5f + 0.4f),
                               origOctColor.a);}
            boolean rendered = EntityRenderer.getInstance().renderPam(batch,
                "768/FULL/EFFECTS/ZOMBIE_OCTOPUS_PROJECTILE/ZOMBIE_OCTOPUS_PROJECTILE.PAM",
                "animation3",iceBlockStateTime,centerX,centerY,1.0f);
            if (tile.isHitFlashing()) {
                batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.
                    GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
                batch.setColor(1.0f, 1.0f, 1.0f, 0.32f);
                EntityRenderer.getInstance().renderPam(
                    batch,
                    "768/FULL/EFFECTS/ZOMBIE_OCTOPUS_PROJECTILE/ZOMBIE_OCTOPUS_PROJECTILE.PAM",
                    "animation3",
                    iceBlockStateTime,centerX,centerY,1.0f);
                batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                    com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);}
            if (!rendered) {
                Color c = batch.getColor();
                batch.setColor(0.9f, 0.4f, 0.1f, 0.75f);
                batch.draw(whiteTexture(), box.x + 6f, box.y + 6f, box.width - 12f, box.height - 12f);
                batch.setColor(c);
            }
            batch.setColor(origOctColor);
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
        if (plant == null || box == null) return;
        float centerX = box.x + box.width / 2f;
        float centerY = box.y + box.height / 2f;
        boolean isFire = plant.getStats() != null && plant.getStats().getBooleanExtra("freezeImmune", false);
        if (!isFire && plant.getDefinition() != null) {
            isFire = plant.getDefinition().hasTag(com.PVZ.model.enums.PlantTag.FIRE);}
        boolean isFrostbite = com.PVZ.model.status.AppStatus.getCurrentChapterEnum()
            == com.PVZ.model.enums.ChapterEnum.FROSTBITE_CAVES
            || (com.PVZ.model.status.AppStatus.currentChapterName != null
            && com.PVZ.model.status.AppStatus.currentChapterName.toUpperCase().contains("FROSTBITE"));
        if (isFire && isFrostbite) {
            EntityRenderer.getInstance().renderPam(batch,
                "768/INITIAL/EFFECTS/FROSTBITE_HEAT_PLANT/FROSTBITE_HEAT_PLANT.PAM",
                "animation",iceBlockStateTime,centerX,centerY,0.95f);return;}
        Object freezeLv = plant.getRuntimeState("freezeLevel");
        if (freezeLv instanceof Number) {
            int lv = ((Number) freezeLv).intValue();
            if (lv >= 3) {
                boolean rendered = EntityRenderer.getInstance().renderPam(batch,
                    "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM",
                    "freeze_idle",iceBlockStateTime,centerX,centerY,0.95f);
                if (!rendered) {
                    Color c = batch.getColor();
                    batch.setColor(0.3f, 0.6f, 1f, 0.65f);
                    batch.draw(engine.iceOverlayTexture(), box.x, box.y, box.width, box.height);
                    batch.setColor(c);}
            } else if (lv > 0) {
                String clip = lv == 1 ? "chill_stage1" : "chill_stage2";
                boolean rendered = EntityRenderer.getInstance().renderPam(batch,
                    "768/FULL/EFFECTS/FROSTBITE_CHILL_PLANT/FROSTBITE_CHILL_PLANT.PAM",
                    clip,iceBlockStateTime,centerX,centerY,0.95f);
                if (!rendered) {
                    Color c = batch.getColor();
                    batch.setColor(0.3f, 0.6f, 1f, 0.25f * lv);
                    batch.draw(engine.iceOverlayTexture(), box.x, box.y, box.width, box.height);
                    batch.setColor(c);}}}
    }

    private static void drawZombiesWithHealthBars(RegularGameEngine engine, SpriteBatch batch) {
        BitmapFont font = FontManager.getInstance().getEnglishTinyFont();
        font.setColor(Color.BLACK);
        com.PVZ.model.entity.zombies.types.zomboss.AbstractZomboss activeBoss = null;
        for (Zombie z : engine.getZombieList()) {
            if (z == null || z.isDead()) continue;
            if (z instanceof com.PVZ.model.entity.zombies.types.zomboss.AbstractZomboss boss) {
                activeBoss = boss;
                continue; // Draw boss bar separately at top of screen
            }
            HealthBarRenderer.draw(batch, (float) z.getX(), (float) z.getY() + 120 + 2, 100,
                (float) z.getHitpoints() / (float) Math.max(1.0, z.getMaxHitpoints()), false);
        }
        font.setColor(Color.WHITE);

        if (activeBoss != null) {
            drawBossHealthBar(engine, batch, activeBoss);
        }
    }

    private static void drawBossHealthBar(RegularGameEngine engine, SpriteBatch batch,
                                          com.PVZ.model.entity.zombies.types.zomboss.AbstractZomboss boss) {
        float barX = 580f;
        float barY = 1010f;
        float barW = 760f;
        float barH = 28f;

        Color origColor = batch.getColor() != null ? batch.getColor().cpy() : new Color(Color.WHITE);
        try {
            // Background shadow & border
            batch.setColor(0f, 0f, 0f, 0.85f);
            batch.draw(whiteTexture(), barX - 4f, barY - 4f, barW + 8f, barH + 8f);

            // Gray empty bar
            batch.setColor(0.2f, 0.2f, 0.2f, 0.9f);
            batch.draw(whiteTexture(), barX, barY, barW, barH);

            // Health Fill
            float hpRatio = (float) Math.max(0.0, Math.min(1.0,
                boss.getHitpoints() / Math.max(1.0, boss.getMaxHitpoints())));
            if (boss.getCurrentPhase() == 3) {
                batch.setColor(0.95f, 0.15f, 0.15f, 1.0f); // Bright red for final phase
            } else if (boss.getCurrentPhase() == 2) {
                batch.setColor(1.0f, 0.55f, 0.1f, 1.0f);  // Orange for phase 2
            } else {
                batch.setColor(0.95f, 0.85f, 0.2f, 1.0f);  // Gold for phase 1
            }
            batch.draw(whiteTexture(), barX, barY, barW * hpRatio, barH);

            // Phase Dividers (at 1/3 and 2/3)
            batch.setColor(0f, 0f, 0f, 0.9f);
            batch.draw(whiteTexture(), barX + barW * 0.333f - 1.5f, barY, 3f, barH);
            batch.draw(whiteTexture(), barX + barW * 0.666f - 1.5f, barY, 3f, barH);

            // Boss Title & Status Text
            BitmapFont font = FontManager.getInstance().getEnglishTinyFont();
            font.setColor(Color.WHITE);
            String status = boss.isStunned() ? " [STUNNED!]" : "";
            String title = "DR. ZOMBOSS - PHASE " + boss.getCurrentPhase() +
                "/3" + status + " (" + (int)(hpRatio * 100) + "%)";
            font.draw(batch, title, barX + 15f, barY + barH - 7f);
        } finally {
            batch.setColor(origColor);
        }
    }
}
