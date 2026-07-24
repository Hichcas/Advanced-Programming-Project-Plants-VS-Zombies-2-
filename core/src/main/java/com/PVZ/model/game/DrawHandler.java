package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.screen.manager.FontManager;
import com.PVZ.view.HealthBarRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class DrawHandler {

    public static void draw(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.zombieEngine != null) engine.zombieEngine.draw(batch);
        batch.begin();
        drawBattleProjectiles(engine, batch);
        drawSuns(engine, batch);
        drawLootDrops(engine, batch);
        drawLawnMowers(engine, batch);
        drawPlantsWithLabels(engine, batch);
        drawZombiesWithHealthBars(engine, batch);
        batch.end();
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

    private static void drawPlantsWithLabels(RegularGameEngine engine, SpriteBatch batch) {
        if (engine.map == null) return;
        BitmapFont font = FontManager.getInstance().getEnglishTinyFont();
        font.setColor(Color.WHITE);
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Plant plant = engine.map.getPlantAt(row, col);
                if (plant == null || plant.isDead()) continue;
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
        if (freezeLv instanceof Number && ((Number) freezeLv).intValue() >= 3) {
            Color c = batch.getColor();
            batch.setColor(0.3f, 0.6f, 1f, 0.45f);
            batch.draw(engine.iceOverlayTexture(), box.x, box.y, box.width, box.height);
            batch.setColor(c);
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
