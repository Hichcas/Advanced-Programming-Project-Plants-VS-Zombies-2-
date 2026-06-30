package com.PVZ.model.entity.zombies.types.zomboss;

import com.PVZ.model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieZombossMechEgypt extends AbstractZomboss {
    private boolean pyramidStompAvailable;

    public ZombieZombossMechEgypt() {
        super("ZombieZombossMechEgypt", 10000, 500, 0.12, 5000, 10000, defaultScaledProps(),
              0.33, 3);
        this.pyramidStompAvailable = true;
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
    public void onPhaseTransition() {}

    @Override
    public void useSpecialAbility() {}

    public boolean canPyramidStomp() { return pyramidStompAvailable; }
}
