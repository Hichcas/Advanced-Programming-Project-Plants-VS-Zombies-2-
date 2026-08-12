package com.PVZ.model.game;

import com.PVZ.model.entity.zombies.base.ZombieTexturePaths;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.model.minigame.izombie.IZombieGame;
import com.PVZ.model.minigame.izombie.ZombieOption;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ZombiePacketBar {

    private static final float SLOT_SIZE = 78f;
    private static final float GAP = 10f;

    private final List<ZombiePacket> packets = new ArrayList<>();
    private final Map<String, Texture> iconCache = new HashMap<>();
    private final Map<String, Boolean> missingLogged = new HashMap<>();
    private Texture darkOverlay;
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
        if (game == null) return;
        BitmapFont smallFont = timerFont != null ? timerFont : font;
        for (ZombiePacket packet : packets) {
            Rectangle b = packet.getBounds();
            boolean affordable = game.getSun() >= packet.getOption().getCost();
            boolean selected = packet.getOption().getAlias().equalsIgnoreCase(
                    selectedAlias == null ? "" : selectedAlias);

            boolean pamDrawn = drawPamIcon(batch, packet.getOption().getAlias(), b);
            if (!pamDrawn) {
                smallFont.setColor(Color.WHITE);
                smallFont.draw(batch, packet.getOption().getDisplayName(), b.x + 4, b.y + b.height - 8, b.width - 8, -1,
                        true);
            }

            if (!affordable) {
                batch.setColor(0f, 0f, 0f, 0.55f);
                batch.draw(darkOverlayPixel(), b.x, b.y, b.width, b.height);
                batch.setColor(Color.WHITE);

                int missing = packet.getOption().getCost() - game.getSun();
                double rate = game.getCurrentSunRate();
                String timerText = rate > 0 ? String.valueOf((int) Math.ceil(missing / rate)) : "-";
                smallFont.setColor(1f, 0.6f, 0.6f, 1f);
                smallFont.draw(batch, timerText, b.x, b.y + b.height * 0.6f, b.width, 1, true);
                smallFont.setColor(Color.WHITE);
            } else if (selected) {
                batch.setColor(1f, 0.85f, 0f, 0.35f);
                batch.draw(darkOverlayPixel(), b.x, b.y, b.width, b.height);
                batch.setColor(Color.WHITE);
            }

            smallFont.setColor(Color.WHITE);
            smallFont.draw(batch, packet.getOption().getCost() + "", b.x, b.y - 2, b.width, 1, true);
        }
    }

    private boolean drawPamIcon(SpriteBatch batch, String alias, Rectangle b) {
        String pamPath = ZombieTexturePaths.getPamPath(alias);
        if (pamPath == null || EntityRenderer.getInstance() == null) return false;
        try {
            Matrix4 original = new Matrix4(batch.getTransformMatrix());
            float scale = 0.20f;
            float cx = b.x + b.width * 0.5f;
            float cy = b.y + b.height * 0.5f;
            Matrix4 hud = new Matrix4(original);
            hud.translate(cx, cy, 0f);
            hud.scale(scale, scale, 1f);
            hud.translate(-195f, -195f, 0f);
            batch.setTransformMatrix(hud);
            boolean ok = EntityRenderer.getInstance().renderPam(batch, pamPath, "idle", 0f, 0f, 0f);
            batch.setTransformMatrix(original);
            return ok;
        } catch (RuntimeException ex) {
            return false;
        }
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

