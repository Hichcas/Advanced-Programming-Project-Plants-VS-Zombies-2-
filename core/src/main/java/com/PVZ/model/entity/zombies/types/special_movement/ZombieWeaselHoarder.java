package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieWeaselHoarder extends AbstractSpecialMovementZombie {
    private int weaselsToRelease;

    public ZombieWeaselHoarder() {
        super("ZombieWeaselHoarderDefault", 500, 100, 0.185, 500, 2500, defaultScaledProps());
        this.weaselsToRelease = 3;
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

    public int getWeaselsToRelease() { return weaselsToRelease; }
    public void releaseWeasel() { if (weaselsToRelease > 0) weaselsToRelease--; }
}
