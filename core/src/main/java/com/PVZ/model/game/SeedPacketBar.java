package com.PVZ.model.game;

import com.PVZ.model.enums.PlantType;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * The strip of unlocked-plant seed packets shown at the top of the game screen. Clicking a
 * packet (via {@link RegularInputProcessor}) selects that plant type on the engine; the next
 * tile click then plants it there, mirroring the real PvZ seed-picker flow.
 *
 * Rendering is placeholder-friendly: each slot draws as a colored box + plant name until a
 * real icon texture is attached with {@link #setIcon(PlantType, Texture)}.
 */
public class SeedPacketBar {

    private static final float SLOT_SIZE = 110f;
    private static final float GAP = 12f;

    private final List<SeedPacket> packets = new ArrayList<>();

    /** Lays out one packet per unlocked plant, left to right, starting at (startX, startY). */
    public void layout(List<PlantType> unlockedPlants, float startX, float startY) {
        packets.clear();
        float x = startX;
        for (PlantType type : unlockedPlants) {
            Rectangle bounds = new Rectangle(x, startY, SLOT_SIZE, SLOT_SIZE);
            packets.add(new SeedPacket(type, bounds));
            x += SLOT_SIZE + GAP;
        }
    }

    public void setIcon(PlantType type, Texture texture) {
        for (SeedPacket packet : packets) {
            if (packet.getPlantType() == type) {
                packet.setIcon(texture);
            }
        }
    }

    /** Returns the packet under the given world coordinates, or null if none. */
    public SeedPacket getPacketAt(float worldX, float worldY) {
        for (SeedPacket packet : packets) {
            if (packet.contains(worldX, worldY)) {
                return packet;
            }
        }
        return null;
    }

    public List<SeedPacket> getPackets() {
        return packets;
    }

    public void drawBackgrounds(ShapeRenderer sr, RegularGameEngine engine, PlantType selected) {
        for (SeedPacket packet : packets) {
            Rectangle b = packet.getBounds();
            boolean affordable = engine == null
                    || packet.getPlantType().getDefinition() == null
                    || engine.getSunCount() >= packet.getPlantType().getDefinition().getCost();
            boolean onCooldown = engine != null && engine.isOnCooldown(packet.getPlantType());

            if (packet.getPlantType() == selected) {
                sr.setColor(Color.GOLD);
            } else if (onCooldown || !affordable) {
                sr.setColor(Color.DARK_GRAY);
            } else {
                sr.setColor(Color.FOREST);
            }
            sr.rect(b.x, b.y, b.width, b.height);
        }
    }

    public void drawIconsAndLabels(SpriteBatch batch, BitmapFont font) {
        for (SeedPacket packet : packets) {
            Rectangle b = packet.getBounds();
            if (packet.getIcon() != null) {
                batch.draw(packet.getIcon(), b.x, b.y, b.width, b.height);
            } else {
                font.setColor(Color.WHITE);
                font.draw(batch, packet.getPlantType().getDisplayName(), b.x + 4, b.y + b.height - 8, b.width - 8, -1, true);
            }
        }
    }
}
