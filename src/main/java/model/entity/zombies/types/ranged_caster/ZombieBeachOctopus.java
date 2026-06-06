package model.entity.zombies.types.ranged_caster;

import model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachOctopus extends AbstractRangedCasterZombie {
    private boolean tentaclesAttached;

    public ZombieBeachOctopus() {
        super("ZombieBeachOctopus", 600, 100, 0.185, 800, 4000, defaultScaledProps(),
              80, 150, 2.5, 3);
        this.tentaclesAttached = true;
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

    public boolean hasTentacles() { return tentaclesAttached; }
    public void detachTentacles() { tentaclesAttached = false; }
}
