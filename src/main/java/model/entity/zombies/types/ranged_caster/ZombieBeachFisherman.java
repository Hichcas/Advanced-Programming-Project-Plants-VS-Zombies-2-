package model.entity.zombies.types.ranged_caster;

import model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachFisherman extends AbstractRangedCasterZombie {
    private boolean hookAvailable;

    public ZombieBeachFisherman() {
        super("ZombieBeachFisherman", 400, 100, 0.185, 600, 3500, defaultScaledProps(),
              0, 300, 6.0, 5);
        this.hookAvailable = true;
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
    public void shoot() {}

    @Override
    public void onHit() {}

    public boolean hasHook() { return hookAvailable; }
    public void useHook() { hookAvailable = false; }
}
