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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import pvz.skin.PvzSkin;

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

    private boolean lastLayoutWasVertical = false;

    public void layout(List<PlantType> unlockedPlants, float startX, float startY, boolean applyDefaultOffset) {
        packets.clear();
        lastLayoutWasVertical = false;
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
        lastLayoutWasVertical = true;
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
                || engine.getSunCount() >= com.PVZ.model.entity.plants.PlantLibrary.getEffectiveCost(packet.getPlantType());
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
        drawIconsAndLabels(batch, font, engine, null);
    }

    public void drawIconsAndLabels(SpriteBatch batch, BitmapFont font, SeedBarEngine engine, PlantType selectedPlant) {
        animTime += Gdx.graphics.getDeltaTime();

        if (lastLayoutWasVertical && !packets.isEmpty()) {
            drawVerticalBarBackground(batch);
        }

        for (SeedPacket packet : packets) {
            Rectangle b = packet.getBounds();
            // Draw backing slot frame
            batch.setColor(0.1f, 0.1f, 0.1f, 0.6f);
            batch.draw(darkOverlayPixel(), b.x, b.y, b.width, b.height);
            if (packet.getPlantType() == selectedPlant) {
                batch.setColor(1f, 0.85f, 0.2f, 0.9f);
                // Draw 3px border
                batch.draw(darkOverlayPixel(), b.x - 3f, b.y - 3f, b.width + 6f, 3f);
                batch.draw(darkOverlayPixel(), b.x - 3f, b.y + b.height, b.width + 6f, 3f);
                batch.draw(darkOverlayPixel(), b.x - 3f, b.y, 3f, b.height);
                batch.draw(darkOverlayPixel(), b.x + b.width, b.y, 3f, b.height);
            }
            batch.setColor(Color.WHITE);

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

    private static Drawable verticalBarBackground;
    private static boolean verticalBarSkinLookupDone = false;

    /**
     * Thin skinned panel behind the conveyor-belt's vertical plant bar so it reads
     * as a proper belt/tray, not icons floating loose over the lawn. Same skin key
     * used for every other panel background in the game, with a plain dark
     * fallback if the skin isn't loaded for some reason.
     */
    private void drawVerticalBarBackground(SpriteBatch batch) {
        if (!verticalBarSkinLookupDone) {
            verticalBarSkinLookupDone = true;
            try {
                com.badlogic.gdx.scenes.scene2d.ui.Skin skin = PvzSkin.get();
                if (skin != null && skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
                    verticalBarBackground = skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
                }
            } catch (Exception ignored) {
            }
        }

        Rectangle first = packets.get(0).getBounds();
        Rectangle last = packets.get(packets.size() - 1).getBounds();
        float pad = 10f;
        float panelX = first.x - pad;
        float panelY = last.y - pad;
        float panelW = first.width + pad * 2f;
        float panelH = (first.y + first.height) - last.y + pad * 2f;

        if (verticalBarBackground != null) {
            verticalBarBackground.draw(batch, panelX, panelY, panelW, panelH);
        } else {
            batch.setColor(0.05f, 0.05f, 0.05f, 0.55f);
            batch.draw(darkOverlayPixel(), panelX, panelY, panelW, panelH);
            batch.setColor(Color.WHITE);
        }
    }

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
