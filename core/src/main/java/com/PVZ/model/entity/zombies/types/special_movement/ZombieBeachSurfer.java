package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachSurfer extends AbstractSpecialMovementZombie {
    private boolean hasSurfboard;

    public ZombieBeachSurfer() {
        super("ZombieBeachSurfer", 400, 100, 0.5, 400, 3000, defaultScaledProps());
        this.hasSurfboard = true;
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
    public void onMove() {}

    public boolean hasSurfboard() { return hasSurfboard; }
    public void loseSurfboard() {
        hasSurfboard = false;
        speed = 0.185;
    }
}
