package com.PVZ.model.entity.zombies.types.zombotany;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.types.basic.AbstractBasicZombie;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombotanySquash extends AbstractBasicZombie {

    private static final int CRUSH_DAMAGE = 1_000_000;

    public ZombotanySquash() {

        super("ZombotanySquashDefault", 200, 100, 0.70, 175, 1200, defaultScaledProps());
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
    protected void attack(Plant targetPlant, float delta, BattleController controller) {
        if (isFrozen()) {
            return;
        }

        if (targetPlant != null && !targetPlant.isDead()) {
            targetPlant.takeDamage(CRUSH_DAMAGE);
        }

        setHitpoints(0);
        die(controller);
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nZOMBOTANY_SQUASH";
    }
}
