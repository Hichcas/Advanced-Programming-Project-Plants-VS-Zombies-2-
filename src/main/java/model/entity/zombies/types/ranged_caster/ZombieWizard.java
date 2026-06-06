package model.entity.zombies.types.ranged_caster;

import model.entity.zombies.base.ScaledProperty;

import java.util.ArrayList;
import java.util.List;

public class ZombieWizard extends AbstractRangedCasterZombie {
    private boolean hasMagicStaff;

    public ZombieWizard() {
        super("ZombieWizard", 480, 100, 0.185, 900, 4000, defaultScaledProps(),
              150, 200, 4.0, 5);
        this.hasMagicStaff = true;
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

    public boolean hasMagicStaff() { return hasMagicStaff; }
    public void transformPlantToSheep(Object target) { this.hasMagicStaff = false; }
}
