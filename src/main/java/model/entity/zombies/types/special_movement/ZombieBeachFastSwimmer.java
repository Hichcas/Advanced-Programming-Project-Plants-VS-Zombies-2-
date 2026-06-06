package model.entity.zombies.types.special_movement;

import model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachFastSwimmer extends AbstractSpecialMovementZombie {
    private boolean inWater;

    public ZombieBeachFastSwimmer() {
        super("ZombieBeachFastSwimmer", 300, 100, 0.35, 300, 2500, defaultScaledProps());
        this.inWater = true;
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

    public boolean isInWater() { return inWater; }
    public void setInWater(boolean inWater) {
        this.inWater = inWater;
        if (!inWater) speed = 0.185;
    }
}
