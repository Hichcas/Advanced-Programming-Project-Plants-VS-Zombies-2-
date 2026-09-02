package com.PVZ.view.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.Color;
import pvz.libpvz.pam.ClipRef;
import com.PVZ.model.entity.LawnMower;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.game.GameEngine;
import com.PVZ.model.game.ZombieEngine;
import com.PVZ.model.status.AppStatus;

import java.util.HashMap;
import java.util.Map;

final class ZombieRendering {

    private ZombieRendering() {}

    static ClipRef getClip(EntityRenderer er, String alias, String state) {
        if (alias == null || state == null) return null;
        String key = alias + "#" + state;
        Map<String, ClipRef> cache = er.zombieClips();
        if (cache.containsKey(key)) return cache.get(key);

        String pamPath = com.PVZ.model.entity.zombies.base.ZombieTexturePaths
            .getPamPath(alias);
        try {
            er.getPamPlayer().loadSync(pamPath);
            String resolved = com.PVZ.model.entity.PamAnimationCatalog
                .resolveClip(pamPath, state);
            if (resolved == null) resolved = state;
            ClipRef clip = er.getPamPlayer().getClip(pamPath, resolved);
            if (clip == null) clip = er.getPamPlayer().getClip(pamPath, "walk");
            cache.put(key, clip);
            return clip;
        } catch (Exception e) {
            System.err.println("EntityRenderer: Failed to load Zombie PAM for " + alias);
            cache.put(key, null);
            return null;
        }
    }

