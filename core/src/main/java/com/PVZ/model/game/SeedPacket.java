package com.PVZ.model.game;

import com.PVZ.model.enums.PlantType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

/**
 * A single clickable seed-packet slot in the {@link SeedPacketBar}. Holds the plant it
 * represents plus its on-screen hitbox. {@link #icon} is optional so the bar still works
 * (as a colored placeholder box) before real artwork is dropped in.
 */
public class SeedPacket {

    private final PlantType plantType;
    private final Rectangle bounds;
    private Texture icon;

    public SeedPacket(PlantType plantType, Rectangle bounds) {
        this.plantType = plantType;
        this.bounds = bounds;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Texture getIcon() {
        return icon;
    }

    public void setIcon(Texture icon) {
        this.icon = icon;
    }

    public boolean contains(float worldX, float worldY) {
        return bounds.contains(worldX, worldY);
    }
}
