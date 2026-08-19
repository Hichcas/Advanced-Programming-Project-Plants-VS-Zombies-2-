package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieCrystalSkull extends AbstractRangedCasterZombie {

    public ZombieCrystalSkull() {
        this("ZombieLostCityCrystalSkull");
    }

    public ZombieCrystalSkull(String alias) {
        super(alias != null ? alias : "ZombieLostCityCrystalSkull", 650, 100, 0.18, 600, 3000, defaultScaledProps(),
              400, 250, 4.0, 5);
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
        ZombieAnimation.trigger(this, "attack", 1.9667);
        if (controller != null && target != null) {
            controller.addZombieProjectile(new ZombieProjectile(
                (float) x, (float) y, (int) projectileDamage, (float) projectileSpeed, (int) row, this));
            System.out.println("[ZombieCrystalSkull] Fired crystal beam at plant in lane " + (int) row);
        }
    }

    @Override
    public void onHit(Plant target) {
        if (target != null) {
            target.takeDamage((int) projectileDamage);
        }
    }
}
