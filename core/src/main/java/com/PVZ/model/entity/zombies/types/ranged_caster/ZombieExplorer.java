package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieExplorer extends AbstractRangedCasterZombie {
    private boolean torchOn;

    public ZombieExplorer() {
        super("ZombieExplorer", 120, 100, 0.185, 300, 2500, defaultScaledProps(),
              150, 150, 2.0, 2);
        this.torchOn = true;
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
        int dmg = torchOn ? (int) projectileDamage * 2 : (int) projectileDamage;
        controller.addZombieProjectile(new ZombieProjectile(
            (float) x, (float) y, dmg, (float) projectileSpeed, (int) row, this));
    }

    @Override
    public void onHit(Plant target) {}

    @Override
    public String getDebugString() {
        return super.getDebugString() + (torchOn ? "\nFIRE" : "\nNOFIRE");
    }

    public boolean isTorchOn() { return torchOn; }
    public void extinguishTorch() { torchOn = false; }
}
