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
        Boolean armed = (Boolean) plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE);
        if (armed == null || !armed) {
            Double armTimer = (Double) plant.getRuntimeState().getOrDefault("armTimer", 0.0);
            armTimer += deltaTime;
            double armTime = plant.getStats().getArmTimeSeconds();
            if (armTimer >= Math.max(armTime, 0.0)) {
                plant.putRuntimeState("armed", Boolean.TRUE);
                armTimer = 0.0;
            }
            plant.putRuntimeState("armTimer", armTimer);
            if (!Boolean.TRUE.equals(plant.getRuntimeState().getOrDefault("armed", Boolean.FALSE))) return;
        }
        Integer lane = (Integer) plant.getRuntimeState().getOrDefault("lane", 0);
        Integer row = (Integer) plant.getRuntimeState().getOrDefault("row", 0);
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        boolean requiresContact = key != null && CONTACT_TRIGGERED.contains(key);
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (requiresContact) {
            if (zombies.isEmpty()) return;
            int plantCol = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            if ("potato_mine".equals(key) || "primal_potato_mine".equals(key)) {
                boolean contact = false;
                for (Zombie zombie : zombies) {
                    if (zombie != null && !zombie.isDead()) {
                        int zCol = mapColOf(context, zombie);
                        if (Math.abs(zCol - plantCol) <= 1) {
                            contact = true;
                            break;
                        }
                    }
                }
                if (!contact) return;
            }
        }
        if ("ice_shroom".equals(key)) {
            if (Boolean.TRUE.equals(plant.getRuntimeState().get("iceTriggered"))) {
                return;
            }
            double freezeSeconds = Math.max(8.0, plant.getStats().getFreezeTimeSeconds());
            context.freezeAllZombies(freezeSeconds);
            if (context instanceof com.PVZ.model.game.RegularGameEngine rge) {
                int iceCol = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
                rge.setGlobalIceEffect(freezeSeconds);
                rge.setIceShroomTileEffect(asInt(plant.getRuntimeState().getOrDefault("row", 0), 0), iceCol, freezeSeconds);
                rge.addTimedPamEffect("768/FULL/EFFECTS/ICESHROOM_FX/ICESHROOM_FX.PAM", "animation",
                    freezeSeconds, 1.35f, rge.getMapCenterX(), rge.getMapCenterY());
            }
            plant.putRuntimeState("iceTriggered", Boolean.TRUE);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 1.1667);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        int damage = plant.getStats().getExplodeDamage() > 0 ? plant.getStats().getExplodeDamage()
            : Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage());

        if ("doom_shroom".equals(key)) {
            if (!Boolean.TRUE.equals(plant.getRuntimeState().get("doomTriggered"))) {
                plant.putRuntimeState("doomTriggered", Boolean.TRUE);
                plant.putRuntimeState("doomTimer", 0.0);
                int globalDamage = Math.max(damage, 1800);
                for (Zombie zombie : context.getAllZombies()) {
                    if (zombie != null && !zombie.isDead()) zombie.takeDamage(globalDamage);
                }
                int craterRow = row;
                int craterCol = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
                if (context instanceof com.PVZ.model.game.RegularGameEngine rge && rge.map != null) {
                    com.PVZ.model.entity.Tile craterTile = rge.map.getTile(craterRow, craterCol);
                    if (craterTile != null) {
                        craterTile.setType(com.PVZ.model.enums.TileType.CRATER);
                        craterTile.setHp(0);
                        rge.addTimedPamEffect("768/FULL/EFFECTS/CRATER/CRATER.PAM", "animation",
                            2.5, 1.8f, craterTile.getX() + craterTile.getWidth()/2f,
                            craterTile.getY() + craterTile.getHeight()/2f);
                        rge.addTimedPamEffect("768/FULL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_FUTURE/ZOMBOSS_MISSILE_EXPLOSION_FUTURE.PAM",
                            "missile_explosion", 1.7, 1.35f,
                            craterTile.getX() + craterTile.getWidth()/2f,
                            craterTile.getY() + craterTile.getHeight()/2f);
                    }
                }
                PlantAnimation.trigger(plant, "stage3_explode", 3.3333);
                if (context instanceof com.PVZ.model.game.RegularGameEngine rge) {
                    float[] center = rge.getPlantWorldCenter(row, craterCol);
                    rge.addTimedPamEffect("768/FULL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_FUTURE/ZOMBOSS_MISSILE_EXPLOSION_FUTURE.PAM",
                        "missile_explosion", 1.7, 1.75f, center[0], center[1]);
                }
            }
            double timer = asDouble(plant.getRuntimeState().getOrDefault("doomTimer", 0.0), 0.0) + deltaTime;
            if (timer >= 2.0) {
                context.removePlant(row, asInt(plant.getRuntimeState().getOrDefault("col", 0), 0));
            } else {
                plant.putRuntimeState("doomTimer", timer);
            }
            return;
        }

        if ("grave_buster".equals(key)) {
            int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            if (context instanceof com.PVZ.model.game.RegularGameEngine rge && rge.map != null) {
                com.PVZ.model.entity.Tile tile = rge.map.getTile(row, col);
                if (tile != null && (tile.getType() == com.PVZ.model.enums.TileType.TOMBSTONE
                    || tile.getType() == com.PVZ.model.enums.TileType.NECROMANCY)) {

                    double eatDuration = 4.5;
                    if (plant.getStats() != null && plant.getStats().getExtra("eatTimeReduction") != null) {
                        eatDuration = Math.max(1.5, 4.5 - asDouble(plant.getStats().getExtra("eatTimeReduction"), 1.0));
                    }

                    double eatTimer = asDouble(plant.getRuntimeState().getOrDefault("eatTimer", 0.0), 0.0) + deltaTime;
                    plant.putRuntimeState("eatTimer", eatTimer);

                    PlantAnimation.trigger(plant, "attack", 1.0);

                    int startHp = asInt(plant.getRuntimeState().getOrDefault("initialGraveHp", tile.getMaxHp() > 0 ? tile.getMaxHp() : 700), 700);
                    plant.putRuntimeState("initialGraveHp", startHp);
                    double progress = Math.min(1.0, eatTimer / eatDuration);
                    int remainingHp = (int) (startHp * (1.0 - progress));
                    tile.setHp(Math.max(1, remainingHp));

                    if (eatTimer >= eatDuration) {
                        com.PVZ.model.enums.GraveVariant variant = tile.getGraveVariant();
                        tile.setType(com.PVZ.model.enums.TileType.NORMAL);
                        tile.setHp(0);

                        float[] center = rge.getPlantWorldCenter(row, col);
                        String fxPam = (variant != null && variant.getDamageFxPamPath() != null)
                            ? variant.getDamageFxPamPath()
                            : "768/INITIAL/EFFECTS/TOMBSTONE_EGYPT_HIEROGLYPH_DAMAGE/TOMBSTONE_EGYPT_HIEROGLYPH_DAMAGE.PAM";
                        rge.addTimedPamEffect(fxPam, "animation", 1.0, 1.0f, center[0], center[1]);

                        if (variant == com.PVZ.model.enums.GraveVariant.DARK_SUN) {
                            rge.addSun(100);
                        } else if (variant == com.PVZ.model.enums.GraveVariant.DARK_PLANTFOOD) {
                            rge.getLootManager().spawnLootDrop(center[0], center[1], com.PVZ.model.entity.LootDrop.LootType.PLANT_FOOD);
                        }

                        boolean explodeOnFinish = plant.getStats() != null && plant.getStats().getBooleanExtra("explodeOnFinish", false);
                        if (explodeOnFinish) {
                            context.damageArea(lane, row, 500);
                            rge.addTimedPamEffect(
                                "768/INITIAL/EFFECTS/GRAVEBUSTER_EXPLOSION_POTATOMINE/GRAVEBUSTER_EXPLOSION_POTATOMINE.PAM",
                                "animation", 1.1667, 1.2f, center[0], center[1]);
                        }

                        context.removePlant(row, col);
                    }
                } else {
                    context.removePlant(row, col);
                }
            }
            return;
        }
        if ("jalapeno".equals(key)) {
            // Lane-clear is intentionally very high damage: it is an instant wipe of the lane.
            int laneDamage = Math.max(damage, 5000);
            context.damageEntireLane(lane, laneDamage);
            if (plant.getStats().getBooleanExtra("meltsIce", false))
                context.meltIceInLane(lane);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 0.6667);
            if (context instanceof com.PVZ.model.game.RegularGameEngine rge) {
                rge.triggerJalapenoLaneEffect(lane, 2.0);
            }
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        if ("squash".equals(key)) {
            String squashState = (String) plant.getRuntimeState().getOrDefault("squashState",
                "idle");
            if ("idle".equals(squashState)) {
                int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
                List<Zombie> candidates = new java.util.ArrayList<>(context.getZombiesInLane(lane));
                candidates.removeIf(z -> z == null || z.isDead());
                candidates.removeIf(z -> {
                    int zCol = mapColOf(context, z);
                    return java.lang.Math.abs(zCol - col) > 1;
                });
                if (candidates.isEmpty()) return;
                plant.putRuntimeState("squashState", "leaping");
                plant.putRuntimeState("squashTimer", 0.0);
                return;
            }
            double jumpTimer = asDouble(plant.getRuntimeState().getOrDefault("squashTimer", 0.0), 0.0);
            jumpTimer += deltaTime;
            plant.putRuntimeState("squashTimer", jumpTimer);
            if (jumpTimer < 0.4) return;
            List<Zombie> targets = context.getZombiesInLane(lane);
            if (!targets.isEmpty()) {
                Zombie nearest = targets.get(0);
                context.damageSingleTarget(nearest, damage);
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
            return;
        }

        if ("iceberg_lettuce".equals(key)) {
            // Real ability: freezes only the first zombie that steps on it — not the whole lane.
            if (context instanceof com.PVZ.model.game.BattleController bc) {
                bc.freezeClosestZombieInLane(lane, Math.max(3.0, plant.getStats().getFreezeTimeSeconds()));
            } else {
                context.freezeZombiesInLane(lane, Math.max(3.0, plant.getStats().getFreezeTimeSeconds()));
            }
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", 0.4);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        if ("tangle_kelp".equals(key)) {
            context.killClosestZombieInLane(lane);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", 0.4);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        if ("grapshot".equals(key) || "grapeshot".equals(key)) {
            int plantCol = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            context.damageAreaAt(row, plantCol, damage, 1);
            int grapeCount = plant.getStats().getIntExtra("grapeCount", 8);
            double grapeLifespan = plant.getStats().getDoubleExtra("grapeLifespanSeconds", 5.0);
            context.spawnBouncingProjectilesFrom(row, plantCol, grapeCount, Math.max(1, damage / 4), grapeLifespan);
            PlantAnimation.trigger(plant, "shooting", 0.9);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", 0.4);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        context.damageArea(lane, row, damage);
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 0.6667);
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

    private static int mapColOf(BehaviorContext context, com.PVZ.model.entity.zombies.base.Zombie z) {
        if (context instanceof com.PVZ.model.game.BattleController bc) {
            return bc.getTileColumn((float) z.getX());
        }
        if (context instanceof com.PVZ.model.game.RegularGameEngine rge) {
            return rge.getTileColumn((float) z.getX());
        }
        return 0;
    }
}
