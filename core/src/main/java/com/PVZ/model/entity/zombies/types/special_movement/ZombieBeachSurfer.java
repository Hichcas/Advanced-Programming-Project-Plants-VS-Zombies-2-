package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.enums.TileType;
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
        if (!hasSurfboard || ctrl == null || ctrl.getMap() == null) return;
        int targetCol = (int) col - 1;
        if (targetCol >= 0) {
            Plant p = ctrl.getPlantAt((int) row, targetCol);
            if (p != null && !p.isDead()) {
                p.takeDamage(9999);
                ctrl.removePlant((int) row, targetCol);
                Tile t = ctrl.getMap().getTile((int) row, targetCol);
                if (t != null) {
                    t.setType(TileType.TOMBSTONE);
                    t.setHp(400);
                }
                System.out.println(alias + " crushed plant and left surfboard obstacle at (" +
                    targetCol + ", " + (int) row + ")!");
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
        currentSpeed = 0.185;
    }
}
