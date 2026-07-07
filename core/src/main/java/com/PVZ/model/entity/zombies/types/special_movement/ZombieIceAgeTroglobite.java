package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieIceAgeTroglobite extends AbstractSpecialMovementZombie {
    private int iceBlocksRemaining;
    private double lastBlockCol;

    public ZombieIceAgeTroglobite() {
        super("ZombieIceAgeTroglobite", 470, 100, 0.185, 600, 3500, defaultScaledProps());
        this.iceBlocksRemaining = 3;
        this.lastBlockCol = 8;
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
        if (iceBlocksRemaining > 0 && Math.abs(lastBlockCol - col) >= 2) {
            pushIceBlock();
            lastBlockCol = col;
            System.out.println(alias + " pushed an ice block! (" + iceBlocksRemaining + " left)");
        }
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nICE:" + iceBlocksRemaining;
    }

    public int getIceBlocksRemaining() { return iceBlocksRemaining; }
    public void pushIceBlock() { if (iceBlocksRemaining > 0) iceBlocksRemaining--; }
}
