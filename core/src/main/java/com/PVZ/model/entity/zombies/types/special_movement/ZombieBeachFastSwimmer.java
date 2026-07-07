package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachFastSwimmer extends AbstractSpecialMovementZombie {
    private boolean inWater;

    public ZombieBeachFastSwimmer() {
        super("ZombieBeachFastSwimmer", 300, 100, 0.35, 300, 2500, defaultScaledProps());
        this.inWater = true;
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
    public void onMove(BattleController ctrl) {
        boolean wasInWater = inWater;
        boolean isWater = ctrl.getTileTypeAt((int)row, (int)col) == TileType.WATER;
        if (isWater) {
            inWater = true;
            if (!wasInWater) {
                speed = 0.35;
                System.out.println(alias + " entered water!");
            }
        } else {
            inWater = false;
            if (wasInWater) {
                speed = 0.185;
                System.out.println(alias + " reached land!");
            }
        }
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + (inWater ? "\nSWIM" : "\nWALK");
    }

    public boolean isInWater() { return inWater; }
}
