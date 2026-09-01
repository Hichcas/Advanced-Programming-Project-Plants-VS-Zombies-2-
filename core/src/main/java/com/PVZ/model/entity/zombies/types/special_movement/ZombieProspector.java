package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.enums.DamageType;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieProspector extends AbstractSpecialMovementZombie {

    private boolean dynamiteTriggered = false;
    private boolean dynamiteExtinguished = false;
    private boolean flewToLeft = false;
    private float fuseTimer = 0f;
    private static final float FUSE_DURATION = 3.5f;

    public ZombieProspector() {
        this("ZombieProspector");
    }

    public ZombieProspector(String alias) {
        super(alias != null ? alias : "ZombieProspector", 380, 100, 0.22, 350, 2000, defaultScaledProps());
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        return list;
    }

    public boolean isFlewToLeft() {
        return flewToLeft;
    }

    public boolean isDynamiteExtinguished() {
        return dynamiteExtinguished;
    }

    @Override
    public void onMove(BattleController ctrl) {
    }

    @Override
    public void update(float delta, BattleController ctrl) {
        super.update(delta, ctrl);
        if (isDead() || ctrl == null || ctrl.getMap() == null) return;

        // Ice extinguishes dynamite fuse
        if (isFrozen() || hasStatusEffect(DamageType.ICE)) {
            if (!dynamiteTriggered && !dynamiteExtinguished) {
                dynamiteExtinguished = true;
                System.out.println("[ZombieProspector] Ice extinguished the dynamite fuse!");
            }
        }

        if (!dynamiteTriggered && !dynamiteExtinguished && !isFrozen()) {
            fuseTimer += delta;
            if (fuseTimer >= FUSE_DURATION || hitpoints < maxHitpoints * 0.8) {
                dynamiteTriggered = true;
                ZombieAnimation.trigger(this, "blastoff", 1.0);

                // Blast off and land safely in Column 0 on the lawn (well ahead of lawnmower)
                Tile t0 = ctrl.getMap().getTile((int) row, 0);
                float tileW = t0 != null ? t0.getWidth() : ctrl.getMap().getTileWidth();
                if (t0 != null) {
                    this.x = t0.getX() + tileW * 0.6f;
                } else {
                    this.x = ctrl.getMap().getStartX() + tileW * 0.6f;
                }
                flewToLeft = true;
                System.out.println("[ZombieProspector] Dynamite blasted "+
                    "Prospector to Column 0; walking backwards towards plants!");
            }
        }
    }

    @Override
    protected void move(float delta, BattleController controller) {
        if (isFrozen()) return;
        if (flewToLeft) {
            // Move rightwards (eastward) into the lawn to attack plants from behind
            this.x += currentSpeed * delta * 100.0;
            if (controller != null && controller.getMap() != null) {
                int tileCol = controller.getTileColumn((float) x);
                col = tileCol;
            }
            ZombieAnimation.trigger(this, "walk", 1.0);
        } else {
            super.move(delta, controller);
        }
    }
}
