package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.DamageType;

import java.util.List;

public class MeleeEatBehavior implements PlantBehavior {

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        boolean isChomperPlant = plant.getDefinition() != null
            && "chomper".equals(plant.getDefinition().getPlantKey());
        if (!isChomperPlant && !isCooldownReady(plant, deltaTime)) {
            return;
        }

        performMeleeAttack(plant, context);
    }

    private boolean isCooldownReady(PlantInstance plant, double deltaTime) {
        double timer = asDouble(plant.getRuntimeState().getOrDefault("meleeTimer",
            0.0), 0.0);
        timer += deltaTime;

        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }

        if (timer < cooldown) {
            plant.putRuntimeState("meleeTimer", timer);
            return false;
        }

        plant.putRuntimeState("meleeTimer", 0.0);
        return true;
    }

    private void performMeleeAttack(PlantInstance plant, BehaviorContext context) {
        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
        boolean isChomper = plant.getDefinition() != null
            && "chomper".equals(plant.getDefinition().getPlantKey());

        if (isChomper) {
            performChomperAttack(plant, context, lane);
            return;
        }

        boolean isWasabi = plant.getDefinition() != null
            && "wasabi_whip".equals(plant.getDefinition().getPlantKey());
        if (isWasabi) {
            double tileWidth = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0),
                177.0);
            double plantX = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0),
                0.0);
            double range = tileWidth * 1.2;
            int damage = computeMeleeDamage(plant);
            boolean hit = applyWasabiWhipDamage(plant, context, lane, plantX, range, damage);
            if (hit) {
                com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", 0.4);
                spawnWhipVisual(plant, context, lane, plantX);
            }
            if (hit && plant.isPlantFoodActive()) {
                context.consumePlantFood(plant);
            }
            return;
        }

        String meleeKey = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        if ("kiwibeast".equals(meleeKey)) {
            performKiwiAttack(plant, context, lane);
            return;
        }

        boolean isAreaAttack = isAreaMelee(plant);

        double tileWidth = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0),
            177.0);
        double plantX = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
        double range = isAreaAttack ? tileWidth * 1.6 : tileWidth * 1.2;
        if ("bonk_choy".equals(meleeKey)) range = tileWidth * 1.25;

        int damage = computeMeleeDamage(plant);
        boolean hitAnything;
        int plantCol = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        if ("phat_beet".equals(meleeKey)) {
            context.damageAreaAt(lane, plantCol, damage, 1, 1);
            hitAnything = true;
        } else {
            hitAnything = applyDamageToZombies(plant, context, lane, isAreaAttack, plantX, range, damage);
        }
        if (hitAnything) {
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "shooting", 0.4);
        }

        if (hitAnything && plant.getDefinition() != null
            && "phat_beet".equals(plant.getDefinition().getPlantKey())) {
            spawnSonicVisual(plant, context, lane, plantX, "PHAT_BEET", 0.7333);
        }

        if (hitAnything && plant.isPlantFoodActive()) {
            context.consumePlantFood(plant);
        }
    }

    private void spawnWhipVisual(PlantInstance plant, BehaviorContext context, int lane, double plantX) {
        double tileW = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0),
            177.0);
        double tileH = asDouble(plant.getRuntimeState().getOrDefault("tileHeight", 234.0),
            234.0);
        double py = asDouble(plant.getRuntimeState().getOrDefault("worldY", 0.0), 0.0);
        com.PVZ.model.entity.plants.behavior.impl.Projectile fx = new Projectile();
        fx.setType(com.PVZ.model.entity.plants.behavior.impl.ProjectileType.WHIP);
        fx.setDamage(0);
        fx.setPierce(0);
        fx.initFreePosition((float) (plantX + tileW * 0.5), (float) (py + tileH * 0.35f), 0f, 0f);
        fx.setFuse(0.4);
        context.spawnProjectile(fx);
    }

    private void spawnSonicVisual(PlantInstance plant, BehaviorContext context, int lane, double plantX, String visualKey, double fuse) {
        double tileW = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0),
            177.0);
        double tileH = asDouble(plant.getRuntimeState().getOrDefault("tileHeight", 234.0),
            234.0);
        double py = asDouble(plant.getRuntimeState().getOrDefault("worldY", 0.0), 0.0);
        com.PVZ.model.entity.plants.behavior.impl.Projectile fx = new Projectile();
        fx.setType(com.PVZ.model.entity.plants.behavior.impl.ProjectileType.WHIP);
        fx.setDamage(0);
        fx.setPierce(0);
        fx.initFreePosition((float) (plantX + tileW * 1.3), (float) (py + tileH * 0.35f), 0f, 0f);
        fx.putExtra("visualKey", visualKey);
        fx.setFuse(fuse);
        context.spawnProjectile(fx);
    }

    private void performChomperAttack(PlantInstance plant, BehaviorContext context, int lane) {
        if (isDigesting(plant)) {
            return;
        }
        double tileWidth = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0),
            177.0);
        double plantX = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0),
            0.0);
        double range = tileWidth * 1.2;

        java.util.List<com.PVZ.model.entity.zombies.base.Zombie> zombies = context.getZombiesInLane(lane);
        for (com.PVZ.model.entity.zombies.base.Zombie z : zombies) {
            if (z == null || z.isDead()) continue;
            if (java.lang.Math.abs(z.getX() - plantX) <= range) {
                z.takeDamage(Double.MAX_VALUE);
                com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 0.9333);
                plant.putRuntimeState("digesting", Boolean.TRUE);
                plant.putRuntimeState("digestTimer", 0.0);
                return;
            }
        }
    }

    private void performKiwiAttack(PlantInstance plant, BehaviorContext context, int lane) {
        double timer = asDouble(plant.getRuntimeState().getOrDefault("kiwiStageTimer", 0.0), 0.0) + 0.1;
        int stage = asInt(plant.getRuntimeState().getOrDefault("kiwiStage", 1), 1);
        if (stage < 2 && timer >= 24.0) { stage = 2; }
        if (stage < 3 && timer >= 72.0) { stage = 3; }
        plant.putRuntimeState("kiwiStageTimer", timer);
        plant.putRuntimeState("kiwiStage", stage);
        int damage = stage == 1 ? 15 : stage == 2 ? 30 : 45;
        double tileW = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0), 177.0);
        int pc = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        int radius = stage;
        context.damageAreaAt(lane, pc, damage, radius, radius);
        Projectile fx = new Projectile();
        fx.setType(com.PVZ.model.entity.plants.behavior.impl.ProjectileType.WHIP);
        fx.setDamage(0); fx.setPierce(0);
        double px = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
        double py = asDouble(plant.getRuntimeState().getOrDefault("worldY", 0.0), 0.0);
        fx.initFreePosition((float)(px + tileW*0.5), (float)(py + 80), 0, 0);
        fx.putExtra("visualKey", "KIWI_BEAST");
        fx.setFuse(0.6667);
        context.spawnProjectile(fx);
        com.PVZ.model.entity.PlantAnimation.trigger(plant, stage == 1 ? "attack_stage1" : stage == 2 ? "attack_stage2" : "attack_stage3", 1.5);
    }

    private boolean isDigesting(PlantInstance plant) {
        if (!Boolean.TRUE.equals(plant.getRuntimeState().getOrDefault("digesting", Boolean.FALSE))) {
            return false;
        }
        double timer = asDouble(plant.getRuntimeState().getOrDefault("digestTimer", 0.0),
            0.0);
        timer += 0.1;
        plant.putRuntimeState("digestTimer", timer);
        if (timer >= 40.0) {
            plant.putRuntimeState("digesting", Boolean.FALSE);
            plant.putRuntimeState("digestTimer", 0.0);
            return false;
        }
        return true;
    }

    private boolean isAreaMelee(PlantInstance plant) {
        return plant.getStats().getBooleanExtra("areaMelee", false)
            || "phat_beet".equals(plant.getDefinition().getPlantKey())
            || "kiwibeast".equals(plant.getDefinition().getPlantKey());
    }

    private boolean applyWasabiWhipDamage(PlantInstance plant, BehaviorContext context,
                                          int lane, double plantX, double range, int damage) {
        boolean hit = false;
        java.util.List<com.PVZ.model.entity.zombies.base.Zombie> front = context.getZombiesInLane(lane);
        for (com.PVZ.model.entity.zombies.base.Zombie z : front) {
            if (z != null && !z.isDead() && java.lang.Math.abs(z.getX() - plantX) <= range) {
                z.takeDamage(damage, com.PVZ.model.enums.DamageType.NORMAL);
                hit = true;
                break;
            }
        }
        for (com.PVZ.model.entity.zombies.base.Zombie z : front) {
            if (z != null && !z.isDead() && z.getX() < plantX - 5
                && java.lang.Math.abs(z.getX() - plantX) <= range) {
                z.takeDamage(damage, com.PVZ.model.enums.DamageType.NORMAL);
                hit = true;
                break;
            }
        }
        return hit;
    }

    private int computeMeleeDamage(PlantInstance plant) {
        int damage = Math.max(0, plant.getStats().getDamage());
        double damageMultiplier = plant.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (plant.isPlantFoodActive()) {
            damageMultiplier = Math.max(damageMultiplier,
                plant.getStats().getDoubleExtra("plantFoodDamageMultiplier", 2.0));
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.round(damage * damageMultiplier));
    }

    private boolean applyDamageToZombies(PlantInstance plant, BehaviorContext context,
                                         int lane, boolean isAreaAttack,
                                         double plantX, double range, int damage) {
        int[] lanes = isAreaAttack ? new int[]{lane - 1, lane, lane + 1} : new int[]{lane};
        boolean hitAnything = false;

        for (int targetLane : lanes) {
            if (targetLane < 0) {
                continue;
            }
            List<Zombie> zombies = context.getZombiesInLane(targetLane);
            for (Zombie z : zombies) {
                if (z == null || z.isDead()) {
                    continue;
                }
                if (Math.abs(z.getX() - plantX) <= range) {
                    z.takeDamage(damage, DamageType.NORMAL);
                    hitAnything = true;
                    if (!isAreaAttack) {
                        break;
                    }
                }
            }
        }
        return hitAnything;
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
}

