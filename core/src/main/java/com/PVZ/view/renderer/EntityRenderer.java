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

import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.enums.DamageType;
import com.badlogic.gdx.graphics.Color;

public class EntityRenderer {
    private static EntityRenderer instance;

    private final TextureBank textures;
    private final PamPlayer pamPlayer;
    private final Map<String, ClipRef> walkClips = new HashMap<>();
    private final Map<String, ClipRef> zombieClips = new HashMap<>();
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

    public ClipRef getZombieClip(String effectiveAlias, String state) {
        if (effectiveAlias == null || state == null) {
            return null;
        }
        String cacheKey = effectiveAlias + "#" + state;
        if (zombieClips.containsKey(cacheKey)) {
            return zombieClips.get(cacheKey);
        }
        String pamPath = ZombieTexturePaths.getPamPath(effectiveAlias);
        try {
            pamPlayer.loadSync(pamPath);

            if (loggedPams.add(pamPath)) {
                java.util.Set<String> catalogClips = PamAnimationCatalog.clipNames(pamPath);
                java.util.List<String> runtimeClips = pamPlayer.clips(pamPath);
                System.out.println("[EntityRenderer] Zombie " + effectiveAlias + " (" + pamPath
                    + ") catalog clips: " + catalogClips + " | runtime clips: " + runtimeClips);
            }

            // Try exact name match first (e.g. for sub-branch tracks like zombie_armor_cone_norm)
            ClipRef clip = pamPlayer.getClip(pamPath, state);
            if (clip == null) {
                String resolvedName = PamAnimationCatalog.resolveClip(pamPath, state);
                clip = resolvedName != null ? pamPlayer.getClip(pamPath, resolvedName) : null;
            }
            if (clip == null) {
                clip = pamPlayer.getClip(pamPath, "walk");
            }
            if (clip != null) {
                zombieClips.put(cacheKey, clip);
            }
            return clip;
        } catch (Exception e) {
            System.err.println("EntityRenderer: Failed to load Zombie PAM for alias " + effectiveAlias + " state " + state + ": " + e.getMessage());
            zombieClips.put(cacheKey, null);
            return null;
        }
    }

    public ClipRef getWalkClip(String alias) {
        return getZombieClip(alias, "walk");
    }

