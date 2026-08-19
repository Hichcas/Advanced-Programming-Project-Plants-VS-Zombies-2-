package com.PVZ.model.entity.zombies.types.zombotany;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.entity.zombies.types.ranged_caster.AbstractRangedCasterZombie;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombotanyPeashooter extends AbstractRangedCasterZombie {

    public ZombotanyPeashooter() {

        super("ZombotanyPeashooterDefault", 220, 100, 0.185, 150, 1500, defaultScaledProps(),
                45, 260, 1.5, 9);
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
    public void shoot(BattleController controller, Plant target) {

        controller.addZombieProjectile(new ZombieProjectile(
                (float) x - 14f, (float) y + 100f, (int) projectileDamage, (float) projectileSpeed, (int) row, this));
    }

    @Override
    public void onHit(Plant target) {

    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nZOMBOTANY_PEASHOOTER";
    }
}
