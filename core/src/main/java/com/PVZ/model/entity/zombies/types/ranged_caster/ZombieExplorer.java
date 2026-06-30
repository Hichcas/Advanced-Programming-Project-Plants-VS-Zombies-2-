package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieExplorer extends AbstractRangedCasterZombie {
    private boolean torchOn;

    public ZombieExplorer() {
        super("ZombieExplorer", 120, 100, 0.185, 300, 2500, defaultScaledProps(),
              150, 150, 2.0, 2);
        this.torchOn = true;
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

    public boolean isTorchOn() { return torchOn; }
    public void extinguishTorch() { torchOn = false; }
}
