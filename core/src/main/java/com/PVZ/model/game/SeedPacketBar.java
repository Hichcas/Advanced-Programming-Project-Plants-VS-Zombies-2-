package com.PVZ.model.game;

import com.PVZ.model.entity.PlantTexturePaths;
import com.PVZ.model.enums.PlantType;
import com.PVZ.view.renderer.EntityRenderer;
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

public class SeedPacketBar {

    private static final float X_OFFSET = 600f;
    private static final float SLOT_SIZE = 110f;
    private static final float GAP = 12f;
    private static final float VERTICAL_SLOT_SIZE = 120f;
    private static final float VERTICAL_GAP = 14f;

    private final List<SeedPacket> packets = new ArrayList<>();
    private final Map<PlantType, Texture> iconCache = new HashMap<>();
    private final Map<PlantType, Boolean> missingLogged = new HashMap<>();

    public void layout(List<PlantType> unlockedPlants, float startX, float startY) {
        layout(unlockedPlants, startX, startY, true);
    }

    public void layout(List<PlantType> unlockedPlants, float startX, float startY, boolean applyDefaultOffset) {
        packets.clear();
        float x = applyDefaultOffset ? startX + X_OFFSET : startX;
        for (PlantType type : unlockedPlants) {
            Rectangle bounds = new Rectangle(x, startY, SLOT_SIZE, SLOT_SIZE);
            SeedPacket packet = new SeedPacket(type, bounds);
            packet.setIcon(getOrLoadIcon(type));
            packets.add(packet);
            x += SLOT_SIZE + GAP;
        }
    }

    public void layoutVertical(List<PlantType> unlockedPlants, float startX, float startY) {
        packets.clear();
        float y = startY;
        for (PlantType type : unlockedPlants) {
            Rectangle bounds = new Rectangle(startX, y, VERTICAL_SLOT_SIZE, VERTICAL_SLOT_SIZE);
            SeedPacket packet = new SeedPacket(type, bounds);
            packet.setIcon(getOrLoadIcon(type));
            packets.add(packet);
            y -= VERTICAL_SLOT_SIZE + VERTICAL_GAP;
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

    public void drawBackgrounds(ShapeRenderer sr, SeedBarEngine engine, PlantType selected) {
        for (SeedPacket packet : packets) {
            Rectangle b = packet.getBounds();
            boolean affordable = engine == null
                || engine.isConveyorBeltMode()
                || packet.getPlantType().getDefinition() == null
                || engine.getSunCount() >= packet.getPlantType().getDefinition().getCost();
            boolean onCooldown = engine != null && !engine.isConveyorBeltMode()
                && engine.isOnCooldown(packet.getPlantType());

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
        drawIconsAndLabels(batch, font, null);
    }

    /** Advances once per draw call so every packet's idle animation plays in sync. */
    private float animTime = 0f;

    public void drawIconsAndLabels(SpriteBatch batch, BitmapFont font, SeedBarEngine engine) {
        animTime += Gdx.graphics.getDeltaTime();
        for (SeedPacket packet : packets) {
            Rectangle b = packet.getBounds();
            float centerX = b.x + b.width / 2f;
            float centerY = b.y + b.height * 0.55f;
            boolean drewAnimated = EntityRenderer.getInstance()
                .renderPlant(batch, packet.getPlantType().name(), animTime, centerX, centerY);
            if (!drewAnimated) {
                // Fall back to the static icon (or the plain label if even that is missing)
                // so nothing on the bar ever silently disappears.
                if (packet.getIcon() != null) {
                    batch.draw(packet.getIcon(), b.x, b.y, b.width, b.height);
                } else {
                    font.setColor(Color.WHITE);
                    font.draw(batch, packet.getPlantType().getDisplayName(), b.x + 4, b.y + b.height - 8, b.width - 8, -1,
                        true);
                }
            }

            if (engine == null) {
                continue;
            }
            double remaining = engine.getRechargeRemainingSeconds(packet.getPlantType());
            if (remaining <= 0) {
                continue;
            }

            batch.setColor(0f, 0f, 0f, 0.55f);
            batch.draw(darkOverlayPixel(), b.x, b.y, b.width, b.height);
            batch.setColor(Color.WHITE);

            font.setColor(Color.WHITE);
            String countdown = (Math.ceil(remaining * 10) / 10.0) + "s";
            font.draw(batch, countdown, b.x, b.y + b.height / 2f + 8, b.width, 1, true);
        }
    }

    private com.badlogic.gdx.graphics.Texture darkOverlayTexture;

    private com.badlogic.gdx.graphics.Texture darkOverlayPixel() {
        if (darkOverlayTexture == null) {
            com.badlogic.gdx.graphics.Pixmap pixmap =
                new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.fill();
            darkOverlayTexture = new com.badlogic.gdx.graphics.Texture(pixmap);
            pixmap.dispose();
        }
        return darkOverlayTexture;
    }
}
