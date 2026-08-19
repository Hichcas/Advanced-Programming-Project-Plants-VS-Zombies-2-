package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieArcade extends AbstractSpecialMovementZombie {

    private float spawnTimer = 0f;
    private static final float SPAWN_INTERVAL = 6.0f;
    private float cabinetActiveTimer = 0f;

    public ZombieArcade() {
        this("Zombie80sArcade");
    }

    public ZombieArcade(String alias) {
        super(alias != null ? alias : "Zombie80sArcade", 850, 100, 0.16, 500, 3000, defaultScaledProps());
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        return list;
    }

    public boolean isCabinetActive() {
        return cabinetActiveTimer > 0;
    }

    @Override
    public void onMove(BattleController ctrl) {
        crushPlantsInFront(ctrl);
    }

    @Override
    public void update(float delta, BattleController ctrl) {
        if (ctrl != null) {
            crushPlantsInFront(ctrl);
        }
        super.update(delta, ctrl);
        if (ctrl != null) {
            crushPlantsInFront(ctrl);
        }

        if (cabinetActiveTimer > 0) {
            cabinetActiveTimer -= delta;
        }

        if (!isDead() && !isFrozen() && ctrl != null && ctrl.getMap() != null) {
            spawnTimer += delta;
            if (spawnTimer >= SPAWN_INTERVAL) {
                spawnTimer = 0f;
                cabinetActiveTimer = 1.7f;
                int spawnCol = Math.max(0, (int) col - 1);
                Tile t = ctrl.getMap().getTile((int) row, spawnCol);
                if (t != null) {
                    Zombie eightBit = ZombieType.TUTORIAL_DEFAULT.create();
                    eightBit.initPosition(t.getX(), t.getY() + (t.getHeight() - 70f) / 2f, (int) row);
                    ctrl.addZombie(eightBit);
                    System.out.println("[ZombieArcade] Arcade cabinet spawned an 8-bit zombie at lane " + (int) row);
                }
            }
        }
    }

    private void crushPlantsInFront(BattleController controller) {
        if (controller == null || isDead()) return;
        int hereCol = controller.getTileColumn((float) x);
        int frontCol = controller.getTileColumn((float) (x - 110f));
        int midCol = controller.getTileColumn((float) (x - 55f));
        int[] colsToCheck = {hereCol, midCol, frontCol};
        for (int c : colsToCheck) {
            if (c >= 0 && c < 9) {
                Plant p = controller.getPlantAt((int) row, c);
                if (p != null && !p.isDead()) {
                    p.takeDamage(999999);
                    controller.removePlant((int) row, c);
                    System.out.println("[ZombieArcade] Arcade machine crushed plant at row=" + (int) row + " col=" + c);
                }
            }
        }
    }

    @Override
    protected void attack(Plant targetPlant, float delta, BattleController controller) {
        if (targetPlant != null && !targetPlant.isDead() && controller != null) {
            targetPlant.takeDamage(999999);
            controller.removePlant((int) row, (int) col);
            System.out.println("[ZombieArcade] Arcade machine crushed plant upon contact!");
        }
    }

    @Override
    protected void move(float delta, BattleController controller) {
        if (isFrozen()) return;
        ZombieAnimation.trigger(this, "push", 1.0);
        super.move(delta, controller);
    }
}
