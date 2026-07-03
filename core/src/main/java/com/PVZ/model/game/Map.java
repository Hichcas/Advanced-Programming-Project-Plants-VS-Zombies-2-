package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.enums.TileType;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Map {
    private Tile[][] tiles;
    private int rows, cols;

    // موقعیت و ابعاد کل نقشه
    private float startX, startY;   // گوشه‌ی بالا-چپ نقشه
    private float totalWidth, totalHeight;

    public Map(float startX, float startY, float mapWidth, float mapHeight,
               int rows, int cols) {
        this.startX = startX;
        this.startY = startY;
        this.totalWidth = mapWidth;
        this.totalHeight = mapHeight;
        this.rows = rows;
        this.cols = cols;

        float tileWidth = mapWidth / cols;
        float tileHeight = mapHeight / rows;

        tiles = new Tile[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                float tileX = startX + c * tileWidth;
                // در LibGDX محور Y به سمت بالا است، بنابراین ردیف ۰ (بالاترین) باید بالای نقشه قرار گیرد.
                // ما فرض می‌کنیم startY لبه‌ی بالایی نقشه است.
                float tileY = startY - (r + 1) * tileHeight;  // y گوشه‌ی پایین-چپ خانه

                tiles[r][c] = new Tile(TileType.NORMAL, null, r, c,
                    tileX, tileY, tileWidth, tileHeight);
            }
        }
    }

    /** رسم حاشیه‌ی تمام خانه‌ها (برای دیباگ) */
    public void renderBorders(ShapeRenderer sr) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c].drawBorder(sr);
            }
        }
    }

    /** تبدیل مختصات دنیا به اندیس شبکه */
    public int worldToRow(float worldY) {
        // worldY = startY - (row+1)*tileHeight + tileHeight/2? ساده‌تر:
        // row = floor((startY - worldY) / tileHeight)
        return (int)((startY - worldY) / (totalHeight / rows));
    }

    public int worldToCol(float worldX) {
        return (int)((worldX - startX) / (totalWidth / cols));
    }

    public boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    // ---------- مدیریت گیاهان ----------
    public Plant getPlantAt(int row, int col) {
        if (!isWithinBounds(row, col)) return null;
        return tiles[row][col].getPlant();
    }

    public void setPlant(int row, int col, Plant plant) {
        if (!isWithinBounds(row, col)) return;
        tiles[row][col].setPlant(plant);
    }

    public void removePlant(int row, int col) {
        if (!isWithinBounds(row, col)) return;
        tiles[row][col].setPlant(null);
    }

    public Tile getTile(int row, int col) {
        if (!isWithinBounds(row, col)) return null;
        return tiles[row][col];
    }

    // getters برای اندازه‌ها (مفید برای کلیک)
    public float getTileWidth() { return totalWidth / cols; }
    public float getTileHeight() { return totalHeight / rows; }
    public float getStartX() { return startX; }
    public float getStartY() { return startY; }
}
