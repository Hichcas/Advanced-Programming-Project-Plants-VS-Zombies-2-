package com.PVZ.model.entity;

import com.PVZ.model.enums.TileType;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;

public class Tile {
    private TileType type;
    private Plant plant;
    private int hp;
    private int gridRow, gridCol;   // موقعیت در شبکه (برای منطق)

    // موقعیت و اندازه در دنیای بازی
    private float worldX, worldY;
    private float width, height;

    public Tile(TileType type, Plant plant, int gridRow, int gridCol,
                float worldX, float worldY, float width, float height) {
        this.type = type;
        this.plant = plant;
        this.gridRow = gridRow;
        this.gridCol = gridCol;
        this.worldX = worldX;
        this.worldY = worldY;
        this.width = width;
        this.height = height;
    }

    public void update(float delta) {
        // فعلاً خالی
    }

    public void drawBorder(ShapeRenderer sr) {
        switch (type) {
            case ICE -> sr.setColor(Color.CYAN);
            case SLIPPERY_UP -> sr.setColor(new Color(0.3f, 0.5f, 1f, 1f));
            case SLIPPERY_DOWN -> sr.setColor(new Color(0.3f, 0.5f, 1f, 1f));
            case TOMBSTONE -> sr.setColor(new Color(0.5f, 0.35f, 0.2f, 1f));
            // NORMAL tiles: draw a very faint border so the map grid is still
            // subtly visible without dominating the view or looking like a "blue bug".
            default -> sr.setColor(new Color(0.95f, 0.95f, 0.95f, 0.15f));
        }
        sr.rect(worldX, worldY, width, height);
    }

    public boolean contains(float px, float py) {
        return px >= this.worldX && px <= this.worldX + this.width &&
            py >= this.worldY && py <= this.worldY + this.height;
    }


    // getters & setters
    public TileType getType() { return type; }
    public void setType(TileType type) { this.type = type; }
    public Plant getPlant() { return plant; }
    public void setPlant(Plant plant) { this.plant = plant; }
    public int getGridRow() { return gridRow; }
    public int getGridCol() { return gridCol; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public float getX() { return worldX; }
    public float getY() { return worldY; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
}
