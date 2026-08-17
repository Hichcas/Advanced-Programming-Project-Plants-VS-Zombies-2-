package com.PVZ.model.entity.plants.behavior.impl;

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
        if ("potato_mine".equals(key) && requiresContact) {
            int plantCol = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            for (Zombie zombie : zombies) {
                if (zombie != null && !zombie.isDead() && Math.abs(mapColOf(context, zombie) - plantCol) <= 1) {
                    context.damageSingleTarget(zombie, Math.max(damageFallback(plant, 1800), 1800));
                }
            }
            spawnFx(plant, context, "POTATO_MINE_EXPLOSION", 1.2);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        if ("primal_potato_mine".equals(key) && requiresContact) {
            int pc = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            context.damageAreaAt(lane, pc, Math.max(damageFallback(plant, 2400), 2400), 1, 1);
            spawnFx(plant, context, "PRIMAL_POTATO_MINE_EXPLOSION", 1.5);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        if ("hot_potato".equals(key)) {
            boolean melted = false;
            if (context instanceof com.PVZ.model.game.RegularGameEngine engine && engine.getMap() != null) {
                int pc = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
                com.PVZ.model.entity.Tile tile = engine.getMap().getTile(lane, pc);
                if (tile != null && tile.getType() == com.PVZ.model.enums.TileType.ICE) {
                    tile.setType(com.PVZ.model.enums.TileType.NORMAL);
                    spawnFx(plant, context, "HOTPOTATO_STEAM", 2.0);
                    melted = true;
                }
            }
            if (melted) {
                com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 4.6667);
                plant.takeDamage(plant.getCurrentHp());
            }
            return;
        }

        if ("grave_buster".equals(key)) {
            if (context instanceof com.PVZ.model.game.RegularGameEngine engine && engine.getMap() != null) {
                int pc = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
                com.PVZ.model.entity.Tile tile = engine.getMap().getTile(lane, pc);
                if (tile != null && tile.getType() == com.PVZ.model.enums.TileType.TOMBSTONE) {
                    tile.setType(com.PVZ.model.enums.TileType.NORMAL);
                    tile.setHp(0);
                    spawnFx(plant, context, "GRAVEBUSTER_DIRT", 1.0);
                    com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 1.0);
                    plant.takeDamage(plant.getCurrentHp());
                }
            }
            return;
        }

        if ("ice_shroom".equals(key)) {
            context.freezeAllZombies(Math.max(3.0, plant.getStats().getFreezeTimeSeconds()));
            spawnFx(plant, context, "ICESHROOM", 1.35);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", 0.4);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        int damage = plant.getStats().getExplodeDamage() > 0 ? plant.getStats().getExplodeDamage()
            : Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage());

        if ("jalapeno".equals(key)) {
            context.damageLane(lane, Math.max(1800, damage));
            if (plant.getStats().getBooleanExtra("meltsIce", false)) context.meltIceInLane(lane);
            spawnFx(plant, context, "JALAPENO_FIRE", 1.6);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 0.6667);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }

        if ("cherry_bomb".equals(key)) {
            context.damageAreaAt(lane, asInt(plant.getRuntimeState().getOrDefault("col", 0), 0), Math.max(1800, damage), 1, 1);
            spawnFx(plant, context, "CHERRYBOMB", 1.5);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 0.7);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }

        if ("doom_shroom".equals(key)) {
            for (Zombie z : context.getAllZombies()) {
                if (z != null && !z.isDead()) z.takeDamage(Double.MAX_VALUE);
            }
            if (context instanceof com.PVZ.model.game.RegularGameEngine engine && engine.getMap() != null) {
                for (int rr = Math.max(0, row - 1); rr <= Math.min(engine.getMap().getRows() - 1, row + 1); rr++) {
                    for (int cc = Math.max(0, asInt(plant.getRuntimeState().getOrDefault("col", 0), 0) - 1);
                         cc <= Math.min(engine.getMap().getCols() - 1, asInt(plant.getRuntimeState().getOrDefault("col", 0), 0) + 1); cc++) {
                        engine.getMap().getTile(rr, cc).setType(com.PVZ.model.enums.TileType.CRATER);
                    }
                }
            }
            spawnFx(plant, context, "DOOMSHROOM", 2.8);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 3.3333);
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
            targets.removeIf(z -> z == null || z.isDead());
            if (!targets.isEmpty()) {
                int pc = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
                Zombie nearest = targets.stream().min(java.util.Comparator.comparingInt(z -> Math.abs(mapColOf(context, z) - pc))).orElse(targets.get(0));
                context.damageSingleTarget(nearest, Math.max(damage, Integer.MAX_VALUE / 4));
                spawnFx(plant, context, "SQUASH", 0.8);
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
            // Freeze only a zombie actually stepping onto this tile.
            int pc = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            Zombie target = null;
            int best = Integer.MAX_VALUE;
            for (Zombie z : zombies) {
                if (z == null || z.isDead()) continue;
                int zc = mapColOf(context, z);
                int dist = Math.abs(zc - pc);
                if (dist <= 1 && dist < best) { best = dist; target = z; }
            }
            if (target == null) return;
            target.freeze((float) Math.max(3.0, plant.getStats().getFreezeTimeSeconds()));
            spawnFx(plant, context, "ICESHROOM", 1.2);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 1.1667);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        if ("tangle_kelp".equals(key)) {
            if (context instanceof com.PVZ.model.game.RegularGameEngine engine && engine.getMap() != null) {
                int pc = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
                com.PVZ.model.entity.Tile tile = engine.getMap().getTile(lane, pc);
                if (tile != null && tile.getType() != com.PVZ.model.enums.TileType.WATER
                    && tile.getType() != com.PVZ.model.enums.TileType.TIDE) return;
            }
            context.killClosestZombieInLane(lane);
            spawnFx(plant, context, "TANGLE_KELP", 2.4);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 2.4667);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        if ("grapshot".equals(key) || "grapeshot".equals(key)) {
            context.damageArea(lane, row, damage);
            int grapeCount = plant.getStats().getIntExtra("grapeCount", 8);
            double grapeLifespan = plant.getStats().getDoubleExtra("grapeLifespanSeconds", 5.0);
            context.spawnBouncingProjectiles(lane, row, grapeCount, damage / 4, grapeLifespan);
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", 0.4);
            plant.takeDamage(plant.getCurrentHp());
            return;
        }
        context.damageAreaAt(lane, asInt(plant.getRuntimeState().getOrDefault("col", 0), 0), damage, 1, 1);
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 0.6667);
        plant.takeDamage(plant.getCurrentHp());
    }

    private int damageFallback(PlantInstance plant, int fallback) {
        int d = plant.getStats().getExplodeDamage();
        return d > 0 ? d : fallback;
    }

    private void spawnFx(PlantInstance plant, BehaviorContext context, String visualKey, double fuse) {
        Projectile fx = new Projectile();
        fx.setType(ProjectileType.UNKNOWN);
        fx.setDamage(0);
        fx.setPierce(0);
        double x = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
        double y = asDouble(plant.getRuntimeState().getOrDefault("worldY", 0.0), 0.0);
        fx.initFreePosition((float) x, (float) (y + 90), 0f, 0f);
        fx.putExtra("visualKey", visualKey);
        fx.setFuse(fuse);
        context.spawnProjectile(fx);
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
