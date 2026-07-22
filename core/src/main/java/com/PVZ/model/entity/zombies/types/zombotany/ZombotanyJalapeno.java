package com.PVZ.model.entity.zombies.types.zombotany;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.types.basic.AbstractBasicZombie;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.Map;

import java.util.ArrayList;
import java.util.List;

public class ZombotanyJalapeno extends AbstractBasicZombie {

    private static final float FUSE_SECONDS = 10.0f;
    private static final int BURN_DAMAGE = 1_000_000;

    private float fuseTimer = 0f;
    private boolean exploded = false;

    public ZombotanyJalapeno() {

        super("ZombotanyJalapenoDefault", 200, 100, 0.185, 175, 1500, defaultScaledProps());
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
    public void onUpdate(float delta, BattleController controller) {
        if (exploded || isDead()) {
            return;
        }
        fuseTimer += delta;
        if (fuseTimer >= FUSE_SECONDS) {
            exploded = true;
            burnRow(controller);
        }
    }

    private void burnRow(BattleController controller) {
        Map map = controller.getMap();
        int thisRow = (int) row;
        System.out.println("[ZombotanyJalapeno] fuse expired -> burning all plants in row " + thisRow);
        if (map == null) {
            return;
        }
        for (int col = 0; col < map.getCols(); col++) {
            Plant plant = controller.getPlantAt(thisRow, col);
            if (plant != null && !plant.isDead()) {
                plant.takeDamage(BURN_DAMAGE);
            }
        }
    }

    public boolean hasExploded() { return exploded; }
    public float getFuseTimer() { return fuseTimer; }

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nZOMBOTANY_JALAPENO fuse=" + String.format("%.1f", fuseTimer) + "s";
    }
}
