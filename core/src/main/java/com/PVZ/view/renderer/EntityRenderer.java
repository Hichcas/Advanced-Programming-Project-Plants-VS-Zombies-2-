package com.PVZ.view.renderer;

import pvz.libpvz.textures.TextureBank;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.pam.ClipRef;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieTexturePaths;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;
import java.util.Map;

public class EntityRenderer {
    private static EntityRenderer instance;

    private final TextureBank textures;
    private final PamPlayer pamPlayer;
    private final Map<String, ClipRef> walkClips = new HashMap<>();

    private EntityRenderer() {
        com.badlogic.gdx.files.FileHandle assetsFolder = Gdx.files.internal("assets/pvz-assets");
        if (!assetsFolder.exists()) {
            assetsFolder = Gdx.files.internal("pvz-assets");
        }
        this.textures = new TextureBank("768", assetsFolder);
        this.pamPlayer = new PamPlayer(textures, assetsFolder);
    }

    public static EntityRenderer getInstance() {
        if (instance == null) {
            instance = new EntityRenderer();
        }
        return instance;
    }

    public void update() {
        textures.update();
    }

    public ClipRef getWalkClip(String alias) {
        if (!walkClips.containsKey(alias)) {
            String pamPath = ZombieTexturePaths.getPamPath(alias);
            try {
                pamPlayer.loadSync(pamPath);
                ClipRef clip = pamPlayer.getClip(pamPath, "walk");
                if (clip == null) {
                    java.util.List<String> available = pamPlayer.clips(pamPath);
                    if (available != null && !available.isEmpty()) {
                        clip = pamPlayer.getClip(pamPath, available.get(0));
                    }
                }
                walkClips.put(alias, clip);
            } catch (Exception e) {
                System.err.println("EntityRenderer: Failed to load PAM for alias " + alias + ": " + e.getMessage());
                walkClips.put(alias, null);
            }
        }
        return walkClips.get(alias);
    }

    public void renderZombie(SpriteBatch batch, Zombie zombie, float stateTime) {
        textures.update();
        ClipRef walkClip = getWalkClip(zombie.getAlias());

        if (walkClip == null) {
            walkClip = getWalkClip("DEFAULT");
        }

        if (walkClip != null) {
            pamPlayer.draw(batch, walkClip, stateTime, (float) zombie.getX(), (float) zombie.getY(), true);
        }
    }

    public TextureBank getTextures() {
        return textures;
    }

    public PamPlayer getPamPlayer() {
        return pamPlayer;
    }
}
