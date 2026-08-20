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

            String resolvedName = PamAnimationCatalog.resolveClip(pamPath, state);
            if (resolvedName == null) {
                resolvedName = state;
            }
            ClipRef clip = pamPlayer.getClip(pamPath, resolvedName);
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
        if (zombie instanceof com.PVZ.model.entity.zombies.types.basic.ZombiePharaoh pharaoh && pharaoh.isSarcophagusBroken()) {
            if ("walk".equals(state)) state = "walk_norm";
            else if ("eat".equals(state)) state = "eat_norm";
            else if ("idle".equals(state)) state = "idle_norm";
        }
        if (zombie instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieBeachSnorkel snorkel) {
            if (zombie.isDying()) {
                state = "die";
            } else if (snorkel.isSubmerged()) {
                state = "particles";
            } else if (!snorkel.isMoving()) {
                state = "eat";
            } else {
                state = "walk";
            }
        }
        if (zombie instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieBeachFastSwimmer swimmer) {
            if (zombie.isDying()) {
                state = "die";
            } else if (swimmer.isInWater()) {
                state = "particles";
            } else if (!swimmer.isMoving()) {
                state = "eat";
            } else {
                state = "walk";
            }
        }
        if (zombie instanceof com.PVZ.model.entity.zombies.types.ranged_caster.ZombieDarkJuggler juggler) {
            if (zombie.isDying()) {
                state = "die";
            } else if (juggler.isSpinning()) {
                state = "spin";
            } else if (!juggler.isMoving()) {
                state = "eat";
            } else {
                state = "walk";
            }
        }

        if (zombie instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieProspector prospector) {
            if (zombie.isDying()) {
                state = "die";
            } else if (!prospector.isMoving()) {
                state = "eat";
            } else {
                state = "walk";
            }
        }

        if (zombie instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieArcade arcade) {
            if (zombie.isDying()) {
                state = "die";
            } else if (!arcade.isMoving()) {
                state = "idle";
            } else {
                state = "push";
            }
        }

        if (zombie instanceof com.PVZ.model.entity.zombies.types.zomboss.AbstractZomboss boss) {
            if (zombie.isDying()) {
                state = "die";
            } else if (boss.isStunned()) {
                state = "stun_loop";
            } else if (!ZombieAnimation.isActive(zombie)) {
                state = "idle";
            }
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
            } else if (zombie instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieBeachSnorkel snorkel && snorkel.isSubmerged()) {
                batch.setColor(0.75f, 0.90f, 1.0f, 0.90f); // Submerged underwater watery tint
            } else if (zombie.getArmor() != null && !zombie.getArmor().isDestroyed() && zombie.getArmor().getType() == com.PVZ.model.entity.zombies.base.ZombieArmor.ArmorType.CROWN) {
                batch.setColor(1.0f, 0.92f, 0.60f, 1.0f); // Royal Knight Golden Aura
            } else if (zombie.getX() < 950.0 && !zombie.isHypnotized()) {
                boolean isFlew = zombie instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieProspector zp && zp.isFlewToLeft();
                if (!isFlew) {
                    float dangerFactor = Math.min(1.0f, Math.max(0.0f, (950.0f - (float) zombie.getX()) / 450.0f));
                    // Flashing emergency warning light (sine pulse at ~10 rad/s):
                    float pulse = 0.5f + 0.5f * (float) Math.sin(stateTime * 10.0f);
                    float redIntensity = dangerFactor * (0.30f + 0.65f * pulse);
                    batch.setColor(1.0f, 1.0f - 0.75f * redIntensity, 1.0f - 0.75f * redIntensity, 1.0f); // Flashing danger red strobe
                }
            }

            float effectiveTime = (zombie.isFrozen() || zombie.isButtered()) ? 0.0f : stateTime;

            Map<String, Boolean> trackVisibility = null;
            String activeArmorTrack = ZombieTexturePaths.getArmorSubBranchTrack(zombie);
            if (activeArmorTrack != null) {
                trackVisibility = new HashMap<>();
                trackVisibility.put("zombie_armor_cone_norm", false);
                trackVisibility.put("zombie_armor_cone_damage_01", false);
                trackVisibility.put("zombie_armor_cone_damage_02", false);
                trackVisibility.put("zombie_armor_bucket_norm", false);
                trackVisibility.put("zombie_armor_bucket_damage_01", false);
                trackVisibility.put("zombie_armor_bucket_damage_02", false);
                trackVisibility.put("zombie_armor_brick_norm", false);
                trackVisibility.put("zombie_armor_brick_damage_01", false);
                trackVisibility.put("zombie_armor_brick_damage_02", false);
                trackVisibility.put("zombie_armor_crown_norm", false);
                trackVisibility.put("zombie_armor_crown_damage_01", false);
                trackVisibility.put("zombie_armor_crown_damage_02", false);
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
            }

            if (trackVisibility == null && zombie.isButtered()) {
                trackVisibility = new HashMap<>();
            }
            if (trackVisibility != null) {
                trackVisibility.put("butter", zombie.isButtered());
                trackVisibility.put("_butter", zombie.isButtered());
                trackVisibility.put("head_butter", zombie.isButtered());
                trackVisibility.put("_bull_head_butter", zombie.isButtered());
            }

            if (isZombotanyAlias(zombie.getAlias())) {
                if (trackVisibility == null) trackVisibility = new HashMap<>();
                // Zombotany is a normal zombie body with the plant replacing its head.
                // Hide the normal head/hair/tie tracks before drawing the plant head overlay.
                trackVisibility.put("anim_head1", false);
                trackVisibility.put("anim_head2", false);
                trackVisibility.put("anim_head", false);
                trackVisibility.put("anim_hair", false);
                trackVisibility.put("Zombie_tie", false);
            }

            if (zombie instanceof com.PVZ.model.entity.zombies.types.heavy_gargantuar.ZombieGargantuar garg && garg.isImpThrown()) {
                if (trackVisibility == null) trackVisibility = new HashMap<>();
                trackVisibility.put("imp", false);
                trackVisibility.put("zombie_imp", false);
                trackVisibility.put("_zombie_imp_states", false);
                trackVisibility.put("_zombie_egypt_imp_states", false);
                trackVisibility.put("imp_body", false);
                trackVisibility.put("imp_head", false);
                trackVisibility.put("imp_arm", false);
            }

            if (zombie instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombiePiano piano) {
                String pianoClipName = zombie.isDying() ? "die" : "play";
                renderPam(batch, "768/FULL/ZOMBIE/PIANO/PIANO.PAM", pianoClipName, effectiveTime, (float) zombie.getX() - 35f, (float) zombie.getY());
            }

            if (zombie instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieArcade arcade) {
                String cabinetClip = zombie.isDying() ? "death" : (arcade.isCabinetActive() ? "active" : "idle");
                renderPam(batch, "768/FULL/EFFECTS/80S_ARCADE_CABINET/80S_ARCADE_CABINET.PAM", cabinetClip, effectiveTime, (float) zombie.getX() - 110f, (float) zombie.getY());
            }

            if (zombie instanceof com.PVZ.model.entity.zombies.types.basic.ZombieIceage iceZ && iceZ.isEncasedInIce()) {
                float zx = (float) zombie.getX() + 35f;
                float zy = (float) zombie.getY() + 45f;
                renderPam(batch, "768/FULL/WORLDMAP/DANGER_NODE_ICEAGE/DANGER_NODE_ICEAGE.PAM", "locked_idle", effectiveTime, zx, zy, 0.36f);
            }

            if (zombie instanceof com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechEgypt egyptBoss && egyptBoss.isMissileActive()) {
                // 1. Render target reticle on ground
                renderPam(batch, "768/INITIAL/EFFECTS/MISSILE_TOE_RETICLE/MISSILE_TOE_RETICLE.PAM", "animation", egyptBoss.getMissileAnimTime(), egyptBoss.getMissileTargetX(), egyptBoss.getMissileTargetY(), 1.0f);
                // 2. Render vertical falling missile
                if (egyptBoss.getMissileCurrentY() > egyptBoss.getMissileTargetY()) {
                    renderPam(batch, "768/INITIAL/EFFECTS/T_MISSILE_TOE_PROJECTILE/T_MISSILE_TOE_PROJECTILE.PAM", "animation", egyptBoss.getMissileAnimTime(), egyptBoss.getMissileTargetX(), egyptBoss.getMissileCurrentY(), 1.2f);
                }
            }

            if (zombie instanceof com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechDark darkBoss) {
                // 1. Render flying fireballs
                for (com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechDark.DarkFireball fb : darkBoss.getActiveFireballs()) {
                    renderPam(batch, "768/FULL/EFFECTS/ZOMBOSS_DARK_FIREBALL/ZOMBOSS_DARK_FIREBALL.PAM", "fireball", darkBoss.getFireballAnimTimer(), fb.currentX, fb.currentY, 1.2f);
                }
                // 2. Render fire breath stream along the 2 rows
                if (darkBoss.isFireBreathActive()) {
                    float fx = (float) zombie.getX() - 100f;
                    float fy = (float) zombie.getY() + 40f;
                    renderPam(batch, "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM", "idle2", darkBoss.getFireBreathTimer(), fx, fy, 1.4f);
                    renderPam(batch, "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM", "idle2", darkBoss.getFireBreathTimer(), fx - 220f, fy, 1.4f);
                    renderPam(batch, "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM", "idle2", darkBoss.getFireBreathTimer(), fx - 440f, fy, 1.4f);
                }
            }

            if (zombie instanceof com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechBeach beachBoss) {
                // 1. Render active small sharks
                for (com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechBeach.SmallShark shark : beachBoss.getActiveSharks()) {
                    renderPam(batch, "768/FULL/EFFECTS/ZOMBOSS_SHARK_PROJECTILE/ZOMBOSS_SHARK_PROJECTILE.PAM", "animation", shark.animTime, shark.x, shark.y, 1.1f);
                }
                // 2. Render water foam and splashes during turbine suction
                if (beachBoss.isTurbineActive()) {
                    float vx = (float) zombie.getX() - 120f;
                    float vy = (float) zombie.getY() + 30f;
                    renderPam(batch, "768/FULL/EFFECTS/WATER_FOAM/WATER_FOAM.PAM", "animation", beachBoss.getTurbineTimer(), vx, vy, 1.3f);
                }
            }

            if (zombie instanceof com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechIceAge iceBoss) {
                // 1. Render active ice missiles
                for (com.PVZ.model.entity.zombies.types.zomboss.ZombieZombossMechIceAge.IceMissile im : iceBoss.getActiveMissiles()) {
                    renderPam(batch, "768/FULL/EFFECTS/ZOMBOSS_GLACIER_BLOCK/ZOMBOSS_GLACIER_BLOCK.PAM", "animation", iceBoss.getMissileAnimTimer(), im.currentX, im.currentY, 0.9f);
                }
                // 2. Render ice wind blizzard breath along the 2 rows
                if (iceBoss.isIceWindActive()) {
                    float wx = (float) zombie.getX() - 100f;
                    float wy = (float) zombie.getY() + 40f;
                    renderPam(batch, "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM", "animation", iceBoss.getIceWindTimer(), wx, wy, 1.4f);
                    renderPam(batch, "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM", "animation", iceBoss.getIceWindTimer(), wx - 220f, wy, 1.4f);
                    renderPam(batch, "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM", "animation", iceBoss.getIceWindTimer(), wx - 440f, wy, 1.4f);
                }
            }

            boolean shouldFlip = zombie.isHypnotized() || (zombie instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieProspector zp && zp.isFlewToLeft());
            if (shouldFlip) {
                com.badlogic.gdx.math.Matrix4 oldTransform = batch.getTransformMatrix().cpy();
                com.badlogic.gdx.math.Matrix4 flipped = oldTransform.cpy();
                float cx = (float) zombie.getX() + 60f;
                flipped.translate(cx, 0, 0);
                flipped.scale(-1f, 1f, 1f);
                flipped.translate(-cx, 0, 0);
                batch.setTransformMatrix(flipped);

                if (trackVisibility != null) {
                    pamPlayer.draw(batch, clip, effectiveTime, (float) zombie.getX(), (float) zombie.getY(), true, trackVisibility);
                } else {
                    pamPlayer.draw(batch, clip, effectiveTime, (float) zombie.getX(), (float) zombie.getY(), true);
                }

                if (zombie.isHitFlashing()) {
                    batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
                    batch.setColor(1.0f, 1.0f, 1.0f, 0.32f);
                    if (trackVisibility != null) {
                        pamPlayer.draw(batch, clip, effectiveTime, (float) zombie.getX(), (float) zombie.getY(), true, trackVisibility);
                    } else {
                        pamPlayer.draw(batch, clip, effectiveTime, (float) zombie.getX(), (float) zombie.getY(), true);
                    }
                    batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
                }

                batch.setTransformMatrix(oldTransform);
            } else {
                if (trackVisibility != null) {
                    pamPlayer.draw(batch, clip, effectiveTime, (float) zombie.getX(), (float) zombie.getY(), true, trackVisibility);
                } else {
                    pamPlayer.draw(batch, clip, effectiveTime, (float) zombie.getX(), (float) zombie.getY(), true);
                }

                if (zombie.isHitFlashing()) {
                    batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
                    batch.setColor(1.0f, 1.0f, 1.0f, 0.32f);
                    if (trackVisibility != null) {
                        pamPlayer.draw(batch, clip, effectiveTime, (float) zombie.getX(), (float) zombie.getY(), true, trackVisibility);
                    } else {
                        pamPlayer.draw(batch, clip, effectiveTime, (float) zombie.getX(), (float) zombie.getY(), true);
                    }
                    batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
                }
            }
            if (zombie.isButtered()) {
                renderPam(batch, "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_BUTTER/SPLAT_KERNALPULT_BUTTER.PAM", "animation", effectiveTime, (float) zombie.getX() + 15f, (float) zombie.getY() + 75f);
            }

            // Zombotany visuals are composed from the normal zombie body PAM plus the
            // corresponding plant idle PAM as a head/top overlay. The provided PAM catalog
            // does not contain dedicated Zombotany PAMs, so this is the asset-faithful fallback.
            renderZombotanyHeadOverlay(batch, zombie, effectiveTime);
            batch.setColor(origColor);
        }
        renderFallingArmors(batch);
    }

    private void renderZombotanyHeadOverlay(SpriteBatch batch, Zombie zombie, float stateTime) {
        if (zombie == null || !isZombotanyAlias(zombie.getAlias()) || zombie.isDying()) return;

        String plantType = zombotanyPlantType(zombie.getAlias());
        ClipRef headClip = getPlantClip(plantType, "idle");
        if (headClip == null) return;

        float x = (float) zombie.getX();
        float y = (float) zombie.getY();
        float scale;
        float xOffset;
        float yOffset;
        switch (plantType) {
            case "PEASHOOTER" -> { scale = 0.34f; xOffset = -4f; yOffset = 78f; }
            case "WALL_NUT" -> { scale = 0.32f; xOffset = -2f; yOffset = 76f; }
            case "JALAPENO" -> { scale = 0.31f; xOffset = -2f; yOffset = 78f; }
            case "SQUASH" -> { scale = 0.33f; xOffset = -3f; yOffset = 78f; }
            default -> { scale = 0.32f; xOffset = -2f; yOffset = 76f; }
        }
        drawClipScaled(batch, headClip, stateTime, x + xOffset, y + yOffset, -scale, scale);
    }

    private boolean isZombotanyAlias(String alias) {
        return "ZombotanyPeashooterDefault".equals(alias)
            || "ZombotanyWallnutDefault".equals(alias)
            || "ZombotanyJalapenoDefault".equals(alias)
            || "ZombotanySquashDefault".equals(alias);
    }

    public void renderZombieAlias(SpriteBatch batch, String alias, String state, float stateTime, float x, float y) {
        if (alias == null) return;
        textures.update();

        if (isZombotanyAlias(alias)) {
            renderZombotanyAlias(batch, alias, state, stateTime, x, y);
            return;
        }

        String resolvedState = state != null ? state : "idle";
        ClipRef clip = getZombieClip(alias, resolvedState);
        if (clip == null) {
            clip = getZombieClip(alias, "walk");
        }
        if (clip == null) {
            clip = getZombieClip("DEFAULT", "walk");
        }
        if (clip != null) {
            pamPlayer.draw(batch, clip, stateTime, x, y, true);
        }
    }

    public void renderZombieAlias(SpriteBatch batch, String alias, String state, float stateTime,
                                  float x, float y, float scale) {
        if (scale == 1f) {
            renderZombieAlias(batch, alias, state, stateTime, x, y);
            return;
        }
        com.badlogic.gdx.math.Matrix4 old = batch.getTransformMatrix().cpy();
        com.badlogic.gdx.math.Matrix4 transform = batch.getTransformMatrix();
        transform.translate(x, y, 0f);
        transform.scale(scale, scale, 1f);
        batch.setTransformMatrix(transform);
        try {
            renderZombieAlias(batch, alias, state, stateTime, 0f, 0f);
        } finally {
            batch.setTransformMatrix(old);
        }
    }

    private void renderZombotanyAlias(SpriteBatch batch, String alias, String state, float stateTime, float x, float y) {
        String resolvedState = state != null ? state : "idle";
        ClipRef bodyClip = getZombieClip(alias, resolvedState);
        if (bodyClip == null) bodyClip = getZombieClip(alias, "walk");
        if (bodyClip == null) bodyClip = getZombieClip("ZombieTutorialDefault", "walk");
        if (bodyClip == null) return;

        Map<String, Boolean> visibility = new HashMap<>();
        visibility.put("anim_head1", false);
        visibility.put("anim_head2", false);
        visibility.put("anim_head", false);
        visibility.put("anim_hair", false);
        visibility.put("Zombie_tie", false);
        pamPlayer.draw(batch, bodyClip, stateTime, x, y, true, visibility);

        String plantType = zombotanyPlantType(alias);
        ClipRef headClip = getPlantClip(plantType, "idle");
        if (headClip == null) return;

        float scale;
        float xOffset;
        float yOffset;
        switch (plantType) {
            case "PEASHOOTER" -> { scale = 0.34f; xOffset = -4f; yOffset = 78f; }
            case "WALL_NUT" -> { scale = 0.32f; xOffset = -2f; yOffset = 76f; }
            case "JALAPENO" -> { scale = 0.31f; xOffset = -2f; yOffset = 78f; }
            case "SQUASH" -> { scale = 0.33f; xOffset = -3f; yOffset = 78f; }
            default -> { scale = 0.32f; xOffset = -2f; yOffset = 76f; }
        }
        drawClipScaled(batch, headClip, stateTime, x + xOffset, y + yOffset, -scale, scale);
    }

    private String zombotanyPlantType(String alias) {
        return switch (alias) {
            case "ZombotanyPeashooterDefault" -> "PEASHOOTER";
            case "ZombotanyWallnutDefault" -> "WALL_NUT";
            case "ZombotanyJalapenoDefault" -> "JALAPENO";
            case "ZombotanySquashDefault" -> "SQUASH";
            default -> "PEASHOOTER";
        };
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
        drawClipScaled(batch, clip, stateTime, x, y, plantScale(plantTypeName));
        return true;
    }

    public boolean renderPlant(SpriteBatch batch, String plantTypeName, String state, float stateTime,
                               float x, float y) {
        textures.update();
        ClipRef clip = getPlantClip(plantTypeName, state);
        if (clip == null) {
            return false;
        }
        drawClipScaled(batch, clip, stateTime, x, y, plantScale(plantTypeName));
        return true;
    }

    private float plantScale(String plantTypeName) {
        if ("THREEPEATER".equalsIgnoreCase(plantTypeName)) {
            return 1.35f;
        }
        return 1.0f;
    }

    private void drawClipScaled(SpriteBatch batch, ClipRef clip, float stateTime,
                                float x, float y, float scale) {
        drawClipScaled(batch, clip, stateTime, x, y, scale, 1f);
    }

    private void drawClipScaled(SpriteBatch batch, ClipRef clip, float stateTime,
                                float x, float y, float scaleX, float scaleY) {
        if (clip == null) return;
        pamPlayer.draw(batch, clip, stateTime, x, y, true);
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

        String clipName = projectileClipName(visualKey);
        String cacheKey = visualKey + "#" + (clipName == null ? "<first>" : clipName);
        ClipRef clip = projectileClips.get(cacheKey);
        if (clip == null) {
            if (Boolean.TRUE.equals(projectilePamFailed.get(cacheKey))) {
                return false;
            }
            String pamPath = com.PVZ.model.entity.plants.behavior.impl.ProjectileVisuals.getPath(visualKey);
            if (pamPath == null) {
                projectilePamFailed.put(cacheKey, true);
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
                    projectilePamFailed.put(cacheKey, true);
                    return false;
                }
                projectileClips.put(cacheKey, clip);
            } catch (Exception e) {
                System.err.println("EntityRenderer: Failed to load projectile PAM " + visualKey + ": " + e.getMessage());
                projectilePamFailed.put(cacheKey, true);
                return false;
            }
        }

        drawClipScaled(batch, clip, stateTime, x, y, projectileScale(visualKey));
        return true;
    }

    private String projectileClipName(String visualKey) {
        return switch (visualKey) {
            case "CITRON" -> "Citron_Citrus_Orb";
            case "CITRON_PF" -> "Plantfood_Citron_Plasma_Orb";
            case "CAULIPOWER" -> "animation";
            case "ELECTRIC_BLUEBERRY" -> "attack";
            case "CACTUS" -> "idle";
            case "FUME" -> "special";
            case "STARFRUIT", "STARFRUIT_PF" -> "animation";
            case "BOWLING_BULB_1", "BOWLING_BULB_2", "BOWLING_BULB_3" -> "animation";
            case "MEGA_GATLING" -> "animation";
            case "ROTOBAGA_1", "ROTOBAGA_2" -> "animation";
            case "GRAPESHOT_FORWARD" -> "animation_forward";
            case "GRAPESHOT_BACKWARD" -> "animation_backward";
            case "GRAPESHOT_UP" -> "animation_verticle_up";
            case "GRAPESHOT_DOWN" -> "animation_verticle_down";
            case "GRAPESHOT" -> "animation_forward";
            default -> null;
        };
    }

    private float projectileScale(String visualKey) {
        return switch (visualKey) {
            case "CITRON", "CITRON_PF" -> 1.25f;
            case "CAULIPOWER" -> 1.20f;
            case "ELECTRIC_BLUEBERRY" -> 1.25f;
            case "CACTUS" -> 0.85f;
            case "FUME" -> 1.10f;
            case "STARFRUIT" -> 0.95f;
            case "STARFRUIT_PF" -> 1.05f;
            case "BOWLING_BULB_1", "BOWLING_BULB_2", "BOWLING_BULB_3" -> 1.0f;
            case "MEGA_GATLING" -> 0.95f;
            case "ROTOBAGA_1", "ROTOBAGA_2" -> 1.45f;
            case "GRAPESHOT_FORWARD", "GRAPESHOT_BACKWARD", "GRAPESHOT_UP", "GRAPESHOT_DOWN", "GRAPESHOT" -> 1.25f;
            default -> 1.0f;
        };
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

    public boolean renderPam(SpriteBatch batch, String pamPath, String clipName, float stateTime, float x, float y, float scale) {
        if (scale == 1.0f || scale <= 0f) {
            return renderPam(batch, pamPath, clipName, stateTime, x, y);
        }
        com.badlogic.gdx.math.Matrix4 oldMatrix = batch.getTransformMatrix().cpy();
        com.badlogic.gdx.math.Matrix4 transform = batch.getTransformMatrix();
        transform.translate(x, y, 0);
        transform.scale(scale, scale, 1.0f);
        transform.translate(-x, -y, 0);
        batch.setTransformMatrix(transform);

        boolean res = renderPam(batch, pamPath, clipName, stateTime, x, y);

        batch.setTransformMatrix(oldMatrix);
        return res;
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
            case PLANT_FOOD:
                pamPath = "768/INITIAL/EFFECTS/PLANTFOOD_PICKUP/PLANTFOOD_PICKUP.PAM";
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
        else if (type == com.PVZ.model.entity.zombies.base.ZombieArmor.ArmorType.BRICK || type == com.PVZ.model.entity.zombies.base.ZombieArmor.ArmorType.ICE_BLOCK) trackName = "zombie_armor_brick_damage_02";
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
