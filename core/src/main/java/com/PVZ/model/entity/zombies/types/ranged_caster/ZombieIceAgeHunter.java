package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieIceAgeHunter extends AbstractRangedCasterZombie {
    private boolean spearThrown;

    public ZombieIceAgeHunter() {
        super("ZombieIceAgeHunter", 380, 100, 0.185, 600, 3000, defaultScaledProps(),
              200, 250, 4.0, 3);
        this.spearThrown = false;
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

    public boolean hasSpear() { return !spearThrown; }
    public void throwSpear() { spearThrown = true; }
}
