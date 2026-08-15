package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.Map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ZombieTombRaiser extends AbstractRangedCasterZombie {
    private int maxTombs;
    private int tombsRaised;
    private float throwCooldownTimer;
    private static final float BONE_THROW_INTERVAL_SECONDS = 12.0f;
    private final Random random = new Random();
    private Map map;
    private final List<ZombieProjectile> bones = new ArrayList<>();

    public ZombieTombRaiser() {
        super("ZombieTombRaiser", 320, 100, 0.185, 700, 3500, defaultScaledProps(),
            50, 100, 5.0, 5);
        this.maxTombs = 3;
        this.tombsRaised = 0;
        this.throwCooldownTimer = 0f;
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
    }

    @Override
    public void onUpdate(float delta, BattleController controller) {
        this.map = controller != null ? controller.getMap() : this.map;
        if (map != null) {
            for (int i = bones.size() - 1; i >= 0; i--) {
                ZombieProjectile bone = bones.get(i);
                int boneCol = controller != null ? controller.getTileColumn(bone.getX()) : bone.getTargetCol();
                boolean reached = boneCol <= bone.getTargetCol();
                if (bone.isDestroyed() || reached) {
                    Tile tile = map.getTile((int) row, bone.getTargetCol());
                    if (tile != null && tile.getType() == TileType.NORMAL && tile.getPlant() == null
                        && canRaiseTomb()) {
                        tile.setType(TileType.TOMBSTONE);
                        tile.setHp(700);
                        raiseTomb();
                        System.out.println("[ZombieTombRaiser] Tomb Raiser Zombie raised a Tombstone (700 HP) at tile (" + (int) row + ", " + bone.getTargetCol() + ")!");
                    }
                    bone.destroy();
                    if (controller != null) controller.removeZombieProjectile(bone);
                    bones.remove(i);
                }
            }
        }

        if (map == null || !canRaiseTomb()) {
            return;
        }
        throwCooldownTimer += delta;
        if (throwCooldownTimer < BONE_THROW_INTERVAL_SECONDS) {
            return;
        }
        throwCooldownTimer = 0f;
        throwBones(controller);
    }

    private void throwBones(BattleController controller) {
        int frontCol = (int) col;
        List<Integer> candidates = new ArrayList<>();
        for (int dc = 1; dc <= frontCol; dc++) {
            int c = frontCol - dc;
            Tile t = map.getTile((int) row, c);
            if (t != null && t.getType() == TileType.NORMAL && t.getPlant() == null) {
                candidates.add(c);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        Collections.shuffle(candidates, random);
        int n = Math.min(3, candidates.size());
        if (n > 0) {
            com.PVZ.model.entity.zombies.base.ZombieAnimation.trigger(this, "power", 3.0);
            System.out.println("[ZombieTombRaiser] Tomb Raiser Zombie performing power summoning animation!");
        }
        for (int i = 0; i < n; i++) {
            int targetCol = candidates.get(i);
            ZombieProjectile bone = new ZombieProjectile(
                (float) x, (float) y, 0, (float) projectileSpeed, (int) row, this, targetCol, true);
            bones.add(bone);
            if (controller != null) {
                controller.addZombieProjectile(bone);
            }
        }
    }

    @Override
    public void onHit(Plant target) {
    }

    @Override
    public void maybeSpawnGraves(Map map, Random random) {
        this.map = map;
    }

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nTOMBS:" + tombsRaised + "/" + maxTombs;
    }

    public boolean canRaiseTomb() {
        return tombsRaised < maxTombs;
    }

    public void raiseTomb() {
        if (canRaiseTomb()) tombsRaised++;
    }
}
