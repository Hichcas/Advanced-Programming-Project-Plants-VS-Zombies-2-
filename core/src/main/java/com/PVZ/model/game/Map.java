package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.enums.TileType;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Map {
    private Tile[][] tiles;
    private int rows, cols;

    private float startX, startY;
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
                float tileY = startY - (r + 1) * tileHeight;
                tiles[r][c] = new Tile(TileType.NORMAL, null, r, c,
                    tileX, tileY, tileWidth, tileHeight);
            }
        }
    }

    public void renderBorders(ShapeRenderer sr) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c].drawBorder(sr);
            }
        }
    }

    public int worldToRow(float worldY) {
        return (int)((startY - worldY) / (totalHeight / rows));
    }

    public int worldToCol(float worldX) {
        return (int)((worldX - startX) / (totalWidth / cols));
    }

    public boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public Plant getPlantAt(int row, int col) {
        if (!isWithinBounds(row, col)) return null;
        return tiles[row][col].getPlant();
    }

    public void setPlant(int row, int col, Plant plant) {
        if (!isWithinBounds(row, col)) return;
        tiles[row][col].setPlant(plant);
        if (plant != null) {
            plant.setPlanted(true);
            plant.putRuntimeState("row", row);
            plant.putRuntimeState("col", col);
            plant.putRuntimeState("lane", row);
            float tileWidth = getTileWidth();
            float tileHeight = getTileHeight();
            float worldX = startX + col * tileWidth;
            float worldY = startY - (row + 1) * tileHeight;
            plant.putRuntimeState("worldX", worldX);
            plant.putRuntimeState("worldY", worldY);
            plant.putRuntimeState("tileWidth", tileWidth);
            plant.putRuntimeState("tileHeight", tileHeight);
        }
    }

    public void removePlant(int row, int col) {
        if (!isWithinBounds(row, col)) return;
        Plant plant = tiles[row][col].getPlant();
        if (plant != null) {
            plant.setPlanted(false);
        }
        tiles[row][col].setPlant(null);
    }

    public Tile getTile(int row, int col) {
        if (!isWithinBounds(row, col)) return null;
        return tiles[row][col];
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public float getTileWidth() { return totalWidth / cols; }
    public float getTileHeight() { return totalHeight / rows; }
    public float getStartX() { return startX; }
    public float getStartY() { return startY; }
}