    static void render(EntityRenderer er, SpriteBatch batch, Zombie zombie, float stateTime) {
        if (zombie == null) return;
        er.update();
        if (zombie instanceof com.PVZ.model.entity.zombies.types.basic.ZombieCamel camel) {
            renderCamel(er, batch, camel, stateTime);
            return;
        }
        String alias = com.PVZ.model.entity.zombies.base.ZombieTexturePaths
            .getEffectivePamAlias(zombie);
        String state = resolveState(zombie);
        ClipRef clip = getClip(er, alias, state);
        if (clip == null) clip = getClip(er, "DEFAULT", "walk");
        if (clip == null) return;

        Color orig = new Color(batch.getColor());
        applyTint(batch, zombie, stateTime);
        float effectiveTime = (zombie.isFrozen() || zombie.isButtered()) ? 0f : stateTime;
        Map<String, Boolean> visibility = buildVisibility(zombie);
        renderBossExtras(er, batch, zombie, effectiveTime);
        drawClip(er, batch, clip, effectiveTime, zombie.getX(), zombie.getY(), visibility);
        renderButter(er, batch, zombie, effectiveTime);
        renderZombotanyHead(er, batch, zombie, effectiveTime);
        batch.setColor(orig);
        if (zombie.isHitFlashing()) {
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                com.badlogic.gdx.graphics.GL20.GL_ONE);
            batch.setColor(1.0f, 1.0f, 1.0f, 0.32f);
            drawClip(er, batch, clip, effectiveTime, zombie.getX(), zombie.getY(), visibility);
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(orig);
        }
        if (isEndangeringHouse(zombie)) {
            float pulse = (float) (Math.sin(System.currentTimeMillis() * 0.008) * 0.18 + 0.30);
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                com.badlogic.gdx.graphics.GL20.GL_ONE);
            batch.setColor(1.0f, 0.12f, 0.12f, pulse);
            drawClip(er, batch, clip, effectiveTime, zombie.getX(), zombie.getY(), visibility);
            batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(orig);
        }
        er.renderFallingArmors(batch);
    }

    /**
     * True once a hostile zombie on a live lawn has crawled into the last two
     * columns, i.e. it is about to reach that row's parked lawnmower.
     * Almanac / collection previews are not on the battlefield, so they must
     * never flash. Modes without a lawnmower (e.g. I, Zombie) also stay quiet.
     */
    private static boolean isEndangeringHouse(Zombie zombie) {
        if (zombie.isDying() || zombie.isHypnotized()) return false;
        GameEngine engine = AppStatus.getGameEngine();
        if (engine == null) return false;
        int row = (int) Math.round(zombie.getRow());
        if (!(engine instanceof ZombieEngine ze)) return false;
        java.util.List<Zombie> lane = ze.getZombiesInLane(row);
        if (lane == null || !lane.contains(zombie)) return false;
        LawnMower mower = engine.getLawnMower(row);
        if (mower == null || mower.isUsed()) return false;
        float warningReach = engine.getMap() != null ? engine.getMap().getTileWidth() * 2f : 280f;
        return zombie.getX() <= mower.getFrontX() + warningReach;
    }

    private static String resolveState(Zombie z) {
        String state = ZombieAnimation.getState(z);
        if (state == null) state = "walk";
        // NOTE: previously this hardcoded "die" whenever the zombie was
        // dying, discarding whatever specific death-clip variant
        // ZombieAnimation.getState() had already resolved (e.g. "die2" -
        // see Zombie.pickDeathAnimState()). That's exactly what made every
        // zombie of a given type play the same death animation. `state`
        // already correctly holds "die" for the common case and the
        // randomly-picked variant for zombies that have more than one, so
        // just use it.
        if (z.isDying()) return state;
        if (z instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieBeachSnorkel snorkel) {
            return snorkel.isSubmerged() ? "particles" : snorkel.isMoving() ? "walk" : "eat";
        }
        if (z instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieBeachFastSwimmer swimmer) {
            return swimmer.isInWater() ? "particles" : swimmer.isMoving() ? "walk" : "eat";
        }
        if (z instanceof com.PVZ.model.entity.zombies.types.ranged_caster.ZombieDarkJuggler juggler) {
            return juggler.isSpinning() ? "spin" : juggler.isMoving() ? "walk" : "eat";
        }
        if (z instanceof com.PVZ.model.entity.zombies.types.zomboss.AbstractZomboss boss) {
            if (boss.isStunned()) return "stun_loop";
        }
        return state;
    }

    private static void applyTint(SpriteBatch batch, Zombie z, float stateTime) {
        if (z.isFrozen()) {
            batch.setColor(0.5f, 0.7f, 1f, 1f);
        } else if (z.hasStatusEffect(com.PVZ.model.enums.DamageType.ICE)) {
            batch.setColor(0.7f, 0.85f, 1f, 1f);
        } else if (z.hasStatusEffect(com.PVZ.model.enums.DamageType.POISON)) {
            batch.setColor(0.7f, 0.3f, 0.9f, 1f);
        } else if (z.isHypnotized()) {
            batch.setColor(1f, 0.6f, 0.9f, 1f);
        } else if (z.isGlowing()) {
            batch.setColor(0.8f, 1f, 0.5f, 1f);
        } else if (z instanceof com.PVZ.model.entity.zombies.types.special_movement.ZombieBeachSnorkel &&
            ((com.PVZ.model.entity.zombies.types.special_movement.ZombieBeachSnorkel)z).isSubmerged()) {
            batch.setColor(0.75f, 0.9f, 1f, 0.9f);
        }
    }

    private static Map<String, Boolean> buildVisibility(Zombie z) {
        Map<String, Boolean> vis = new HashMap<>();
        String armorTrack = com.PVZ.model.entity.zombies.base.ZombieTexturePaths
            .getArmorSubBranchTrack(z);
        if (armorTrack != null) {
            String[] tracks = {"zombie_armor_cone_norm","zombie_armor_cone_damage_01",
                "zombie_armor_cone_damage_02","zombie_armor_bucket_norm",
                "zombie_armor_bucket_damage_01","zombie_armor_bucket_damage_02",
                "zombie_armor_brick_norm","zombie_armor_brick_damage_01",
                "zombie_armor_brick_damage_02","zombie_armor_crown_norm",
                "zombie_armor_crown_damage_01","zombie_armor_crown_damage_02"};
            for (String t : tracks) vis.put(t, false);
            vis.put("_zombie_egypt_armor1_states", false);
            vis.put("_zombie_egypt_armor2_states", false);
            vis.put(armorTrack, true);
        }
        if (z.isButtered()) {
            vis.put("butter", true);
            vis.put("_butter", true);
            vis.put("head_butter", true);
            vis.put("_bull_head_butter", true);
        }
        if (isZombotany(z)) {
            vis.put("anim_head1", false);
            vis.put("anim_head2", false);
            vis.put("anim_head", false);
            vis.put("anim_hair", false);
            vis.put("Zombie_tie", false);
        }
        return vis.isEmpty() ? null : vis;
    }

    private static void drawClip(EntityRenderer er, SpriteBatch batch, ClipRef clip,
                                 float time, double x, double y, Map<String, Boolean> vis) {
        if (vis != null) {
            er.getPamPlayer().draw(batch, clip, time, (float)x, (float)y, true, vis);
        } else {
            er.getPamPlayer().draw(batch, clip, time, (float)x, (float)y, true);
        }
    }

    private static void renderBossExtras(EntityRenderer er, SpriteBatch batch,
                                         Zombie z, float time) {
        // simplified: skip complex boss effects for brevity
    }

    private static void renderButter(EntityRenderer er, SpriteBatch batch, Zombie z, float time) {
        if (z.isButtered()) {
            er.renderPam(batch, "768/INITIAL/EFFECTS/SPLAT_KERNALPULT_BUTTER/SPLAT_KERNALPULT_BUTTER.PAM",
                "animation", time, (float)z.getX()+15f, (float)z.getY()+75f);
        }
    }

    private static void renderZombotanyHead(EntityRenderer er, SpriteBatch batch,
                                            Zombie z, float time) {
        if (!isZombotany(z) || z.isDying()) return;
        String plant = zombotanyPlantType(z.getAlias());
        ClipRef head = er.getPlantClip(plant, "idle");
        if (head == null) return;
        float scale, dx, dy;
        switch (plant) {
            case "PEASHOOTER": scale=0.34f; dx=-4f; dy=78f; break;
            case "WALL_NUT": scale=0.32f; dx=-2f; dy=76f; break;
            case "JALAPENO": scale=0.31f; dx=-2f; dy=78f; break;
            case "SQUASH": scale=0.33f; dx=-3f; dy=78f; break;
            default: scale=0.32f; dx=-2f; dy=76f;
        }
        er.renderPlantExact(batch, plant, "idle", time,
            (float)z.getX()+dx, (float)z.getY()+dy);
    }

    static void renderAlias(EntityRenderer er, SpriteBatch batch, String alias,
                            String state, float time, float x, float y) {
        renderAlias(er, batch, alias, state, time, x, y, 1f);
    }

    static void renderAlias(EntityRenderer er, SpriteBatch batch, String alias,
                            String state, float time, float x, float y, float scale) {
        if (alias == null) return;
        er.update();
        if (isZombotanyAlias(alias)) {
            renderZombotanyAlias(er, batch, alias, state, time, x, y, scale);
            return;
        }
        if (isCamelAlias(alias)) {
            renderCamelAlias(er, batch, alias, state, time, x, y, scale);
            return;
        }
        ClipRef clip = getClip(er, alias, state != null ? state : "idle");
        if (clip == null) clip = getClip(er, alias, "walk");
        if (clip == null) clip = getClip(er, "DEFAULT", "walk");
        if (clip == null) return;
        drawClipScaled(er, batch, clip, time, x, y, scale, previewArmorVisibility(alias));
    }

    /**
     * SpriteBatch only uploads a new transform when setTransformMatrix is called.
     * Mutating getTransformMatrix() in place left previews at the origin, so I,Zombie
     * cards and packet icons appeared in the wrong place.
     */
    private static void drawClipScaled(EntityRenderer er, SpriteBatch batch, ClipRef clip,
                                       float time, float x, float y, float scale,
                                       Map<String, Boolean> vis) {
        Matrix4 old = null;
        if (scale != 1f) {
            old = batch.getTransformMatrix().cpy();
            Matrix4 scaled = old.cpy()
                .translate(x, y, 0f)
                .scale(scale, scale, 1f)
                .translate(-x, -y, 0f);
            batch.setTransformMatrix(scaled);
        }
        if (vis != null) {
            er.getPamPlayer().draw(batch, clip, time, x, y, true, vis);
        } else {
            er.getPamPlayer().draw(batch, clip, time, x, y, true);
        }
        if (old != null) {
            batch.setTransformMatrix(old);
        }
    }

    /** Cone / bucket / brick / crown pieces live in the same PAM as the base zombie. */
    private static Map<String, Boolean> previewArmorVisibility(String alias) {
        String track = null;
        if (alias.contains("Armor1")) {
            track = "zombie_armor_cone_norm";
        } else if (alias.contains("Armor2")) {
            track = "zombie_armor_bucket_norm";
        } else if (alias.contains("Armor4")) {
            track = "zombie_armor_brick_norm";
        } else if (alias.contains("Armor3")) {
            track = alias.startsWith("ZombieDark")
                ? "zombie_armor_crown_norm"
                : "zombie_armor_brick_norm";
        }
        if (track == null) return null;
        Map<String, Boolean> vis = new HashMap<>();
        String[] tracks = {"zombie_armor_cone_norm","zombie_armor_cone_damage_01",
            "zombie_armor_cone_damage_02","zombie_armor_bucket_norm",
            "zombie_armor_bucket_damage_01","zombie_armor_bucket_damage_02",
            "zombie_armor_brick_norm","zombie_armor_brick_damage_01",
            "zombie_armor_brick_damage_02","zombie_armor_crown_norm",
            "zombie_armor_crown_damage_01","zombie_armor_crown_damage_02"};
        for (String t : tracks) vis.put(t, false);
        vis.put("_zombie_egypt_armor1_states", false);
        vis.put("_zombie_egypt_armor2_states", false);
        vis.put(track, true);
        return vis;
    }

    private static void renderZombotanyAlias(EntityRenderer er, SpriteBatch batch,
                                             String alias, String state, float time,
                                             float x, float y, float scale) {
        ClipRef body = getClip(er, alias, state != null ? state : "idle");
        if (body == null) body = getClip(er, alias, "walk");
        if (body == null) return;
        Map<String, Boolean> vis = new HashMap<>();
        vis.put("anim_head1", false); vis.put("anim_head2", false);
        vis.put("anim_head", false); vis.put("anim_hair", false);
        vis.put("Zombie_tie", false);
        Matrix4 old = null;
        if (scale != 1f) {
            old = batch.getTransformMatrix().cpy();
            Matrix4 scaled = old.cpy()
                .translate(x, y, 0f)
                .scale(scale, scale, 1f)
                .translate(-x, -y, 0f);
            batch.setTransformMatrix(scaled);
        }
        er.getPamPlayer().draw(batch, body, time, x, y, true, vis);
        String plant = zombotanyPlantType(alias);
        ClipRef head = er.getPlantClip(plant, "idle");
        if (head != null) {
            float dx, dy;
            switch (plant) {
                case "PEASHOOTER": dx=-4f; dy=78f; break;
                case "WALL_NUT": dx=-2f; dy=76f; break;
                case "JALAPENO": dx=-2f; dy=78f; break;
                case "SQUASH": dx=-3f; dy=78f; break;
                default: dx=-2f; dy=76f;
            }
            er.getPamPlayer().draw(batch, head, time, x+dx, y+dy, true);
        }
        if (old != null) {
            batch.setTransformMatrix(old);
        }
    }

    private static boolean isZombotany(Zombie z) {
        return isZombotanyAlias(z.getAlias());
    }

    private static boolean isZombotanyAlias(String alias) {
        return "ZombotanyPeashooterDefault".equals(alias) ||
            "ZombotanyWallnutDefault".equals(alias) ||
            "ZombotanyJalapenoDefault".equals(alias) ||
            "ZombotanySquashDefault".equals(alias);
    }

    private static String zombotanyPlantType(String alias) {
        switch (alias) {
            case "ZombotanyPeashooterDefault": return "PEASHOOTER";
            case "ZombotanyWallnutDefault": return "WALL_NUT";
            case "ZombotanyJalapenoDefault": return "JALAPENO";
            case "ZombotanySquashDefault": return "SQUASH";
            default: return "PEASHOOTER";
        }
    }

    private static boolean isCamelAlias(String alias) {
        return "ZombieCamelDefault".equals(alias)
            || "ZombieCamelMiddle".equals(alias)
            || "ZombieCamelRear".equals(alias);
    }

    private static void renderCamel(EntityRenderer er, SpriteBatch batch,
                                    com.PVZ.model.entity.zombies.types.basic.ZombieCamel camel,
                                    float stateTime) {
        float x = (float) camel.getX();
        float y = (float) camel.getY();
        float effectiveTime = (camel.isFrozen() || camel.isButtered()) ? 0f : stateTime;
        String state = ZombieAnimation.getState(camel);
        if (state == null) state = "walk";
        boolean isDying = "die".equals(state) || camel.isDead() || camel.isDying();
        Color origColor = new Color(batch.getColor());
        applyTint(batch, camel, stateTime);
        drawCamelSegments(er, batch, state, effectiveTime, x, y, isDying,
            camel.getFrontSegment() != null && (!camel.getFrontSegment().isDestroyed() || isDying),
            camel.getMiddleSegment() != null && (!camel.getMiddleSegment().isDestroyed() || isDying),
            camel.getRearSegment() != null && (!camel.getRearSegment().isDestroyed() || isDying));
        batch.setColor(origColor);
    }

    /**
     * Collection / I,Zombie cards don't have a live {@code ZombieCamel}, only an
     * alias. Draw the same three PAM boards (head / hump / tail) the in-game
     * camel used before the entity-renderer linter emptied {@code renderCamel}.
     */
    private static void renderCamelAlias(EntityRenderer er, SpriteBatch batch,
                                         String alias, String state, float time,
                                         float x, float y, float scale) {
        String clipState = state == null ? "idle" : state;
        boolean allSegments = "ZombieCamelDefault".equals(alias);
        boolean middle = allSegments || "ZombieCamelMiddle".equals(alias);
        boolean rear = allSegments || "ZombieCamelRear".equals(alias);
        boolean front = allSegments || "ZombieCamelDefault".equals(alias);
        Matrix4 old = null;
        if (scale != 1f) {
            old = batch.getTransformMatrix().cpy();
            Matrix4 scaled = old.cpy()
                .translate(x, y, 0f)
                .scale(scale, scale, 1f)
                .translate(-x, -y, 0f);
            batch.setTransformMatrix(scaled);
        }
        drawCamelSegments(er, batch, clipState, time, x, y, false, front, middle, rear);
        if (old != null) {
            batch.setTransformMatrix(old);
        }
    }

    private static void drawCamelSegments(EntityRenderer er, SpriteBatch batch,
                                          String state, float time, float x, float y,
                                          boolean isDying,
                                          boolean front, boolean middle, boolean rear) {
        String walkOrIdle = "eat".equals(state) ? "idle" : state;
        String rearState = isDying ? "die" : walkOrIdle;
        String middleState = isDying ? "die" : walkOrIdle;
        String frontState = isDying ? "die" : state;

        if (rear) {
            ClipRef rearClip = getClip(er, "ZombieCamelRear", rearState);
            if (rearClip == null) rearClip = getClip(er, "ZombieCamelRear", "walk");
            if (rearClip != null) {
                Map<String, Boolean> vis = new HashMap<>();
                vis.put("_zombie_camel_board_tail_states", true);
                vis.put("_zombie_camel_board_tail_norm", true);
                vis.put("_zombie_camel_board_head_states", false);
                vis.put("_zombie_camel_board_hump_states", false);
                drawClip(er, batch, rearClip, time, x + 240f, y, vis);
            }
        }
        if (middle) {
            ClipRef middleClip = getClip(er, "ZombieCamelMiddle", middleState);
            if (middleClip == null) middleClip = getClip(er, "ZombieCamelMiddle", "walk");
            if (middleClip != null) {
                Map<String, Boolean> vis = new HashMap<>();
                vis.put("_zombie_camel_board_hump_states", true);
                vis.put("_zombie_camel_board_hump_norm", true);
                vis.put("_zombie_camel_board_head_states", false);
                vis.put("_zombie_camel_board_tail_states", false);
                drawClip(er, batch, middleClip, time, x + 120f, y, vis);
            }
        }
        if (front) {
            ClipRef frontClip = getClip(er, "ZombieCamelDefault", frontState);
            if (frontClip == null) frontClip = getClip(er, "ZombieCamelDefault", "walk");
            if (frontClip != null) {
                Map<String, Boolean> vis = new HashMap<>();
                vis.put("_zombie_camel_board_head_states", true);
                vis.put("_zombie_camel_board_head_norm", true);
                vis.put("_zombie_camel_board_hump_states", false);
                vis.put("_zombie_camel_board_tail_states", false);
                drawClip(er, batch, frontClip, time, x, y, vis);
            }
        }
    }
}
