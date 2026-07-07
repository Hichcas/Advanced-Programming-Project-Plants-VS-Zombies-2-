package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieWeasel extends AbstractSpecialMovementZombie {
    public ZombieWeasel() {
        super("ZombieWeaselDefault", 100, 50, 0.4, 50, 500, defaultScaledProps());
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
    public void onMove(BattleController ctrl) {}

    @Override
    public void onSpawn() {
        System.out.println("Weasel spawned at x=" + String.format("%.1f", x) + " row=" + (int)row);
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nWEASEL";
    }
}
