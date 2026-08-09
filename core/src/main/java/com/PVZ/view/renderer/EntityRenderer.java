package com.PVZ.view.renderer;

import pvz.libpvz.textures.TextureBank;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.pam.ClipRef;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieTexturePaths;
import com.PVZ.model.entity.PlantTexturePaths;
import com.PVZ.model.entity.PamAnimationCatalog;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.HashMap;
import java.util.Map;

public class EntityRenderer {
    private static EntityRenderer instance;

    private final TextureBank textures;
    private final PamPlayer pamPlayer;
    private final Map<String, ClipRef> walkClips = new HashMap<>();
    private final Map<String, ClipRef> plantClips = new HashMap<>();
    private final Map<String, Boolean> plantPamFailed = new HashMap<>();
    private final java.util.Set<String> loggedPams = new java.util.HashSet<>();

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
                String resolvedName = PamAnimationCatalog.resolveClip(pamPath, "walk");
                ClipRef clip = resolvedName != null ? pamPlayer.getClip(pamPath, resolvedName) : null;
                if (clip == null) {
                    clip = pamPlayer.getClip(pamPath, "walk");
                }
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


    public ClipRef getPlantClip(String plantTypeName, String state) {
        if (plantTypeName == null) {
            return null;
        }
        String cacheKey = plantTypeName + "#" + state;
        if (plantClips.containsKey(cacheKey)) {
            return plantClips.get(cacheKey);
        }
        if (Boolean.TRUE.equals(plantPamFailed.get(plantTypeName))) {
            return null;
        }
        String pamPath = PlantTexturePaths.getPamPath(plantTypeName);
        if (pamPath == null) {
            plantPamFailed.put(plantTypeName, true);
            return null;
        }
        try {
            pamPlayer.loadSync(pamPath);

            if (loggedPams.add(pamPath)) {
                java.util.Set<String> catalogClips = PamAnimationCatalog.clipNames(pamPath);
                java.util.List<String> runtimeClips = pamPlayer.clips(pamPath);
                System.out.println("[EntityRenderer] " + plantTypeName + " (" + pamPath
                    + ") catalog clips: " + catalogClips + " | runtime clips: " + runtimeClips);
            }

            String resolvedName = PamAnimationCatalog.resolveClip(pamPath, state);
            ClipRef clip = resolvedName != null ? pamPlayer.getClip(pamPath, resolvedName) : null;

            if (clip == null) {
                java.util.List<String> available = pamPlayer.clips(pamPath);
                if (available != null) {
                    for (String name : available) {
                        if (name != null && state != null && name.toLowerCase().contains(state.toLowerCase())) {
                            clip = pamPlayer.getClip(pamPath, name);
                            if (clip != null) break;
                        }
                    }
                    if (clip == null && !available.isEmpty()) {
                        clip = pamPlayer.getClip(pamPath, available.get(0));
                    }
                }
            }

            if (clip != null) {
                plantClips.put(cacheKey, clip);
            } else {
                plantPamFailed.put(plantTypeName, true);
            }
            return clip;
        } catch (Exception e) {
            System.err.println("EntityRenderer: Failed to load PAM for plant " + plantTypeName + ": " + e.getMessage());
            plantPamFailed.put(plantTypeName, true);
            return null;
        }
    }

    public ClipRef getPlantClip(String plantTypeName) {
        return getPlantClip(plantTypeName, "idle");
    }


