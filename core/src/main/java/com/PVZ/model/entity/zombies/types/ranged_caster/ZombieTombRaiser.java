package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieTombRaiser extends AbstractRangedCasterZombie {
    private int maxTombs;
    private int tombsRaised;

    public ZombieTombRaiser() {
        super("ZombieTombRaiser", 320, 100, 0.185, 700, 3500, defaultScaledProps(),
              50, 100, 5.0, 5);
        this.maxTombs = 3;
        this.tombsRaised = 0;
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("ProjectileDamage", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        return list;
    }

    @Override
    public void shoot() {}

    @Override
    public void onHit() {}

    public boolean canRaiseTomb() { return tombsRaised < maxTombs; }
    public void raiseTomb() { if (canRaiseTomb()) tombsRaised++; }
}
