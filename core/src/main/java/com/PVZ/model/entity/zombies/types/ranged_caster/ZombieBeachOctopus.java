package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachOctopus extends AbstractRangedCasterZombie {

    public ZombieBeachOctopus() {
        super("ZombieBeachOctopus", 600, 100, 0.185, 800, 4000, defaultScaledProps(),
            80, 150, 6.0, 9);
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
        if (target == null || target.isDead() || controller == null || controller.getMap() == null) {
            return;
        }
        Object r = target.getRuntimeState("row");
        Object c = target.getRuntimeState("col");
        int targetRow = r instanceof Number ? ((Number) r).intValue() : (int) this.row;
        int targetCol = c instanceof Number ? ((Number) c).intValue() : (int) this.col;
        Tile tile = controller.getMap().getTile(targetRow, targetCol);
        if (tile != null && tile.getOctopusHp() <= 0) {
            tile.setOctopusHp(200);
            target.disableForTicks(Integer.MAX_VALUE);
            System.out.println(alias + " threw an octopus on plant at (" + targetCol + ", " + targetRow + ")!");
        }
    }

    @Override
    public void onHit(Plant target) {
    }
}
