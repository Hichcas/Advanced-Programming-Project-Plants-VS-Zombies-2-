package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachOctopus extends AbstractRangedCasterZombie {
    private boolean tentaclesAttached;

    public ZombieBeachOctopus() {
        super("ZombieBeachOctopus", 600, 100, 0.185, 800, 4000, defaultScaledProps(),
              80, 150, 2.5, 3);
        this.tentaclesAttached = true;
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
        if (hasTentacles()) {
            controller.addZombieProjectile(new ZombieProjectile(
                (float) x, (float) y, (int) projectileDamage, (float) projectileSpeed, (int) row, this));
            detachTentacles();
        }
    }

    @Override
    public void onHit(Plant target) {
        if (target != null && !target.isDead()) {
            target.disableForTicks(30);
            System.out.println(alias + " froze a plant");
        }
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + (hasTentacles() ? "\nTENT" : "\nNOTENT");
    }

    public boolean hasTentacles() { return tentaclesAttached; }
    public void detachTentacles() { tentaclesAttached = false; }
}
