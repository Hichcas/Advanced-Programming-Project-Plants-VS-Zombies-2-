package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;

public abstract class AbstractSpecialMovementZombie extends Zombie {
    public AbstractSpecialMovementZombie(String alias, double hitpoints, double eatDPS, double speed,
                                         int wavePointCost, int weight, List<ScaledProperty> scaledProps) {
        super(alias, hitpoints, eatDPS, speed, wavePointCost, weight, scaledProps);
    }

    @Override
    public void onSpawn() {}

    @Override
    public void onUpdate(double deltaTime) {}

    @Override
    public void onDestroy() {}

    public abstract void onMove();
}
