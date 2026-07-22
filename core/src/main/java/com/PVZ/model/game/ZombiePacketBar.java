package com.PVZ.model.game;

import com.PVZ.model.entity.zombies.base.ZombieTexturePaths;
import com.PVZ.model.minigame.izombie.IZombieGame;
import com.PVZ.model.minigame.izombie.ZombieOption;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
            packet.setIcon(getOrLoadIcon(option.getAlias()));
            packets.add(packet);
            y -= (SLOT_SIZE + GAP);
        }
    }

    private Texture getOrLoadIcon(String alias) {
        if (alias == null) return null;
        if (iconCache.containsKey(alias)) return iconCache.get(alias);
        String path = ZombieTexturePaths.getPath(alias);
        Texture texture = null;
        try {
            if (path != null && Gdx.files.internal(path).exists()) {
                texture = new Texture(Gdx.files.internal(path));
            }
        } catch (RuntimeException ex) {
            texture = null;
        }
        if (texture == null && !missingLogged.containsKey(alias)) {
            missingLogged.put(alias, Boolean.TRUE);
            System.out.println("[ZombiePacketBar] no icon found for " + alias + " at assets/" + path
                    + " -> falling back to label box");
        }
        iconCache.put(alias, texture);
        return texture;
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

            if (packet.getIcon() != null) {
                batch.setColor(Color.WHITE);
                batch.draw(packet.getIcon(), b.x, b.y, b.width, b.height);
            } else {
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

