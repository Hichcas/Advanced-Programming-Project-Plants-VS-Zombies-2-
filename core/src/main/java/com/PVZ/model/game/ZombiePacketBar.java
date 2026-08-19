package com.PVZ.model.game;

import com.PVZ.model.entity.zombies.base.ZombieTexturePaths;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.model.minigame.izombie.IZombieGame;
import com.PVZ.model.minigame.izombie.ZombieOption;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ZombiePacketBar {

    private static final float SLOT_SIZE = 130f;
    private static final float GAP = 3f;
    private static final float PANEL_PAD = 4f;

    private final List<ZombiePacket> packets = new ArrayList<>();
    private final Map<String, Texture> iconCache = new HashMap<>();
    private final Map<String, Boolean> missingLogged = new HashMap<>();
    private Texture darkOverlay;
    private static Drawable panelBackground;
    private static Drawable slotBackground;
    private static boolean skinLookupDone = false;
    private float previewAnimationTime = 0f;

    private static void ensureSkinBackgrounds() {
        if (skinLookupDone) {
            return;
        }
        skinLookupDone = true;
        try {
            com.badlogic.gdx.scenes.scene2d.ui.Skin skin = PvzSkin.get();
            if (skin == null) {
                return;
            }
            if (skin.has("image_ui_dialog_asset_inner_bkgd_10", Drawable.class)) {
                panelBackground = skin.getDrawable("image_ui_dialog_asset_inner_bkgd_10");
            }
            if (skin.has("image_ui_if_bundle_reward1_bg_10", Drawable.class)) {
                slotBackground = skin.getDrawable("image_ui_if_bundle_reward1_bg_10");
            }
        } catch (Exception ignored) {
        }
    }

    public void layout(IZombieGame game, float x, float topY) {
        packets.clear();
        if (game == null) return;
        float y = topY;
        for (ZombieOption option : game.getRoster()) {
            Rectangle bounds = new Rectangle(x, y, SLOT_SIZE, SLOT_SIZE);
            ZombiePacket packet = new ZombiePacket(option, bounds);
            // The icon is a PAM animation, not a Texture/PNG. EntityRenderer draws it below.
            packet.setIcon(null);
            packets.add(packet);
            y -= (SLOT_SIZE + GAP);
        }
    }

    public ZombiePacket getPacketAt(float worldX, float worldY) {
        for (ZombiePacket packet : packets) {
            if (packet.contains(worldX, worldY)) return packet;
        }
        return null;
    }

    public List<ZombiePacket> getPackets() { return packets; }

    public void draw(SpriteBatch batch, BitmapFont font, BitmapFont timerFont, IZombieGame game, String selectedAlias) {
        if (game == null || packets.isEmpty()) return;
        advancePreviewAnimation();
        ensureSkinBackgrounds();
        BitmapFont smallFont = timerFont != null ? timerFont : font;

        // One skinned backing panel behind the whole roster column, instead of
        // icons floating on bare background - matches the rest of the game's UI.
        Rectangle first = packets.get(0).getBounds();
        Rectangle last = packets.get(packets.size() - 1).getBounds();
        float panelX = first.x - PANEL_PAD;
        float panelY = last.y - PANEL_PAD;
        float panelW = first.width + PANEL_PAD * 2f;
        float panelH = (first.y + first.height) - last.y + PANEL_PAD * 2f;
        if (panelBackground != null) {
            panelBackground.draw(batch, panelX, panelY, panelW, panelH);
        } else {
            batch.setColor(0.05f, 0.05f, 0.05f, 0.55f);
            batch.draw(darkOverlayPixel(), panelX, panelY, panelW, panelH);
            batch.setColor(Color.WHITE);
        }

        for (ZombiePacket packet : packets) {
            Rectangle b = packet.getBounds();
            boolean affordable = game.getSun() >= packet.getOption().getCost();
            boolean selected = packet.getOption().getAlias().equalsIgnoreCase(
                    selectedAlias == null ? "" : selectedAlias);

            if (slotBackground != null) {
                slotBackground.draw(batch, b.x, b.y, b.width, b.height);
            }

            boolean pamDrawn = drawPamIcon(batch, packet.getOption().getAlias(), b);
            if (!pamDrawn) {
                // Keep the slot visually clean even if a PAM cannot be loaded.
                // No extra text is rendered here by design.
            }

            if (!affordable) {
                batch.setColor(0f, 0f, 0f, 0.50f);
                batch.draw(darkOverlayPixel(), b.x, b.y, b.width, b.height);
                batch.setColor(Color.WHITE);
            } else if (selected) {
                batch.setColor(1f, 0.85f, 0f, 0.28f);
                batch.draw(darkOverlayPixel(), b.x, b.y, b.width, b.height);
                batch.setColor(Color.WHITE);
            }
        }
    }

    private boolean drawPamIcon(SpriteBatch batch, String alias, Rectangle b) {
        if (EntityRenderer.getInstance() == null || alias == null) return false;

        // Render the actual zombie alias directly from its PAM rather than constructing a
        // gameplay zombie. This makes the roster preview independent of combat state and
        // also works for aliases whose gameplay class has custom initialisation.
        try {
            // Scale the PAM up while keeping its full canvas inside the packet.
            float scale = 0.48f;
            float px = b.x + b.width * 0.5f;
            float py = b.y + b.height * 0.5f;
            EntityRenderer.getInstance().renderZombieAlias(
                    batch, alias, "idle", previewAnimationTime, px, py, scale);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private void advancePreviewAnimation() {
        try {
            previewAnimationTime += Gdx.graphics.getDeltaTime();
        } catch (Exception ignored) {
            previewAnimationTime += 0.016f;
        }
        if (previewAnimationTime > 60f) previewAnimationTime -= 60f;
    }

    private Texture darkOverlayPixel() {
        if (darkOverlay == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(1f, 1f, 1f, 1f);
            pixmap.fill();
            darkOverlay = new Texture(pixmap);
            pixmap.dispose();
        }
        return darkOverlay;
    }

    public void dispose() {
        if (darkOverlay != null) darkOverlay.dispose();
        for (Texture t : iconCache.values()) {
            if (t != null) t.dispose();
        }
    }
}

