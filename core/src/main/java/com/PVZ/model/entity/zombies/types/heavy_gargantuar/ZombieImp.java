package com.PVZ.model.entity.zombies.types.heavy_gargantuar;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.DamageType;

import java.util.ArrayList;
import java.util.List;

public class ZombieImp extends Zombie {
    public enum Theme { BASIC, EGYPT, ICEAGE, BEACH, DARK }

    private Theme theme;

    public ZombieImp(String alias, Theme theme) {
        super(alias, 190, 100, 0.22, 100, 1000, defaultScaledProps());
        this.theme = theme;
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
    public boolean isFireImmune() {
        return theme == Theme.DARK || super.isFireImmune();
    }

    @Override
    public void takeDamage(int amount, DamageType type) {
        if (type == DamageType.FIRE && isFireImmune()) {
            System.out.println(alias + " is completely immune to fire damage!");
            return;
        }
        super.takeDamage(amount, type);
    }

    @Override
    public void onSpawn() {}

    @Override
    public void onDestroy() {}

    public Theme getTheme() { return theme; }
}