    public void renderZombie(SpriteBatch batch, Zombie zombie, float stateTime) {
        if (zombie == null) return;
        textures.update();

        if (zombie instanceof com.PVZ.model.entity.zombies.types.basic.ZombieCamel camel) {
            renderZombieCamel(batch, camel, stateTime);
            return;
        }

        String effectiveAlias = ZombieTexturePaths.getEffectivePamAlias(zombie);
        String state = ZombieAnimation.getState(zombie);
        if (state == null) {
            state = "walk";
        }

        ClipRef clip = getZombieClip(effectiveAlias, state);
        if (clip == null) {
            clip = getZombieClip("DEFAULT", "walk");
        }

        if (clip != null) {
            Color origColor = batch.getColor() != null ? new Color(batch.getColor()) : new Color(Color.WHITE);

            // Apply Status Effect Color Tinting & Overlays
            if (zombie.isFrozen()) {
                batch.setColor(0.5f, 0.7f, 1.0f, 1.0f); // Frozen solid (blue)
            } else if (zombie.hasStatusEffect(DamageType.ICE)) {
                batch.setColor(0.7f, 0.85f, 1.0f, 1.0f); // Chilled / Slowed (cyan)
            } else if (zombie.hasStatusEffect(DamageType.POISON)) {
                batch.setColor(0.7f, 0.3f, 0.9f, 1.0f); // Poisoned (purple)
            } else if (zombie.isHypnotized()) {
                batch.setColor(1.0f, 0.6f, 0.9f, 1.0f); // Hypnotized (pink)
            } else if (zombie.isGlowing()) {
                batch.setColor(0.8f, 1.0f, 0.5f, 1.0f); // Plant Food Drop Glow (bright green/gold)
            }

            boolean flipX = zombie.isHypnotized();
            float effectiveTime = zombie.isFrozen() ? 0.0f : stateTime;

            String activeArmorTrack = ZombieTexturePaths.getArmorSubBranchTrack(zombie);
            if (activeArmorTrack != null) {
                Map<String, Boolean> trackVisibility = new HashMap<>();
                trackVisibility.put("zombie_armor_cone_norm", false);
                trackVisibility.put("zombie_armor_cone_damage_01", false);
                trackVisibility.put("zombie_armor_cone_damage_02", false);
                trackVisibility.put("zombie_armor_bucket_norm", false);
                trackVisibility.put("zombie_armor_bucket_damage_01", false);
                trackVisibility.put("zombie_armor_bucket_damage_02", false);
                trackVisibility.put("zombie_armor_brick_norm", false);
                trackVisibility.put("zombie_armor_brick_damage_01", false);
                trackVisibility.put("zombie_armor_brick_damage_02", false);
                trackVisibility.put("_zombie_egypt_armor1_states", false);
                trackVisibility.put("_zombie_egypt_armor2_states", false);

                trackVisibility.put(activeArmorTrack, true);

                if (zombie.getAlias() != null && zombie.getAlias().contains("Mummy")) {
                    com.PVZ.model.entity.zombies.base.ZombieArmor.ArmorType type = zombie.getArmor() != null ? zombie.getArmor().getType() : null;
                    if (type == com.PVZ.model.entity.zombies.base.ZombieArmor.ArmorType.CONE) {
                        trackVisibility.put("_zombie_egypt_armor1_states", true);
                    } else if (type == com.PVZ.model.entity.zombies.base.ZombieArmor.ArmorType.BUCKET) {
                        trackVisibility.put("_zombie_egypt_armor2_states", true);
                    }
                }

                pamPlayer.draw(batch, clip, effectiveTime, (float) zombie.getX(), (float) zombie.getY(), true, trackVisibility);
            } else {
                pamPlayer.draw(batch, clip, effectiveTime, (float) zombie.getX(), (float) zombie.getY(), true);
            }
            batch.setColor(origColor);
        }
        renderFallingArmors(batch);
    }

    private void renderZombieCamel(SpriteBatch batch, com.PVZ.model.entity.zombies.types.basic.ZombieCamel camel, float stateTime) {
        float x = (float) camel.getX();
        float y = (float) camel.getY();
        float effectiveTime = camel.isFrozen() ? 0.0f : stateTime;
        String state = ZombieAnimation.getState(camel);
        if (state == null) state = "walk";

        boolean isDying = "die".equals(state) || camel.isDead();
        Color origColor = batch.getColor() != null ? new Color(batch.getColor()) : new Color(Color.WHITE);

        // 1. Render Rear Segment (Tail) (Offset 240f)
        if (camel.getRearSegment() != null && (!camel.getRearSegment().isDestroyed() || isDying)) {
            String rearState = isDying ? "die" : ("eat".equals(state) ? "idle" : state);
            ClipRef rearClip = getZombieClip("ZombieCamelRear", rearState);
            if (rearClip == null) rearClip = getZombieClip("ZombieCamelRear", "walk");
            if (rearClip != null) {
                Map<String, Boolean> vis = new HashMap<>();
                vis.put("_zombie_camel_board_tail_states", true);
                vis.put("_zombie_camel_board_tail_norm", true);
                vis.put("_zombie_camel_board_head_states", false);
                vis.put("_zombie_camel_board_hump_states", false);
                pamPlayer.draw(batch, rearClip, effectiveTime, x + 240f, y, true, vis);
            }
        }

        // 2. Render Middle Segment (Hump) (Offset 120f)
        if (camel.getMiddleSegment() != null && (!camel.getMiddleSegment().isDestroyed() || isDying)) {
            String middleState = isDying ? "die" : ("eat".equals(state) ? "idle" : state);
            ClipRef middleClip = getZombieClip("ZombieCamelMiddle", middleState);
            if (middleClip == null) middleClip = getZombieClip("ZombieCamelMiddle", "walk");
            if (middleClip != null) {
                Map<String, Boolean> vis = new HashMap<>();
                vis.put("_zombie_camel_board_hump_states", true);
                vis.put("_zombie_camel_board_hump_norm", true);
                vis.put("_zombie_camel_board_head_states", false);
                vis.put("_zombie_camel_board_tail_states", false);
                pamPlayer.draw(batch, middleClip, effectiveTime, x + 120f, y, true, vis);
            }
        }

        // 3. Render Front Segment (Head) (Offset 0f)
        if (camel.getFrontSegment() != null && (!camel.getFrontSegment().isDestroyed() || isDying)) {
            String frontState = isDying ? "die" : state;
            ClipRef frontClip = getZombieClip("ZombieCamelDefault", frontState);
            if (frontClip == null) frontClip = getZombieClip("ZombieCamelDefault", "walk");
            if (frontClip != null) {
                Map<String, Boolean> vis = new HashMap<>();
                vis.put("_zombie_camel_board_head_states", true);
                vis.put("_zombie_camel_board_head_norm", true);
                vis.put("_zombie_camel_board_hump_states", false);
                vis.put("_zombie_camel_board_tail_states", false);
                pamPlayer.draw(batch, frontClip, effectiveTime, x, y, true, vis);
            }
        }

        batch.setColor(origColor);
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
        return renderPam(batch, pamPath, null, stateTime, x, y);
    }

