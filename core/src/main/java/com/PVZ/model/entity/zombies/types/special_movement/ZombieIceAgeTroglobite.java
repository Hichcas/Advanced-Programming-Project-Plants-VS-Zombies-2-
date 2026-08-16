package com.PVZ.model.entity.zombies.types.special_movement;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombieIceAgeTroglobite extends AbstractSpecialMovementZombie {
    private int iceBlocksRemaining;
    private double lastBlockCol;
    private float pushTimer;

    private boolean initialBlockSpawned;
    private int currentBlockCol;
    private int currentBlockHp;

    public ZombieIceAgeTroglobite() {
        super("ZombieIceAgeTroglobite", 470, 100, 0.185, 600, 3500, defaultScaledProps());
        this.iceBlocksRemaining = 3;
        this.lastBlockCol = 8;
        this.pushTimer = 0.0f;
        this.initialBlockSpawned = false;
        this.currentBlockCol = -1;
        this.currentBlockHp = 1800;
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
    public void update(float delta, BattleController ctrl) {
        updateEffects(delta);
        ZombieAnimation.tick(this, delta);

        if (isDying()) {
            animStateTime += delta;
            if (!ZombieAnimation.isActive(this)) {
                finishDeath(ctrl);
            }
            return;
        }

        if (!isFrozen()) {
            animStateTime += delta;
        }

        if (hitpoints <= 0 && (armor == null || armor.isDestroyed())) {
            startDeath(ctrl);
            return;
        }
        if (isFrozen()) {
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, ctrl);
            return;
        }
        if (hypnotized) {
            updateHypnotized(delta, ctrl);
            hitbox.setPosition((float) x, (float) y);
            onUpdate(delta, ctrl);
            return;
        }
        if (ctrl == null) {
            return;
        }

        int tileCol = ctrl.getTileColumn((float) x);
        col = tileCol;
        Plant plantInFront = ctrl.getPlantAt((int) row, tileCol);

        // Spawn initial ice block immediately in front on spawn
        if (!initialBlockSpawned && ctrl.getMap() != null) {
            initialBlockSpawned = true;
            int initialCol = Math.min(8, Math.max(0, tileCol - 1));
            pushIceBlockToTile(ctrl, (int) row, initialCol);
            lastBlockCol = col;
        }

        // Push the same ice block forward as Troglobite advances to a new column
        if (Math.abs(lastBlockCol - col) >= 1) {
            int targetCol = Math.max(0, tileCol - 1);
            pushIceBlockToTile(ctrl, (int) row, targetCol);
            lastBlockCol = col;
        }

        if (plantInFront != null && !plantInFront.isDead()) {
            moving = false;
            attack(plantInFront, delta, ctrl);
        } else {
            moving = true;
            move(delta, ctrl);
        }

        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta, ctrl);
    }

    private void pushIceBlockToTile(BattleController ctrl, int targetRow, int targetCol) {
        if (ctrl == null || ctrl.getMap() == null) return;
        if (targetRow < 0 || targetRow >= 5 || targetCol < 0 || targetCol >= 9) return;

        ZombieAnimation.trigger(this, "push", 4.0333);

        // 1. Move from old tile if we already have an active block
        if (currentBlockCol >= 0 && currentBlockCol < 9 && currentBlockCol != targetCol) {
            Tile oldTile = ctrl.getMap().getTile(targetRow, currentBlockCol);
            if (oldTile != null && oldTile.getType() == TileType.ICE) {
                currentBlockHp = oldTile.getHp() > 0 ? oldTile.getHp() : currentBlockHp;
                oldTile.setType(TileType.NORMAL);
                oldTile.setHp(0);
            }
        }

        // 2. Place on new tile ahead
        Tile targetTile = ctrl.getMap().getTile(targetRow, targetCol);
        if (targetTile != null) {
            Plant p = targetTile.getPlant();
            if (p != null && !p.isDead()) {
                p.takeDamage(99999, this, ctrl);
                System.out.println("[Troglobite] Crushed plant at (" + targetRow + ", " + targetCol + ")");
            }
            targetTile.setType(TileType.ICE);
            targetTile.setHp(currentBlockHp > 0 ? currentBlockHp : 1800);
            currentBlockCol = targetCol;
        }

        System.out.println("[Troglobite] Moved ice block to (" + targetRow + ", " + targetCol + ") with " + currentBlockHp + " HP");
    }

    @Override
    public void onMove(BattleController ctrl) {}

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nICE:" + iceBlocksRemaining;
    }

    public int getIceBlocksRemaining() { return iceBlocksRemaining; }
    public void pushIceBlock() { if (iceBlocksRemaining > 0) iceBlocksRemaining--; }
}
