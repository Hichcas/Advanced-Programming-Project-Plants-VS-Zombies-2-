package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombieBeachOctopus extends AbstractRangedCasterZombie {

    private final Random random = new Random();

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

        if (plantInFront != null && !plantInFront.isDead()) {
            moving = false;
            attack(plantInFront, delta, ctrl);
        } else {
            moving = true;
            move(delta, ctrl);
        }

        // Ranged Octopus toss cooldown check across ALL rows and columns
        rangedCooldown += delta;
        if (rangedCooldown >= attackCooldown) {
            Plant target = findRandomValidPlant(ctrl);
            if (target != null) {
                shoot(ctrl, target);
                rangedCooldown = 0;
            }
        }

        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta, ctrl);
    }

    private Plant findRandomValidPlant(BattleController ctrl) {
        if (ctrl == null || ctrl.getMap() == null) return null;
        List<Plant> candidates = new ArrayList<>();
        com.PVZ.model.game.Map map = ctrl.getMap();
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getCols(); c++) {
                Tile tile = map.getTile(r, c);
                if (tile != null && tile.getOctopusHp() <= 0) {
                    Plant p = tile.getPlant();
                    if (p != null && !p.isDead()) {
                        p.putRuntimeState("row", r);
                        p.putRuntimeState("col", c);
                        candidates.add(p);
                    }
                }
            }
        }
        if (candidates.isEmpty()) return null;
        return candidates.get(random.nextInt(candidates.size()));
    }

    @Override
    public void shoot(BattleController controller, Plant target) {
        if (target == null || target.isDead() || controller == null || controller.getMap() == null) {
            return;
        }
        ZombieAnimation.trigger(this, "toss", 1.25);

        Object r = target.getRuntimeState("row");
        Object c = target.getRuntimeState("col");
        int targetRow = r instanceof Number ? ((Number) r).intValue() : (int) this.row;
        int targetCol = c instanceof Number ? ((Number) c).intValue() : (int) this.col;
        Tile targetTile = controller.getMap().getTile(targetRow, targetCol);

        if (targetTile != null) {
            // Launch coordinate: 283 pixels left (-283) and 247 pixels up (+247) relative to zombie origin
            float spawnX = (float) this.x - 283f;
            float spawnY = (float) this.y + 247f;

            float targetX = targetTile.getX() + targetTile.getWidth() / 2f;
            float targetY = targetTile.getY() + targetTile.getHeight() / 2f;

//            OctopusProjectile proj = new OctopusProjectile(
//                spawnX, spawnY, targetX, targetY, targetRow, this, target, targetTile
//            );
         //   controller.addZombieProjectile(proj);
            System.out.println(alias + " tossed octopus projectile from (" + spawnX + ", " + spawnY + ") -> plant at (" + targetCol + ", " + targetRow + ")");
        }
    }

    @Override
    public void onHit(Plant target) {
    }
}
