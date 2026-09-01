package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.DamageType;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieExplorer extends Zombie {
    private boolean torchOn;

    public ZombieExplorer() {
        super("ZombieExplorer", 120, 100, 0.185, 300, 2500, defaultScaledProps());
        this.torchOn = true;
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
    public void takeDamage(int amount, DamageType type) {
        if (torchOn && type == DamageType.ICE) {
            extinguishTorch();
            System.out.println("[ZombieExplorer] Explorer zombie torch extinguished by ice attack!");
            return;
        }
        super.takeDamage(amount, type);
    }

    @Override
    public void onSpawn() { }

    @Override
    public void onDestroy() { }

    @Override
    public void onUpdate(float delta, BattleController controller) {
        if (torchOn && controller != null && !isDead()) {
            int tileCol = controller.getTileColumn((float) x);
            Plant plantInFront = controller.getPlantAt((int) row, tileCol);
            if (plantInFront != null && !plantInFront.isDead()) {
                plantInFront.takeDamage(9999);
                System.out.println("[ZombieExplorer] Explorer zombie burned plant " +
                    plantInFront.getDefinition().getName() + " instantly with torch!");
            }
        }
    }

    public boolean isTorchOn() { return torchOn; }
    public void extinguishTorch() { torchOn = false; }
    public void reigniteTorch() { torchOn = true; }

    @Override
    public String getDebugString() {
        return super.getDebugString() + (torchOn ? "\nFIRE" : "\nNOFIRE");
    }
}
