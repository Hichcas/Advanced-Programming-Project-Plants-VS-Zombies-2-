package com.PVZ.model.entity;

import com.PVZ.model.enums.TileType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Tile {
    TileType type; Plant plant; int gridX, gridY;

    public Tile(TileType type, Plant plant, int gridX, int gridY) {
        this.type = type;
        this.plant = plant;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public void update(float delta) {  }
    public void draw(SpriteBatch batch) { }


    public TileType getType() { return type; }
    public Plant getPlant() { return plant; }
    public void setPlant(Plant plant) { this.plant = plant; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
}
