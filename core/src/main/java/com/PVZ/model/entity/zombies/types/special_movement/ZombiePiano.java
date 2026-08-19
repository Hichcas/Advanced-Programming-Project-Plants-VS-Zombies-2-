package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombiePiano extends AbstractSpecialMovementZombie {

    public ZombiePiano() {
        this("ZombiePiano");
    }

    public ZombiePiano(String alias) {
        super(alias != null ? alias : "ZombiePiano", 900, 100, 0.16, 500, 3000, defaultScaledProps());
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
        crushPlantsInFront(ctrl);
    }

    @Override
    public void update(float delta, BattleController controller) {
        if (controller != null) {
            crushPlantsInFront(controller);
        }
        super.update(delta, controller);
        if (controller != null) {
            crushPlantsInFront(controller);
        }
    }

    private void crushPlantsInFront(BattleController controller) {
        if (controller == null || isDead()) return;
        int hereCol = controller.getTileColumn((float) x);
        int frontCol = controller.getTileColumn((float) (x - 40f));
        int[] colsToCheck = {hereCol, frontCol};
        for (int c : colsToCheck) {
            if (c >= 0 && c < 9) {
                Plant p = controller.getPlantAt((int) row, c);
                if (p != null && !p.isDead()) {
                    p.takeDamage(999999);
                    controller.removePlant((int) row, c);
                    System.out.println("[ZombiePiano] Piano crushed plant at row=" + (int) row + " col=" + c);
                }
            }
        }
    }

    @Override
    protected void attack(Plant targetPlant, float delta, BattleController controller) {
        if (targetPlant != null && !targetPlant.isDead() && controller != null) {
            targetPlant.takeDamage(999999);
            controller.removePlant((int) row, (int) col);
            System.out.println("[ZombiePiano] Piano crushed plant upon contact!");
        }
    }

    @Override
    protected void move(float delta, BattleController controller) {
        if (isFrozen()) return;
        ZombieAnimation.trigger(this, "play", 1.0);
        super.move(delta, controller);
    }
}
