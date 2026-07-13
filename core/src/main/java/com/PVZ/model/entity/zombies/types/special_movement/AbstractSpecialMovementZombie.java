package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.game.BattleController;

import java.util.List;

public abstract class AbstractSpecialMovementZombie extends Zombie {
    public AbstractSpecialMovementZombie(String alias, double hitpoints, double eatDPS, double speed,
                                         int wavePointCost, int weight, List<ScaledProperty> scaledProps) {
        super(alias, hitpoints, eatDPS, speed, wavePointCost, weight, scaledProps);
    }

    @Override
    public void onSpawn() {}

    @Override
    public void onDestroy() {}

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
        if (plant != null && !plant.isDead()) {
            moving = false;
            attack(plant, delta, ctrl);
        } else {
            moving = true;
            move(delta, ctrl);
            onMove(ctrl);
        }
        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta, ctrl);
    }

    public abstract void onMove(BattleController ctrl);
}
