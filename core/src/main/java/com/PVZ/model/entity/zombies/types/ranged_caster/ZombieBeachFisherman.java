package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.BattleController;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class ZombieBeachFisherman extends AbstractRangedCasterZombie {

    public ZombieBeachFisherman() {
        super("ZombieBeachFisherman", 400, 100, 0.185, 600, 3500, defaultScaledProps(),
              0, 300, 4.0, 9);
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
    public void onSpawn() {
        super.onSpawn();
        this.rangedCooldown = 2.0f;
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
        Plant targetPlant = findFarthestPlantInLane(ctrl);

        boolean inDeepWater = false;
        boolean nextIsDeepWater = false;

        if (ctrl.getMap() != null) {
            float gridRight = ctrl.getMap().getStartX() + ctrl.getMap().getTotalWidth();
            if (x >= gridRight - 15f) {
                inDeepWater = true;
                nextIsDeepWater = true;
            } else if (tileCol >= 0 && tileCol < ctrl.getMap().getCols()) {
                Tile currentTile = ctrl.getMap().getTile((int) row, tileCol);
                TileType ctType = currentTile != null ? currentTile.getType() : TileType.NORMAL;
                inDeepWater = (ctType == TileType.WATER || ctType == TileType.TIDE);

                int nextCol = tileCol - 1;
                if (nextCol >= 0) {
                    TileType ntType = ctrl.getTileTypeAt((int) row, nextCol);
                    nextIsDeepWater = (ntType == TileType.WATER || ntType == TileType.TIDE);
                }
            }
        }

        boolean canMove = false;
        if (inDeepWater) {
            if (nextIsDeepWater) {
                canMove = true;
            } else {
                Tile currentTile = (ctrl.getMap() != null && tileCol >= 0 && tileCol < ctrl.getMap().getCols()) ? ctrl.getMap().getTile((int) row, tileCol) : null;
                // Never enter LOW_COAST or NORMAL! Stop at right side of the current water tile.
                if (currentTile != null && x > currentTile.getX() + currentTile.getWidth() * 0.5f) {
                    canMove = true;
                }
            }
        }

        if (plantInFront != null && !plantInFront.isDead()) {
            moving = false;
            attack(plantInFront, delta, ctrl);
        } else if (canMove) {
            moving = true;
            move(delta, ctrl);
            if (targetPlant != null) {
                rangedCooldown += delta;
                if (rangedCooldown >= attackCooldown) {
                    shoot(ctrl, targetPlant);
                    rangedCooldown = 0;
                }
            }
        } else {
            moving = false;
            if (targetPlant != null) {
                rangedCooldown += delta;
                if (rangedCooldown >= attackCooldown) {
                    shoot(ctrl, targetPlant);
                    rangedCooldown = 0;
                }
            }
        }

        hitbox.setPosition((float) x, (float) y);
        onUpdate(delta, ctrl);
    }

    private Plant findFarthestPlantInLane(BattleController ctrl) {
        if (ctrl == null) return null;
        for (int c = 0; c < 9; c++) {
            Plant p = ctrl.getPlantAt((int) row, c);
            if (p != null && !p.isDead()) return p;
        }
        return null;
    }

    @Override
    public void shoot(BattleController controller, Plant target) {
        if (target != null && !target.isDead() && controller != null && controller.getMap() != null) {
            ZombieAnimation.trigger(this, "cast", 1.2667);

            // Hook impact damage
            target.takeDamage(100, this, controller);

            // Find current tile row and col of the plant in map
            int plantRow = (int) this.row;
            int plantCol = -1;
            for (int c = 0; c < 9; c++) {
                if (controller.getPlantAt(plantRow, c) == target) {
                    plantCol = c;
                    break;
                }
            }
            if (plantCol == -1) {
                for (int r = 0; r < 5; r++) {
                    for (int c = 0; c < 9; c++) {
                        if (controller.getPlantAt(r, c) == target) {
                            plantRow = r;
                            plantCol = c;
                            break;
                        }
                    }
                }
            }
            if (plantCol == -1) return;

            if (target.isDead()) {
                controller.removePlant(plantRow, plantCol);
                System.out.println("[Fisherman] Hook strike killed plant at (" + plantRow + ", " + plantCol + ")");
                return;
            }

            int newCol = plantCol + 1;
            TileType newTileType = controller.getTileTypeAt(plantRow, newCol);
            boolean pulledIntoWater = newCol >= 8 || newTileType == TileType.WATER || newTileType == TileType.TIDE;

            if (pulledIntoWater) {
                ZombieAnimation.trigger(this, "toss", 2.4333);
                target.takeDamage(99999, this, controller);
                controller.removePlant(plantRow, plantCol);
                System.out.println("[Fisherman] Yanked and drowned plant at (" + plantRow + ", " + plantCol + ") into the sea!");
            } else {
                ZombieAnimation.trigger(this, "reel", 1.4667);
                if (controller.getPlantAt(plantRow, newCol) == null) {
                    controller.getMap().removePlant(plantRow, plantCol);
                    controller.getMap().setPlant(plantRow, newCol, target);
                    System.out.println("[Fisherman] Hooked plant from col " + plantCol + " to col " + newCol);
                } else {
                    target.takeDamage(99999, this, controller);
                    controller.removePlant(plantRow, plantCol);
                    System.out.println("[Fisherman] Hooked plant collided with occupied tile — crushed!");
                }
            }
        }
    }

    @Override
    public void onHit(Plant target) {}

    @Override
    public String getDebugString() {
        return super.getDebugString() + "\nFISHER";
    }
}
