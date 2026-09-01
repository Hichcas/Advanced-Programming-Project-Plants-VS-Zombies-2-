package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.Map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ZombieTombRaiser extends Zombie {
    private int maxTombs;
    private int tombsRaised;
    private float throwCooldownTimer;
    private static final float INITIAL_DELAY_SECONDS = 5.0f;
    private static final float THROW_INTERVAL_SECONDS = 10.0f;
    private final Random random = new Random();
    private Map map;
    private final List<ZombieProjectile> bones = new ArrayList<>();

    private boolean isCastingPower = false;
    private float castTimer = 0.0f;
    private int pendingTargetCol = -1;

    public ZombieTombRaiser() {
        super("ZombieTombRaiser", 320, 100, 0.185, 700, 3500, defaultScaledProps());
        this.maxTombs = 3;
        this.tombsRaised = 0;
        this.throwCooldownTimer = THROW_INTERVAL_SECONDS - INITIAL_DELAY_SECONDS;
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
    public void onSpawn() { }

    @Override
    public void onDestroy() { }

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
                    if (tile != null && tile.getType() == TileType.NORMAL
                        && tile.getPlant() == null && canRaiseTomb()) {
                        tile.setType(TileType.TOMBSTONE);
                        tile.setHp(700);tile.setMaxHp(700);
                        tile.setGraveVariant(com.PVZ.model.enums.GraveVariant.EGYPT);raiseTomb();
                        if (controller != null && controller.getEngine() != null) {
                            float[] center = controller.getEngine().getPlantWorldCenter((int) row, bone.getTargetCol());
                            controller.getEngine().addTimedPamEffect(
                                "768/INITIAL/EFFECTS/ZOMBIE_EGYPT_TOMBRAISER_BONE_HIT"+
                                    "/ZOMBIE_EGYPT_TOMBRAISER_BONE_HIT.PAM",
                                "animation", 1.3333, 1.0f, center[0], center[1]);}
                        System.out.println("[ZombieTombRaiser] Bone landed! Raised Tombstone (700 HP) at tile (" +
                            (int) row + ", " + bone.getTargetCol() + ")!");}bone.destroy();
                    if (controller != null) controller.removeZombieProjectile(bone);
                    bones.remove(i);}}}
        if (map == null || !canRaiseTomb()) {return;}
        if (isCastingPower) {castTimer += delta;
            if (castTimer >= 0.8f && pendingTargetCol != -1) {
                ZombieProjectile bone = new ZombieProjectile(
                    (float) x, (float) y, 0, 180f, (int) row, this, pendingTargetCol, true);
                bones.add(bone);
                if (controller != null) {controller.addZombieProjectile(bone);}
                System.out.println("[ZombieTombRaiser] Bone projectile released from hands towards column "
                    + pendingTargetCol + "!");pendingTargetCol = -1;}
            if (castTimer >= 3.0f) {
                isCastingPower = false;castTimer = 0.0f;}return;}
        throwCooldownTimer += delta;
        if (throwCooldownTimer >= THROW_INTERVAL_SECONDS) {
            throwCooldownTimer = 0f;
            prepareBoneThrow(controller);}
    }

    private void prepareBoneThrow(BattleController controller) {
        int currentColumn = controller != null ? controller.getTileColumn((float) x) : (int) col;
        List<Integer> candidates = new ArrayList<>();
        for (int dc = 1; dc <= currentColumn; dc++) {
            int c = currentColumn - dc;
            Tile t = map.getTile((int) row, c);
            if (t != null && t.getType() == TileType.NORMAL && t.getPlant() == null) {
                candidates.add(c);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }
        Collections.shuffle(candidates, random);
        pendingTargetCol = candidates.get(0);
        isCastingPower = true;
        castTimer = 0.0f;
        com.PVZ.model.entity.zombies.base.ZombieAnimation.trigger(this, "power", 3.0);
        System.out.println("[ZombieTombRaiser] Started power animation (3.0s)! Target column: " + pendingTargetCol);
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
