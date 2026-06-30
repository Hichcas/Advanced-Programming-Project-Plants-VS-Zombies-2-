package com.PVZ.model.entity.zombies.types.basic;

import com.PVZ.model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieDarkKing extends AbstractBasicZombie {
    private double buffRadius;
    private double speedBoost;
    private double damageBoost;

    public ZombieDarkKing() {
        super("ZombieDarkKing", 800, 100, 0.185, 1200, 5000, defaultScaledProps());
        this.buffRadius = 200.0;
        this.speedBoost = 1.5;
        this.damageBoost = 1.3;
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        return list;
    }

    public double getBuffRadius() { return buffRadius; }
    public double getSpeedBoost() { return speedBoost; }
    public double getDamageBoost() { return damageBoost; }
}
