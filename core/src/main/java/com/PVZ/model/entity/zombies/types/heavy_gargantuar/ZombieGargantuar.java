package com.PVZ.model.entity.zombies.types.heavy_gargantuar;

import com.PVZ.model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieGargantuar extends AbstractGargantuar {
    public enum Theme { BASIC, EGYPT, ICEAGE, BEACH, DARK }

    private Theme theme;

    public ZombieGargantuar(String alias, Theme theme) {
        super(alias, 3600, 0, 0.24, 1500, 3000, defaultScaledProps(),
              1500, 2.0, 1.0, 0.5, 2);
        this.theme = theme;
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("SmashDamage", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        return list;
    }

    @Override
    public void smash() {}

    @Override
    public void throwImp() {}

    public Theme getTheme() { return theme; }
}
