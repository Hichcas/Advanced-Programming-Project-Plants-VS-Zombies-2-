package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieIceAgeDodo extends AbstractSpecialMovementZombie {
    private boolean isFlying;

    public ZombieIceAgeDodo() {
        super("ZombieIceAgeDodo", 490, 100, 0.3, 600, 3500, defaultScaledProps());
        this.isFlying = true;
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

    public boolean isFlying() { return isFlying; }
    public void setFlying(boolean flying) { isFlying = flying; }
}
