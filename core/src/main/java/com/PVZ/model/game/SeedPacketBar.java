package com.PVZ.model.game;

import com.PVZ.model.entity.PlantTexturePaths;
import com.PVZ.model.enums.PlantType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The strip of unlocked-plant seed packets shown at the top of the game screen. Clicking a
 * packet (via {@link RegularInputProcessor}) selects that plant type on the engine; the next
 * tile click then plants it there, mirroring the real PvZ seed-picker flow.
 *
 * NOTE: {@link #layout} is called every frame by GameScreen (to refresh the loadout), which
 * rebuilds all {@link SeedPacket} objects from scratch — so per-packet icons set via
 * {@link #setIcon} would get wiped out every frame. Icons are therefore cached here, keyed by
 * PlantType, and re-attached to the fresh packets on every layout() call.
 */
public class SeedPacketBar {
    static private final float Xoffset = 600f;

    private static final float SLOT_SIZE = 110f;
    private static final float GAP = 12f;

    private final List<SeedPacket> packets = new ArrayList<>();
    private final Map<PlantType, Texture> iconCache = new HashMap<>();
    private final Map<PlantType, Boolean> missingLogged = new HashMap<>();

    /** Lays out one packet per unlocked plant, left to right, starting at (startX, startY). */
    public void layout(List<PlantType> unlockedPlants, float startX, float startY) {
        packets.clear();
        float x = startX + Xoffset;   // <-- آفست اینجا اضافه بشه
        for (PlantType type : unlockedPlants) {
            Rectangle bounds = new Rectangle(x, startY, SLOT_SIZE, SLOT_SIZE);
            SeedPacket packet = new SeedPacket(type, bounds);
            packet.setIcon(getOrLoadIcon(type));
            packets.add(packet);
            x += SLOT_SIZE + GAP;
        }
    }

    public void setIcon(PlantType type, Texture texture) {
        iconCache.put(type, texture);
        for (SeedPacket packet : packets) {
            if (packet.getPlantType() == type) {
                packet.setIcon(texture);
            }
        }
    }

    /**
     * Loads (once) and caches the real Plants/*.png icon for this plant type. Prints a single
     * console line the first time a given plant's icon is missing, so it's obvious from the
     * command-line run which file name/path to fix — without spamming every frame.
     */
    private Texture getOrLoadIcon(PlantType type) {
        if (type == null) {
            return null;
        }
        if (iconCache.containsKey(type)) {
            return iconCache.get(type);
        }
        String path = PlantTexturePaths.getPath(type.name());
        Texture texture = null;
        try {
            if (Gdx.files.internal(path).exists()) {
                texture = new Texture(Gdx.files.internal(path));
            }
        } catch (RuntimeException ex) {
            texture = null;
        }
        if (texture == null && !missingLogged.containsKey(type)) {
            missingLogged.put(type, Boolean.TRUE);
            System.out.println("[SeedPacketBar] no icon found for " + type.name() + " at assets/" + path
                + " -> falling back to label box");
        }
        iconCache.put(type, texture);
        return texture;
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

            // once a real icon is loaded, skip painting a solid background box over it —
            // only draw a thin selection/status tint so the artwork stays visible.
            if (packet.getIcon() != null && packet.getPlantType() != selected && !onCooldown && affordable) {
                continue;
            }

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
