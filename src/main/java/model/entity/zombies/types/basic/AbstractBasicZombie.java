package model.entity.zombies.types.basic;

import model.entity.zombies.base.ScaledProperty;
import model.entity.zombies.base.Zombie;

import java.util.List;

public abstract class AbstractBasicZombie extends Zombie {
    public AbstractBasicZombie(String alias, double hitpoints, double eatDPS, double speed,
                               int wavePointCost, int weight, List<ScaledProperty> scaledProps) {
        super(alias, hitpoints, eatDPS, speed, wavePointCost, weight, scaledProps);
    }

    @Override
    public void onSpawn() {}

    @Override
    public void onUpdate(double deltaTime) {}

    @Override
    public void onDestroy() {}
}
