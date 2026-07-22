package com.PVZ.model.entity.zombies.types.zombotany;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.types.basic.AbstractBasicZombie;

import java.util.ArrayList;
import java.util.List;

public class ZombotanyWallnut extends AbstractBasicZombie {

    public ZombotanyWallnut() {

        super("ZombotanyWallnutDefault", 1600, 100, 0.185, 200, 2500, defaultScaledProps());
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
    public String getDebugString() {
        return super.getDebugString() + "\nZOMBOTANY_WALLNUT";
    }
}
