package com.PVZ.view.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import pvz.libpvz.pam.ClipRef;
import com.PVZ.model.entity.zombies.base.ZombieArmor;

import java.util.HashMap;
import java.util.Map;

final class FallingArmorHelper {

    static final class FallingArmorPiece {
        float x, y, vx, vy, lifetime, alpha;
        String pamAlias, armorTrack;
        FallingArmorPiece(float x, float y, String pamAlias, String armorTrack) {
            this.x = x; this.y = y;
            vx = (float)(Math.random()*40-20);
            vy = (float)(Math.random()*60+80);
            lifetime = 0.8f; alpha = 1f;
            this.pamAlias = pamAlias; this.armorTrack = armorTrack;
        }
        boolean update(float delta) {
            x += vx*delta; y += vy*delta; vy -= 350f*delta;
            lifetime -= delta;
            if (lifetime < 0.3f) alpha = Math.max(0f, lifetime/0.3f);
            return lifetime <= 0;
        }
    }

    static void spawn(EntityRenderer er, float x, float y,
                      ZombieArmor.ArmorType type, String alias) {
        String pamAlias = alias != null ? alias : "ZombieTutorialArmor1Default";
        String track = null;
        if (type == ZombieArmor.ArmorType.CONE) track = "zombie_armor_cone_damage_02";
        else if (type == ZombieArmor.ArmorType.BUCKET) track = "zombie_armor_bucket_damage_02";
        else if (type == ZombieArmor.ArmorType.BRICK || type == ZombieArmor.ArmorType.ICE_BLOCK)
            track = "zombie_armor_brick_damage_02";
        else if (type == ZombieArmor.ArmorType.CROWN) track = "zombie_armor_crown_damage_02";
        er.fallingArmors().add(new FallingArmorPiece(x, y, pamAlias, track));
    }

    static void render(EntityRenderer er, SpriteBatch batch) {
        if (er.fallingArmors().isEmpty()) return;
        float delta = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        Color orig = batch.getColor().cpy();
        for (FallingArmorPiece piece : er.fallingArmors()) {
            if (piece.update(delta)) {
                er.fallingArmors().remove(piece);
                continue;
            }
            ClipRef clip = er.getZombieClip(piece.pamAlias, "walk");
            if (clip == null) continue;
            batch.setColor(1f,1f,1f,piece.alpha);
            if (piece.armorTrack != null) {
                Map<String, Boolean> vis = new HashMap<>();
                vis.put("zombie_armor_cone_norm", false);
                vis.put("zombie_armor_cone_damage_01", false);
                vis.put("zombie_armor_cone_damage_02", false);
                vis.put("zombie_armor_bucket_norm", false);
                vis.put("zombie_armor_bucket_damage_01", false);
                vis.put("zombie_armor_bucket_damage_02", false);
                vis.put("zombie_armor_brick_norm", false);
                vis.put("zombie_armor_brick_damage_01", false);
                vis.put("zombie_armor_brick_damage_02", false);
                vis.put(piece.armorTrack, true);
                er.getPamPlayer().draw(batch, clip, 0.5f, piece.x, piece.y, false, vis);
            } else {
                er.getPamPlayer().draw(batch, clip, 0.5f, piece.x, piece.y, false);
            }
        }
        batch.setColor(orig);
    }
}
