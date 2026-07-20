package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachSnorkel extends AbstractSpecialMovementZombie {
    private boolean submerged;
    private float surfaceTimer;
    private static final float SURFACE_EAT_TIME = 3.0f;

    public ZombieBeachSnorkel() {
        super("ZombieBeachSnorkel", 350, 100, 0.185, 200, 3000, defaultScaledProps());
        this.submerged = true;
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
    public void onMove(BattleController ctrl) {}

    @Override
    public void update(float delta, BattleController ctrl) {
        updateEffects(delta);
        if (hitpoints <= 0 && (armor == null || armor.isDestroyed())) {
            die(ctrl);
            return;
        }
        int tileCol = ctrl.getTileColumn((float) x);
        col = tileCol;
        Plant plant = ctrl.getPlantAt((int) row, tileCol);
        boolean hasPlant = plant != null && !plant.isDead();

        if (submerged) {
            moving = true;
            move(delta, ctrl);
            if (hasPlant) {
                surface();
                surfaceTimer = SURFACE_EAT_TIME;
                System.out.println(alias + " surfaced!");
            }
        } else {
            surfaceTimer -= delta;
            if (hasPlant) {
                moving = false;
                attack(plant, delta, ctrl);
            } else {
                moving = true;
                move(delta, ctrl);
            }
            if (surfaceTimer <= 0) {
                dive();
                System.out.println(alias + " dove!");
            }
        }
        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta, ctrl);
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + (submerged ? "\nSUB" : "\nSURF");
    }

    public boolean isSubmerged() { return submerged; }
    public void surface() { submerged = false; }
    public void dive() { submerged = true; }

    @Override
    public boolean isProjectileImmune() { return submerged; }
}
