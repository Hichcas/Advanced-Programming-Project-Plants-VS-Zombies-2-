package com.PVZ.model.game;

import com.PVZ.model.entity.Tile;
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
        return collected > 0 ? "Collected " + collected + " sun." : "No sun at selected location.";
    }
}
