package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.view.renderer.EntityRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class OctopusProjectile extends ZombieProjectile {

    private final float startX, startY;
    private final float targetX, targetY;
    private final Plant targetPlant;
    private final Tile targetTile;
    private final float totalDuration;
    private float flightTimer = 0f;
    private float animStateTime = 0f;

    public OctopusProjectile(float startX, float startY, float targetX, float targetY,
                             int row, Zombie owner, Plant targetPlant, Tile targetTile) {
        super(startX, startY, 0, 0, row, owner);
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetPlant = targetPlant;
        this.targetTile = targetTile;
        this.totalDuration = 0.85f;
        this.x = startX;
        this.y = startY;
        this.hitbox.setPosition(startX, startY);
    }

    @Override
    public void update(float delta) {
        if (destroyed) return;
        flightTimer += delta;
        animStateTime += delta;

        float progress = Math.min(1.0f, flightTimer / totalDuration);

        // Smooth parabolic arc flight
        float currentLinearX = startX + (targetX - startX) * progress;
        float currentLinearY = startY + (targetY - startY) * progress;
        float arcHeight = 140f * (float) Math.sin(Math.PI * progress);

        this.x = currentLinearX;
        this.y = currentLinearY + arcHeight;
        this.hitbox.setPosition(x, y);

        if (progress >= 1.0f) {
            landOnTarget();
        }
    }

    private void landOnTarget() {
        if (targetTile != null && targetTile.getOctopusHp() <= 0) {
            targetTile.setOctopusHp(200);
            if (targetPlant != null && !targetPlant.isDead()) {
                targetPlant.disableForTicks(Integer.MAX_VALUE);
            }
            System.out.println("[OctopusProjectile] Landed on target tile! Attached octopus with 200 HP.");
        }
        destroy();
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (destroyed) return;

        boolean rendered = EntityRenderer.getInstance().renderPam(
            batch,
            "768/FULL/EFFECTS/ZOMBIE_OCTOPUS_PROJECTILE/ZOMBIE_OCTOPUS_PROJECTILE.PAM",
            "animation",
            animStateTime,
            x,
            y,
            1.0f
        );

        if (!rendered) {
            // High-visibility fallback sprite
            if (texture != null) {
                batch.draw(texture, x - 16f, y - 16f, 32f, 32f);
            }
        }
    }
}