    public ClipRef getPlantClipExact(String plantTypeName, String exactClipName) {
        if (plantTypeName == null || exactClipName == null) {
            return null;
        }
        String cacheKey = plantTypeName + "#exact#" + exactClipName;
        if (plantClips.containsKey(cacheKey)) {
            return plantClips.get(cacheKey);
        }
        String pamPath = PlantTexturePaths.getPamPath(plantTypeName);
        if (pamPath == null) {
            return null;
        }
        try {
            pamPlayer.loadSync(pamPath);
            ClipRef clip = pamPlayer.getClip(pamPath, exactClipName);
            plantClips.put(cacheKey, clip);
            return clip;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean renderPlantExact(SpriteBatch batch, String plantTypeName, String exactClipName,
                                     float stateTime, float x, float y) {
        textures.update();
        ClipRef clip = getPlantClipExact(plantTypeName, exactClipName);
        if (clip == null) {
            return false;
        }
        pamPlayer.draw(batch, clip, stateTime, x, y, true);
        return true;
    }

    public boolean renderPlant(SpriteBatch batch, String plantTypeName, String state, float stateTime,
                                float x, float y) {
        textures.update();
        ClipRef clip = getPlantClip(plantTypeName, state);
        if (clip == null) {
            return false;
        }
        pamPlayer.draw(batch, clip, stateTime, x, y, true);
        return true;
    }

    public boolean renderPlant(SpriteBatch batch, String plantTypeName, float stateTime,
                                float x, float y) {
        return renderPlant(batch, plantTypeName, "idle", stateTime, x, y);
    }

    private final Map<String, ClipRef> projectileClips = new HashMap<>();
    private final Map<String, Boolean> projectilePamFailed = new HashMap<>();

    /**
     * Draws a projectile using its real PAM effect (e.g. the flying pea sprite) instead of
     * a procedural placeholder shape.
     * @return true if drawn, false if this visual key has no PAM mapping / failed to load —
     *         caller (Projectile.draw) should fall back to the pixmap shape in that case.
     */
    public boolean renderProjectile(SpriteBatch batch, String visualKey, float stateTime, float x, float y) {
        if (visualKey == null) {
            return false;
        }
        textures.update();
        ClipRef clip = projectileClips.get(visualKey);
        if (clip == null) {
            if (Boolean.TRUE.equals(projectilePamFailed.get(visualKey))) {
                return false;
            }
            String pamPath = com.PVZ.model.entity.plants.behavior.impl.ProjectileVisuals.getPath(visualKey);
            if (pamPath == null) {
                projectilePamFailed.put(visualKey, true);
                return false;
            }
            try {
                pamPlayer.loadSync(pamPath);
                java.util.List<String> available = pamPlayer.clips(pamPath);
                if (available != null && !available.isEmpty()) {
                    clip = pamPlayer.getClip(pamPath, available.get(0));
                }
                if (clip == null) {
                    projectilePamFailed.put(visualKey, true);
                    return false;
                }
                projectileClips.put(visualKey, clip);
            } catch (Exception e) {
                System.err.println("EntityRenderer: Failed to load projectile PAM " + visualKey + ": " + e.getMessage());
                projectilePamFailed.put(visualKey, true);
                return false;
            }
        }
        pamPlayer.draw(batch, clip, stateTime, x, y, true);
        return true;
    }

    private final Map<String, ClipRef> genericPamClips = new HashMap<>();
    private final Map<String, Boolean> genericPamFailed = new HashMap<>();

    /**
     * Draws any simple, single-purpose PAM (e.g. a UI icon like the "locked" overlay) by its
     * raw asset path, using whatever its first clip is. Reusable anywhere a one-off PAM visual
     * is needed without a dedicated per-entity-type helper (unlike renderPlant/renderProjectile,
     * which pick a specific clip by canonical state).
     */
    public boolean renderPam(SpriteBatch batch, String pamPath, float stateTime, float x, float y) {
        if (pamPath == null) {
            return false;
        }
        textures.update();
        ClipRef clip = genericPamClips.get(pamPath);
        if (clip == null) {
            if (Boolean.TRUE.equals(genericPamFailed.get(pamPath))) {
                return false;
            }
            try {
                pamPlayer.loadSync(pamPath);
                java.util.List<String> available = pamPlayer.clips(pamPath);
                if (available != null && !available.isEmpty()) {
                    clip = pamPlayer.getClip(pamPath, available.get(0));
                }
                if (clip == null) {
                    genericPamFailed.put(pamPath, true);
                    return false;
                }
                genericPamClips.put(pamPath, clip);
            } catch (Exception e) {
                System.err.println("EntityRenderer: Failed to load PAM " + pamPath + ": " + e.getMessage());
                genericPamFailed.put(pamPath, true);
                return false;
            }
        }
        pamPlayer.draw(batch, clip, stateTime, x, y, true);
        return true;
    }

    public TextureBank getTextures() {
        return textures;
    }

    public PamPlayer getPamPlayer() {
        return pamPlayer;
    }
}
