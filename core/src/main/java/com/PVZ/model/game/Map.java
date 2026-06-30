package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.enums.TileType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Map {
    private Tile[][] tiles;
    private int rows, cols;
    private static final int TILE_W = 100;
    private static final int TILE_H = 100;

    public Map(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        tiles = new Tile[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c] = new Tile(TileType.NORMAL,null , r, c);
            }
        }
    }

    public void render(SpriteBatch batch) {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                tiles[r][c].draw(batch);
            }
        }
    }

    public Plant getPlantAt(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return null;
        return tiles[row][col].getPlant();
    }

    public void setPlant(int row, int col, Plant plant) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return;
        tiles[row][col].setPlant(plant);
    }

    public void removePlant(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return;
        tiles[row][col].setPlant(null);
    }

    public Tile getTile(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return null;
        return tiles[row][col];
    }
}
