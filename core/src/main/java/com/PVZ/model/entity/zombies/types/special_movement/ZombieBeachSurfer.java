package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachSurfer extends AbstractSpecialMovementZombie {
    private boolean hasSurfboard;

    public ZombieBeachSurfer() {
        super("ZombieBeachSurfer", 400, 100, 0.5, 400, 3000, defaultScaledProps());
        this.hasSurfboard = true;
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
        if (!hasSurfboard) return;
        int targetCol = (int)col - 1;
        if (targetCol >= 0) {
            Plant p = ctrl.getPlantAt((int)row, targetCol);
            if (p != null && !p.isDead()) {
                p.takeDamage(9999);
                ctrl.removePlant((int)row, targetCol);
                System.out.println(alias + " crushed a plant while surfing!");
                loseSurfboard();
            }
        }
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + (hasSurfboard ? "\nBRD" : "\nWALK");
    }

    public boolean hasSurfboard() { return hasSurfboard; }
    public void loseSurfboard() {
        hasSurfboard = false;
        speed = 0.185;
    }
}
