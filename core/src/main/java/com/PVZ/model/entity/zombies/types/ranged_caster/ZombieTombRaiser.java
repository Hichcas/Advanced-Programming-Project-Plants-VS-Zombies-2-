package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.Map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombieTombRaiser extends AbstractRangedCasterZombie {
    private int maxTombs;
    private int tombsRaised;
    private int ticksSinceLastGrave;
    private static final int GRAVE_INTERVAL_TICKS = 80;

    public ZombieTombRaiser() {
        super("ZombieTombRaiser", 320, 100, 0.185, 700, 3500, defaultScaledProps(),
              50, 100, 5.0, 5);
        this.maxTombs = 3;
        this.tombsRaised = 0;
        this.ticksSinceLastGrave = 0;
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
        if (canRaiseTomb()) {
            raiseTomb();
        }
    }

    @Override
    public void onHit(Plant target) { }

    @Override
    public void maybeSpawnGraves(Map map, Random random) {
        ticksSinceLastGrave++;
        if (ticksSinceLastGrave < GRAVE_INTERVAL_TICKS) {
            return;
        }
        ticksSinceLastGrave = 0;
        if (!canRaiseTomb() || map == null) {
            return;
        }
        int placed = 0;
        int attempts = 0;
        while (placed < 2 && attempts < 50) {
            int r = random.nextInt(5);
            int c = random.nextInt(9);
            Tile tile = map.getTile(r, c);
            if (tile != null && tile.getType() == TileType.NORMAL && tile.getPlant() == null) {
                tile.setType(TileType.TOMBSTONE);
                tile.setHp(700);
                tombsRaised++;
                placed++;
            }
            attempts++;
        }
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nTOMBS:" + tombsRaised + "/" + maxTombs;
    }

    public boolean canRaiseTomb() { return tombsRaised < maxTombs; }
    public void raiseTomb() { if (canRaiseTomb()) tombsRaised++; }
}
