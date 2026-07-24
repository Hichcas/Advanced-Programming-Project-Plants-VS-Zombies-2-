package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Sun;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.badlogic.gdx.math.Rectangle;

public class SunHandler {

    public static void spawnSun(RegularGameEngine engine, int amount) {
        spawnSunAt(engine, 0, 0, amount);
    }

    public static void spawnSunAt(RegularGameEngine engine, int row, int col, int amount) {
        if (amount <= 0) return;
        double x = 0, y = 0;
        if (engine.map != null) {
            Tile tile = engine.map.getTile(row, col);
            if (tile != null) {
                x = tile.getX() + tile.getWidth() / 2.0;
                y = tile.getY() + tile.getHeight() / 2.0;
            }
        }
        engine.sunManager.spawnFalling(x, y, amount, y - 120);
    }

    public static int collectSunAtWorldPoint(RegularGameEngine engine, float worldX, float worldY) {
        Rectangle pointer = new Rectangle(worldX - 8f, worldY - 8f, 16f, 16f);
        int collected = engine.sunManager.collectAt(pointer);
        if (collected > 0) engine.addSun(collected);
        applyRadioactiveExplosions(engine);
        return collected;
    }

    public static String collectSunAt(RegularGameEngine engine, int x, int y) {
        int row = engine.normalizeIndex(y);
        int col = engine.normalizeIndex(x);
        double worldX = x * 100.0, worldY = y * 100.0;
        if (engine.map != null) {
            Tile tile = engine.map.getTile(row, col);
            if (tile != null) {
                worldX = tile.getX() + tile.getWidth() / 2.0;
                worldY = tile.getY() + tile.getHeight() / 2.0;
            }
        }
        int collected = engine.sunManager.collectAt(worldX, worldY);
        engine.addSun(collected);
        int explosions = applyRadioactiveExplosions(engine);
        if (explosions > 0) {
            return "A radioactive sun exploded!";
        }
        return collected > 0 ? "Collected " + collected + " sun." : "No sun at selected location.";
    }

    public static int applyRadioactiveExplosions(RegularGameEngine engine) {
        java.util.List<Sun> exploded = engine.sunManager.drainExplodedSuns();
        for (Sun sun : exploded) {
            detonateRadioactiveSun(engine, sun);
        }
        return exploded.size();
    }

    private static void detonateRadioactiveSun(RegularGameEngine engine, Sun sun) {
        if (engine.map == null) {
            return;
        }
        int centerRow = engine.map.worldToRow((float) sun.getY());
        int centerCol = engine.map.worldToCol((float) sun.getX());
        System.out.println("A radioactive sun exploded at (" + centerCol + ", " + centerRow + ")!");
        for (Zombie z : engine.getZombieList()) {
            if (z == null || z.isDead()) {
                continue;
            }
            int zRow = (int) z.getRow();
            int zCol = engine.map.worldToCol((float) z.getX());
            if (Math.abs(zRow - centerRow) <= 2 && Math.abs(zCol - centerCol) <= 2) {
                z.takeDamage(150);
            }
        }
        for (int r = centerRow - 1; r <= centerRow + 1; r++) {
            for (int c = centerCol - 1; c <= centerCol + 1; c++) {
                Plant plant = engine.map.getPlantAt(r, c);
                if (plant != null && !plant.isDead()) {
                    plant.takeDamage(80);
                }
            }
        }
    }
}
