package model.entity.zombies.types.special_movement;

import model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachSnorkel extends AbstractSpecialMovementZombie {
    private boolean submerged;

    public ZombieBeachSnorkel() {
        super("ZombieBeachSnorkel", 350, 100, 0.185, 200, 3000, defaultScaledProps());
        this.submerged = true;
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

    public boolean isSubmerged() { return submerged; }
    public void surface() { submerged = false; }
    public void dive() { submerged = true; }
}
