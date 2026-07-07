package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachFisherman extends AbstractRangedCasterZombie {
    private boolean hookAvailable;

    public ZombieBeachFisherman() {
        super("ZombieBeachFisherman", 400, 100, 0.185, 600, 3500, defaultScaledProps(),
              0, 300, 6.0, 5);
        this.hookAvailable = true;
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
    public void shoot(BattleController controller, Plant target) {
        if (hasHook()) {
            useHook();
        }
    }

    @Override
    public void onHit(Plant target) {}

    @Override
    public String getDebugString() {
        return super.getDebugString() + (hasHook() ? "\nHOOK" : "\nNOHOOK");
    }

    public boolean hasHook() { return hookAvailable; }
    public void useHook() { hookAvailable = false; }
}