    /**
     * مثل renderPam(batch, path, stateTime, x, y) ولی اجازه می‌دهد کلیپ مشخصی (مثلاً "attack"
     * برای چمن‌زنِ فعال‌شده در برابر "idle" برای چمن‌زن پارک‌شده) به‌جای اولین کلیپ موجود
     * انتخاب شود. اگر clipName پیدا نشد یا null بود، مثل قبل به اولین کلیپ موجود برمی‌گردد.
     */
    public boolean renderPam(SpriteBatch batch, String pamPath, String clipName, float stateTime, float x, float y) {
        if (pamPath == null) {
            return false;
        }
        textures.update();
        String cacheKey = clipName == null ? pamPath : pamPath + "#" + clipName;
        ClipRef clip = genericPamClips.get(cacheKey);
        if (clip == null) {
            if (Boolean.TRUE.equals(genericPamFailed.get(cacheKey))) {
                return false;
            }
            try {
                pamPlayer.loadSync(pamPath);
                java.util.List<String> available = pamPlayer.clips(pamPath);
                String chosen = null;
                if (clipName != null && available != null && available.contains(clipName)) {
                    chosen = clipName;
                } else if (available != null && !available.isEmpty()) {
                    chosen = available.get(0);
                }
                if (chosen != null) {
                    clip = pamPlayer.getClip(pamPath, chosen);
                }
                if (clip == null) {
                    genericPamFailed.put(cacheKey, true);
                    return false;
                }
                genericPamClips.put(cacheKey, clip);
            } catch (Exception e) {
                System.err.println("EntityRenderer: Failed to load PAM " + pamPath + ": " + e.getMessage());
                genericPamFailed.put(cacheKey, true);
                return false;
            }
        }
        pamPlayer.draw(batch, clip, stateTime, x, y, true);
        return true;
    }


    public boolean renderSun(SpriteBatch batch, com.PVZ.model.entity.Sun.SunType type,
                             float animationTime, boolean falling, boolean reachedGround,
                             float x, float y) {
        String pamPath = "768/INITIAL/EFFECTS/SUN/SUN.PAM";
        String clipName = "animation";
        float localTime = animationTime;
        if (type == com.PVZ.model.entity.Sun.SunType.SPECIAL && falling && !reachedGround) {
            if (animationTime < 0.5333f) {
                clipName = "transition_blue";
            } else {
                clipName = "blue";
                localTime = animationTime - 0.5333f;
            }
        } else if (type == com.PVZ.model.entity.Sun.SunType.RADIOACTIVE && falling && !reachedGround) {
            if (animationTime < 0.5333f) {
                clipName = "transition_red";
            } else {
                clipName = "red";
                localTime = animationTime - 0.5333f;
            }
        }
        return renderPam(batch, pamPath, clipName, localTime, x, y);
    }

