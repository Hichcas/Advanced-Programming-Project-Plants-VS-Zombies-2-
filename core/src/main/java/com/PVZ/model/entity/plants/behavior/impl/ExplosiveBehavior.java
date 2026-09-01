package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.PlantAnimation;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;
import java.util.Set;

public class ExplosiveBehavior implements PlantBehavior {

    private static final Set<String> CONTACT_TRIGGERED = Set.of(
        "potato_mine", "primal_potato_mine", "tangle_kelp", "iceberg_lettuce", "squash");

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (!isArmed(plant, deltaTime)) {
            return;
        }

        String key = getPlantKey(plant);
        int lane = getLane(plant);
        int row = getRow(plant);

        if (requiresContactAndNoContact(plant, context, key, lane)) {
            return;
        }

        dispatchByPlantKey(plant, context, deltaTime, key, lane, row);
    }

    private boolean isArmed(PlantInstance plant, double deltaTime) {
        Object armedObj = plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE);
        if (!(armedObj instanceof Boolean armed) || !armed) {
            double armTimer = asDouble(plant.getRuntimeState().getOrDefault("armTimer", 0.0), 0.0);
            armTimer += deltaTime;
            double armTime = plant.getStats().getArmTimeSeconds();
            if (armTimer >= Math.max(armTime, 0.0)) {
                plant.putRuntimeState("armed", Boolean.TRUE);
                armTimer = 0.0;
            }
            plant.putRuntimeState("armTimer", armTimer);
            return false;
        }
        return true;
    }

    private String getPlantKey(PlantInstance plant) {
        return plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
    }

    private int getLane(PlantInstance plant) {
        return asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
    }

    private int getRow(PlantInstance plant) {
        return asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
    }

    private boolean requiresContactAndNoContact(PlantInstance plant, BehaviorContext context,
                                                String key, int lane) {
        if (!CONTACT_TRIGGERED.contains(key)) {
            return false;
        }

        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            return true;
        }

        if ("potato_mine".equals(key) || "primal_potato_mine".equals(key)) {
            int plantCol = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            for (Zombie zombie : zombies) {
                if (zombie != null && !zombie.isDead()) {
                    int zCol = mapColOf(context, zombie);
                    if (Math.abs(zCol - plantCol) <= 1) {
                        return false;
                    }
                }
            }
            return true;
        }

        return false;
    }

    private void dispatchByPlantKey(PlantInstance plant, BehaviorContext context,
                                    double deltaTime, String key, int lane, int row) {
        if ("ice_shroom".equals(key)) {
            handleIceShroom(plant, context, lane, row);
            return;
        }
        if ("doom_shroom".equals(key)) {
            handleDoomShroom(plant, context, deltaTime, lane, row);
            return;
        }
        if ("grave_buster".equals(key)) {
            handleGraveBuster(plant, context, deltaTime, lane, row);
            return;
        }
        if ("jalapeno".equals(key)) {
            handleJalapeno(plant, context, lane);
            return;
        }
        if ("squash".equals(key)) {
            handleSquash(plant, context, deltaTime, lane, row);
            return;
        }
        if ("iceberg_lettuce".equals(key)) {
            handleIcebergLettuce(plant, context, lane);
            return;
        }
        if ("tangle_kelp".equals(key)) {
            handleTangleKelp(plant, context, lane);
            return;
        }
        if ("grapshot".equals(key) || "grapeshot".equals(key)) {
            handleGrapeshot(plant, context, lane, row);
            return;
        }

        int damage = calculateDamage(plant);
        context.damageArea(lane, row, damage);
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 0.6667);
        plant.takeDamage(plant.getCurrentHp());
    }

    private int calculateDamage(PlantInstance plant) {
        return plant.getStats().getExplodeDamage() > 0
            ? plant.getStats().getExplodeDamage()
            : Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage());
    }

    private void handleIceShroom(PlantInstance plant, BehaviorContext context,
                                 int lane, int row) {
        if (Boolean.TRUE.equals(plant.getRuntimeState().get("iceTriggered"))) {
            return;
        }
        double freezeSeconds = Math.max(8.0, plant.getStats().getFreezeTimeSeconds());
        context.freezeAllZombies(freezeSeconds);
        if (context instanceof com.PVZ.model.game.RegularGameEngine rge) {
            int iceCol = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            rge.setGlobalIceEffect(freezeSeconds);
            rge.setIceShroomTileEffect(row, iceCol, freezeSeconds);
            rge.addTimedPamEffect(
                "768/FULL/EFFECTS/ICESHROOM_FX/ICESHROOM_FX.PAM",
                "animation", freezeSeconds, 1.35f,
                rge.getMapCenterX(), rge.getMapCenterY());
        }
        plant.putRuntimeState("iceTriggered", Boolean.TRUE);
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 1.1667);
        plant.takeDamage(plant.getCurrentHp());
    }

    private void handleDoomShroom(PlantInstance plant, BehaviorContext context,
                                  double deltaTime, int lane, int row) {
        if (!Boolean.TRUE.equals(plant.getRuntimeState().get("doomTriggered"))) {
            plant.putRuntimeState("doomTriggered", Boolean.TRUE);
            plant.putRuntimeState("doomTimer", 0.0);
            int globalDamage = Math.max(calculateDamage(plant), 1800);
            for (Zombie zombie : context.getAllZombies()) {
                if (zombie != null && !zombie.isDead()) {
                    zombie.takeDamage(globalDamage);
                }
            }
            int craterCol = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            if (context instanceof com.PVZ.model.game.RegularGameEngine rge
                && rge.map != null) {
                com.PVZ.model.entity.Tile craterTile = rge.map.getTile(row, craterCol);
                if (craterTile != null) {
                    craterTile.setType(com.PVZ.model.enums.TileType.CRATER);
                    craterTile.setHp(0);
                    float[] center = rge.getPlantWorldCenter(row, craterCol);
                    rge.addTimedPamEffect(
                        "768/FULL/EFFECTS/CRATER/CRATER.PAM",
                        "animation", 2.5, 1.8f,
                        craterTile.getX() + craterTile.getWidth() / 2f,
                        craterTile.getY() + craterTile.getHeight() / 2f);
                    rge.addTimedPamEffect(
                        "768/FULL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_FUTURE/"
                            + "ZOMBOSS_MISSILE_EXPLOSION_FUTURE.PAM",
                        "missile_explosion", 1.7, 1.35f,
                        craterTile.getX() + craterTile.getWidth() / 2f,
                        craterTile.getY() + craterTile.getHeight() / 2f);
                    rge.addTimedPamEffect(
                        "768/FULL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_FUTURE/"
                            + "ZOMBOSS_MISSILE_EXPLOSION_FUTURE.PAM",
                        "missile_explosion", 1.7, 1.75f,
                        center[0], center[1]);
                }
            }
            PlantAnimation.trigger(plant, "stage3_explode", 3.3333);
        }

        double timer = asDouble(plant.getRuntimeState().getOrDefault("doomTimer", 0.0), 0.0)
            + deltaTime;
        if (timer >= 2.0) {
            context.removePlant(row, asInt(plant.getRuntimeState().getOrDefault("col", 0), 0));
        } else {
            plant.putRuntimeState("doomTimer", timer);
        }
    }

    private void handleGraveBuster(PlantInstance plant, BehaviorContext context,
                                   double deltaTime, int lane, int row) {
        int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        if (context instanceof com.PVZ.model.game.RegularGameEngine rge
            && rge.map != null) {
            com.PVZ.model.entity.Tile tile = rge.map.getTile(row, col);
            if (tile == null) {context.removePlant(row, col);return;}
            if (tile.getType() != com.PVZ.model.enums.TileType.TOMBSTONE
                && tile.getType() != com.PVZ.model.enums.TileType.NECROMANCY) {
                context.removePlant(row, col);return;}
            double eatDuration = 4.5;
            Object reduction = plant.getStats().getExtra("eatTimeReduction");
            if (reduction != null) {
                eatDuration = Math.max(1.5, 4.5 - asDouble(reduction, 1.0));}
            double eatTimer = asDouble(plant.getRuntimeState().getOrDefault("eatTimer", 0.0), 0.0)
                + deltaTime;plant.putRuntimeState("eatTimer", eatTimer);
            PlantAnimation.trigger(plant, "attack", 1.0);
            int startHp = asInt(plant.getRuntimeState().getOrDefault("initialGraveHp", 700), 700);
            plant.putRuntimeState("initialGraveHp", startHp);
            double progress = Math.min(1.0, eatTimer / eatDuration);
            int remainingHp = (int) (startHp * (1.0 - progress));
            tile.setHp(Math.max(1, remainingHp));if (eatTimer >= eatDuration) {
                com.PVZ.model.enums.GraveVariant variant = tile.getGraveVariant();
                tile.setType(com.PVZ.model.enums.TileType.NORMAL);tile.setHp(0);
                float[] center = rge.getPlantWorldCenter(row, col);
                String fxPam = (variant != null && variant.getDamageFxPamPath() != null)
                    ? variant.getDamageFxPamPath()
                    : "768/INITIAL/EFFECTS/TOMBSTONE_EGYPT_HIEROGLYPH_DAMAGE/"
                      + "TOMBSTONE_EGYPT_HIEROGLYPH_DAMAGE.PAM";
                rge.addTimedPamEffect(fxPam, "animation", 1.0, 1.0f, center[0], center[1]);
                if (variant == com.PVZ.model.enums.GraveVariant.DARK_SUN) {
                    rge.addSun(100);rge.spawnSunAt(row, col, 100);
                } else if (variant == com.PVZ.model.enums.GraveVariant.DARK_PLANTFOOD) {
                    if (rge.getPlantFoodManager() != null) {
                        rge.getPlantFoodManager().addPlantFood(1);}
                    if (rge.getLootManager() != null) {
                        rge.getLootManager().spawnLootDrop(
                            center[0], center[1],
                            com.PVZ.model.entity.LootDrop.LootType.PLANT_FOOD);}}
                boolean explodeOnFinish = plant.getStats() != null
                    && plant.getStats().getBooleanExtra("explodeOnFinish", false);
                if (explodeOnFinish) {
                    context.damageArea(lane, row, 500);
                    rge.addTimedPamEffect(
                        "768/INITIAL/EFFECTS/GRAVEBUSTER_EXPLOSION_POTATOMINE/"
                            + "GRAVEBUSTER_EXPLOSION_POTATOMINE.PAM",
                        "animation", 1.1667, 1.2f, center[0], center[1]);}
                context.removePlant(row, col);}} else {context.removePlant(row, col);}}

    private void handleJalapeno(PlantInstance plant, BehaviorContext context, int lane) {
        int laneDamage = Math.max(calculateDamage(plant), 5000);
        context.damageEntireLane(lane, laneDamage);
        if (plant.getStats().getBooleanExtra("meltsIce", false)) {
            context.meltIceInLane(lane);
        }
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 0.6667);
        if (context instanceof com.PVZ.model.game.RegularGameEngine rge) {
            rge.triggerJalapenoLaneEffect(lane, 2.0);
        }
        plant.takeDamage(plant.getCurrentHp());
    }

    private void handleSquash(PlantInstance plant, BehaviorContext context,
                              double deltaTime, int lane, int row) {
        String squashState = (String) plant.getRuntimeState().getOrDefault("squashState", "idle");
        if ("idle".equals(squashState)) {
            int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            List<Zombie> candidates = new java.util.ArrayList<>(context.getZombiesInLane(lane));
            candidates.removeIf(z -> z == null || z.isDead());
            candidates.removeIf(z -> {
                int zCol = mapColOf(context, z);
                return Math.abs(zCol - col) > 1;
            });
            if (candidates.isEmpty()) {
                return;
            }
            plant.putRuntimeState("squashState", "leaping");
            plant.putRuntimeState("squashTimer", 0.0);
            return;
        }

        double jumpTimer = asDouble(plant.getRuntimeState().getOrDefault("squashTimer", 0.0), 0.0)
            + deltaTime;
        plant.putRuntimeState("squashTimer", jumpTimer);
        if (jumpTimer < 0.4) {
            return;
        }

        List<Zombie> targets = context.getZombiesInLane(lane);
        if (!targets.isEmpty()) {
            context.damageSingleTarget(targets.get(0), calculateDamage(plant));
        }

        boolean canCrushTwice = plant.getStats().getBooleanExtra("canCrush2x", false);
        int crushesDone = asInt(plant.getRuntimeState().getOrDefault("squashCrushes", 0), 0) + 1;
        if (canCrushTwice && crushesDone < 2) {
            plant.putRuntimeState("squashCrushes", crushesDone);
            plant.putRuntimeState("squashState", "idle");
            plant.putRuntimeState("squashTimer", 0.0);
            return;
        }

        com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", 0.4);
        plant.takeDamage(plant.getCurrentHp());
    }

    private void handleIcebergLettuce(PlantInstance plant, BehaviorContext context, int lane) {
        double freezeSeconds = Math.max(3.0, plant.getStats().getFreezeTimeSeconds());
        if (context instanceof com.PVZ.model.game.BattleController bc) {
            bc.freezeClosestZombieInLane(lane, freezeSeconds);
        } else {
            context.freezeZombiesInLane(lane, freezeSeconds);
        }
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", 0.4);
        plant.takeDamage(plant.getCurrentHp());
    }

    private void handleTangleKelp(PlantInstance plant, BehaviorContext context, int lane) {
        context.killClosestZombieInLane(lane);
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", 0.4);
        plant.takeDamage(plant.getCurrentHp());
    }

    private void handleGrapeshot(PlantInstance plant, BehaviorContext context,
                                 int lane, int row) {
        int plantCol = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        context.damageAreaAt(row, plantCol, calculateDamage(plant), 1);
        int grapeCount = plant.getStats().getIntExtra("grapeCount", 8);
        double grapeLifespan = plant.getStats().getDoubleExtra("grapeLifespanSeconds", 5.0);
        context.spawnBouncingProjectilesFrom(
            row, plantCol, grapeCount,
            Math.max(1, calculateDamage(plant) / 4),
            grapeLifespan);
        PlantAnimation.trigger(plant, "shooting", 0.9);
        plant.takeDamage(plant.getCurrentHp());
    }

    private static int asInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static double asDouble(Object value, double defaultValue) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null ? defaultValue : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static int mapColOf(BehaviorContext context, Zombie z) {
        if (context instanceof com.PVZ.model.game.BattleController bc) {
            return bc.getTileColumn((float) z.getX());
        }
        if (context instanceof com.PVZ.model.game.RegularGameEngine rge) {
            return rge.getTileColumn((float) z.getX());
        }
        return 0;
    }
}
