package com.PVZ.model.game;

import com.PVZ.model.minigame.izombie.ZombieOption;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class ZombiePacket {

    private final ZombieOption option;
    private final Rectangle bounds;
    private Texture icon;

    public ZombiePacket(ZombieOption option, Rectangle bounds) {
        this.option = option;
        this.bounds = bounds;
    }

    public ZombieOption getOption() { return option; }
    public Rectangle getBounds() { return bounds; }
    public Texture getIcon() { return icon; }
    public void setIcon(Texture icon) { this.icon = icon; }

    public boolean contains(float worldX, float worldY) {
        return bounds.contains(worldX, worldY);
    }
}
