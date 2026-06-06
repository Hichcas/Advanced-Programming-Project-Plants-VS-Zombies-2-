package model.entity.zombies.types.basic;

import model.entity.zombies.base.ScaledProperty;
import model.entity.zombies.base.ZombieArmor;

import java.util.ArrayList;
import java.util.List;

public class ZombieDark extends AbstractBasicZombie {
    private ZombieArmor secondaryArmor;

    public ZombieDark(String alias, ZombieArmor armor) {
        super(alias, 190, 100, 0.185, 100, 1000, defaultScaledProps());
        this.armor = armor;
    }

    public ZombieDark(String alias, ZombieArmor armor1, ZombieArmor armor2) {
        this(alias, armor1);
        this.secondaryArmor = armor2;
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        return list;
    }

    public ZombieArmor getSecondaryArmor() { return secondaryArmor; }

    @Override
    public double getEffectiveHitpoints() {
        double base = super.getEffectiveHitpoints();
        if (secondaryArmor != null) {
            base += secondaryArmor.getHealth();
        }
        return base;
    }
}
