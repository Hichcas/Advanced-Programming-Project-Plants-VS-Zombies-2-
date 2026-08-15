package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Sun;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.SunManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ZombieRa extends AbstractRangedCasterZombie {
    private int stolenSunAmount;

    public ZombieRa() {
        super("ZombieRa", 380, 100, 0.185, 700, 3000, defaultScaledProps(),
              0, 0, 3.0, 0);
        this.stolenSunAmount = 0;
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
            (float) x, (float) y, (int) projectileDamage, (float) projectileSpeed, (int) row, this));
    }

    @Override
    public void onHit(Plant target) { }

    @Override
    public void stealNearbySun(SunManager sunManager) {
        if (sunManager == null) {
            return;
        }
        double range = 150.0;
        Iterator<Sun> it = sunManager.getSuns().iterator();
        while (it.hasNext()) {
            Sun sun = it.next();
            if (!sun.isCollected() && sun.hasReachedGround()) {
                double dx = sun.getX() - x;
                double dy = sun.getY() - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < range) {
                    stolenSunAmount += sun.getAmount();
                    sun.collect();
                    com.PVZ.model.entity.zombies.base.ZombieAnimation.trigger(this, "power", 1.5);
                }
            }
        }
    }

    @Override
    public void onDestroy() { }

    @Override
    public void die(BattleController controller) {
        if (stolenSunAmount > 0 && controller != null) {
            controller.addSun(stolenSunAmount);
            System.out.println("Ra zombie returned " + stolenSunAmount + " stolen suns.");
        }
        super.die(controller);
    }

    public int getStolenSunAmount() {
        return stolenSunAmount;
    }
}
