package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieIceAgeHunter extends AbstractRangedCasterZombie {
    private boolean spearThrown;

    public ZombieIceAgeHunter() {
        super("ZombieIceAgeHunter", 380, 100, 0.185, 600, 3000, defaultScaledProps(),
              200, 250, 4.0, 3);
        this.spearThrown = false;
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
        if (hasSpear()) {
            controller.addZombieProjectile(new ZombieProjectile(
                (float) x, (float) y, (int) projectileDamage, (float) projectileSpeed, (int) row, this));
            throwSpear();
        }
    }

    @Override
    public void onHit(Plant target) {}

    @Override
    public String getDebugString() {
        return super.getDebugString() + (hasSpear() ? "\nSPEAR" : "\nTHROWN");
    }

    public boolean hasSpear() { return !spearThrown; }
    public void throwSpear() { spearThrown = true; }
}
