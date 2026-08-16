package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieIceAgeDodo extends AbstractSpecialMovementZombie {
    private boolean isFlying;
    private float flightTimer;
    private int flightDistanceTiles;
    private double flightStartX;

    public ZombieIceAgeDodo() {
        super("ZombieIceAgeDodo", 490, 100, 0.25, 600, 3500, defaultScaledProps());
        this.isFlying = true;
        this.flightTimer = 0.0f;
        this.flightDistanceTiles = 4;
        this.flightStartX = -1;
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        return list;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        this.flightStartX = x;
        this.isFlying = true;
        ZombieAnimation.trigger(this, "fly", 2.6667);
    }

    @Override
    public void update(float delta, BattleController ctrl) {
        updateEffects(delta);
        ZombieAnimation.tick(this, delta);

        if (isDying()) {
            animStateTime += delta;
            if (!ZombieAnimation.isActive(this)) {
                finishDeath(ctrl);
            }
            return;
        }

        if (!isFrozen()) {
            animStateTime += delta;
        }

        if (hitpoints <= 0 && (armor == null || armor.isDestroyed())) {
            startDeath(ctrl);
            return;
        }
        if (isFrozen()) {
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, ctrl);
            return;
        }
        if (hypnotized) {
            updateHypnotized(delta, ctrl);
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, ctrl);
            return;
        }
        if (ctrl == null) {
            return;
        }

        int tileCol = ctrl.getTileColumn((float) x);
        col = tileCol;
        Plant plantInFront = ctrl.getPlantAt((int) row, tileCol);

        if (isFlying) {
            flightTimer += delta;
            ZombieAnimation.trigger(this, "fly", 2.6667);

            // Check if blocked by a tall defensive plant (e.g. Tall-nut)
            if (plantInFront != null && (plantInFront.getMaxHp() >= 4000 || plantInFront.getType().name().contains("TALL"))) {
                // Land immediately when hitting a tall barricade
                isFlying = false;
                flightTimer = 0.0f;
                moving = false;
                attack(plantInFront, delta, ctrl);
            } else {
                moving = true;
                // Fly forward over ground obstacles
                x -= currentSpeed * delta * 150; // faster flight speed

                // If flown beyond distance (4 full tiles ~ 320 units), land
                float tileW = ctrl.getMap() != null ? ctrl.getMap().getTileWidth() : 80.0f;
                if (flightStartX > 0 && (flightStartX - x) >= (flightDistanceTiles * tileW)) {
                    isFlying = false;
                    flightTimer = 0.0f;
                }
            }
        } else {
            // Walking on the ground
            if (plantInFront != null && !plantInFront.isDead()) {
                moving = false;
                attack(plantInFront, delta, ctrl);
            } else {
                moving = true;
                move(delta, ctrl);
            }
        }

        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta, ctrl);
    }

    @Override
    public void onMove(BattleController ctrl) {}

    @Override
    public String getDebugString() {
        return super.getDebugString() + (isFlying ? "\nFLYING" : "\nGROUND");
    }

    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean flying) {
        isFlying = flying;
    }
}
