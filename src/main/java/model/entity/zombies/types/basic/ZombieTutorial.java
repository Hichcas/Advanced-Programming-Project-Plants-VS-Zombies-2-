package model.entity.zombies.types.basic;

import model.entity.zombies.base.ScaledProperty;
import model.entity.zombies.base.ZombieArmor;

import java.util.ArrayList;
import java.util.List;

public class ZombieTutorial extends AbstractBasicZombie {
    private boolean isFlag;

    public ZombieTutorial(String alias, ZombieArmor armor) {
        super(alias, 190, 100, 0.185, 100, 1000, defaultScaledProps());
        this.isFlag = alias.contains("Flag");
        this.armor = armor;
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        return list;
    }

    public boolean isFlagBearer() { return isFlag; }
}
