package com.PVZ.model.entity;

import com.PVZ.model.enums.TileType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Tile {
    private TileType type;
    private Plant plant;
    private Plant basePlant;
    private int octopusHp;
    private int hp;
    private int gridRow, gridCol;

    private float worldX, worldY;
    private float width, height;

    private com.PVZ.model.enums.GraveVariant graveVariant;
    private int maxHp = 700;
    private float graveAnimTime = 0f;

    public Tile(TileType type, Plant plant, int gridRow, int gridCol,
                float worldX, float worldY, float width, float height) {
        this.type = type;
        this.plant = plant;
        this.basePlant = null;
        this.octopusHp = 0;
        this.hp = 0;
        this.maxHp = 700;
        this.graveVariant = null;
        this.graveAnimTime = 0f;
        this.gridRow = gridRow;
        this.gridCol = gridCol;
        this.worldX = worldX;
        this.worldY = worldY;
        this.width = width;
        this.height = height;
    }

    public void update(float delta) {
        if (isGrave()) {
            graveAnimTime += delta;
        }
    }

    public void drawBorder(ShapeRenderer sr) {
        switch (type) {
            case ICE -> sr.setColor(Color.CYAN);
            case SLIPPERY_UP -> sr.setColor(new Color(0.3f, 0.5f, 1f, 1f));
            case SLIPPERY_DOWN -> sr.setColor(new Color(0.3f, 0.5f, 1f, 1f));
            case TOMBSTONE -> sr.setColor(new Color(0.5f, 0.35f, 0.2f, 1f));
            default -> sr.setColor(new Color(0.95f, 0.95f, 0.95f, 0.15f));
        }
        sr.rect(worldX, worldY, width, height);
    }

    public boolean contains(float px, float py) {
        return px >= this.worldX && px <= this.worldX + this.width &&
            py >= this.worldY && py <= this.worldY + this.height;
    }


    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public Plant getBasePlant() {
        return basePlant;
    }

    public void setBasePlant(Plant basePlant) {
        this.basePlant = basePlant;
    }

    public int getOctopusHp() {
        return octopusHp;
    }

    public void setOctopusHp(int octopusHp) {
        this.octopusHp = octopusHp;
    }

    public int getGridRow() {
        return gridRow;
    }

    public int getGridCol() {
        return gridCol;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public float getX() {
        return worldX;
    }

    public float getY() {
        return worldY;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public boolean isGrave() {
        return type == TileType.TOMBSTONE || type == TileType.NECROMANCY;
    }

    public com.PVZ.model.enums.GraveVariant getGraveVariant() {
        return graveVariant;
    }

    public void setGraveVariant(com.PVZ.model.enums.GraveVariant graveVariant) {
        this.graveVariant = graveVariant;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public float getGraveAnimTime() {
        return graveAnimTime;
    }

    public void setGraveAnimTime(float graveAnimTime) {
        this.graveAnimTime = graveAnimTime;
    }
}