    public boolean renderLoot(SpriteBatch batch, com.PVZ.model.entity.LootDrop.LootType type,
                              float animationTime, float x, float y) {
        if (type == null) return false;
        String pamPath;
        String clip;
        switch (type) {
            case COIN:
                pamPath = "768/INITIAL/EFFECTS/COIN_GOLD/COIN_GOLD.PAM";
                clip = "animation";
                break;
            case DIAMOND:
                pamPath = "768/INITIAL/EFFECTS/TUTORIAL_DIAMOND/TUTORIAL_DIAMOND.PAM";
                clip = "idle";
                break;
            case POT:
            default:
                return false;
        }
        return renderPam(batch, pamPath, clip, animationTime, x, y);
    }

    private static class FallingArmorPiece {
        float x, y;
        float vx, vy;
        float lifetime;
        float alpha;
        String pamAlias;
        String armorTrack;

        FallingArmorPiece(float x, float y, String pamAlias, String armorTrack) {
            this.x = x;
            this.y = y;
            this.vx = (float) (Math.random() * 40 - 20);
            this.vy = (float) (Math.random() * 60 + 80);
            this.lifetime = 0.8f;
            this.alpha = 1.0f;
            this.pamAlias = pamAlias;
            this.armorTrack = armorTrack;
        }

        boolean update(float delta) {
            x += vx * delta;
            y += vy * delta;
            vy -= 350f * delta;
            lifetime -= delta;
            if (lifetime < 0.3f) {
                alpha = Math.max(0f, lifetime / 0.3f);
            }
            return lifetime <= 0;
        }
    }

    public TextureBank getTextures() {
        return textures;
    }

    public PamPlayer getPamPlayer() {
        return pamPlayer;
    }

    private final java.util.List<FallingArmorPiece> fallingArmors = new java.util.concurrent.CopyOnWriteArrayList<>();

    public void spawnFallingArmor(float x, float y, com.PVZ.model.entity.zombies.base.ZombieArmor.ArmorType type, String alias) {
        String pamAlias = alias != null ? alias : "ZombieTutorialArmor1Default";
        String trackName = null;
        if (type == com.PVZ.model.entity.zombies.base.ZombieArmor.ArmorType.CONE) trackName = "zombie_armor_cone_damage_02";
        else if (type == com.PVZ.model.entity.zombies.base.ZombieArmor.ArmorType.BUCKET) trackName = "zombie_armor_bucket_damage_02";
        else if (type == com.PVZ.model.entity.zombies.base.ZombieArmor.ArmorType.BRICK) trackName = "zombie_armor_brick_damage_02";
        else if (type == com.PVZ.model.entity.zombies.base.ZombieArmor.ArmorType.CROWN) trackName = "zombie_armor_crown_damage_02";

        fallingArmors.add(new FallingArmorPiece(x, y, pamAlias, trackName));
    }

    public void renderFallingArmors(SpriteBatch batch) {
        if (fallingArmors.isEmpty()) return;
        float delta = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        Color origColor = batch.getColor() != null ? new Color(batch.getColor()) : new Color(Color.WHITE);

        for (FallingArmorPiece piece : fallingArmors) {
            if (piece.update(delta)) {
                fallingArmors.remove(piece);
                continue;
            }
            ClipRef clip = getZombieClip(piece.pamAlias, "walk");
            if (clip != null) {
                batch.setColor(1.0f, 1.0f, 1.0f, piece.alpha);
                if (piece.armorTrack != null) {
                    Map<String, Boolean> trackVisibility = new HashMap<>();
                    trackVisibility.put("zombie_armor_cone_norm", false);
                    trackVisibility.put("zombie_armor_cone_damage_01", false);
                    trackVisibility.put("zombie_armor_cone_damage_02", false);
                    trackVisibility.put("zombie_armor_bucket_norm", false);
                    trackVisibility.put("zombie_armor_bucket_damage_01", false);
                    trackVisibility.put("zombie_armor_bucket_damage_02", false);
                    trackVisibility.put("zombie_armor_brick_norm", false);
                    trackVisibility.put("zombie_armor_brick_damage_01", false);
                    trackVisibility.put("zombie_armor_brick_damage_02", false);
                    trackVisibility.put(piece.armorTrack, true);
                    pamPlayer.draw(batch, clip, 0.5f, piece.x, piece.y, false, trackVisibility);
                } else {
                    pamPlayer.draw(batch, clip, 0.5f, piece.x, piece.y, false);
                }
            }
        }
        batch.setColor(origColor);
    }
}
