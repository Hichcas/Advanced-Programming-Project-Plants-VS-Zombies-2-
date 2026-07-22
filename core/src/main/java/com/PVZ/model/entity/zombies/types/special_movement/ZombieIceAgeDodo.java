package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieIceAgeDodo extends AbstractSpecialMovementZombie {
    private boolean isFlying;

    public ZombieIceAgeDodo() {
        super("ZombieIceAgeDodo", 490, 100, 0.3, 600, 3500, defaultScaledProps());
        this.isFlying = true;
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
        if (hypnotized) {
            updateHypnotized(delta, ctrl);
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, ctrl);
            return;
        }
        int tileCol = ctrl.getTileColumn((float) x);
        col = tileCol;

        // Dodo flies over plants — never stops for them
        moving = true;
        move(delta, ctrl);
        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta, ctrl);
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + (isFlying ? "\nFLYING" : "");
    }

    public boolean isFlying() { return isFlying; }
    public void setFlying(boolean flying) { isFlying = flying; }
}
