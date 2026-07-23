package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.DamageType;

import java.util.List;

/**
 * Behavior for melee eating plants (Chomper, Bonk Choy, etc.).
 * Refactored to comply with Checkstyle and PMD (method length ≤ 50 lines).
 */
public class MeleeEatBehavior implements PlantBehavior {

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        boolean isChomperPlant = plant.getDefinition() != null
            && "chomper".equals(plant.getDefinition().getPlantKey());

        // Chomper's "cooldown" is the 40s digest period tracked by isDigesting(), not a
        // generic attack-interval timer. Gating it behind isCooldownReady() (which reads
        // actionIntervalSeconds == 40 for Chomper) meant it silently waited a full 40
        // seconds after being planted before ever taking its first bite. Skip the generic
        // gate for Chomper and let performMeleeAttack's own digest check handle timing.
        if (!isChomperPlant && !isCooldownReady(plant, deltaTime)) {
            return;
        }

        // Perform attack
        performMeleeAttack(plant, context);
    }

    /**
     * Checks if the melee cooldown has elapsed and updates the timer.
     * Returns true if ready to attack.
     */
    private boolean isCooldownReady(PlantInstance plant, double deltaTime) {
        double timer = asDouble(plant.getRuntimeState().getOrDefault("meleeTimer", 0.0), 0.0);
        timer += deltaTime;

        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }

        if (timer < cooldown) {
            plant.putRuntimeState("meleeTimer", timer);
            return false;
        }

        // Reset timer for next attack
        plant.putRuntimeState("meleeTimer", 0.0);
        return true;
    }

    /**
     * Performs the melee attack: determines area or single target,
     * calculates damage, applies to zombies in range.
     */
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
            double tileWidth = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0), 177.0);
            double plantX = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
            double range = tileWidth * 1.2;
            int damage = computeMeleeDamage(plant);
            boolean hit = applyWasabiWhipDamage(plant, context, lane, plantX, range, damage);
            if (hit) {
                spawnWhipVisual(plant, context, lane, plantX);
            }
            if (hit && plant.isPlantFoodActive()) {
                context.consumePlantFood(plant);
            }
            return;
        }

        boolean isAreaAttack = isAreaMelee(plant);

        double tileWidth = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0), 177.0);
        double plantX = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
        double range = isAreaAttack ? tileWidth * 1.6 : tileWidth * 1.2;

        int damage = computeMeleeDamage(plant);
        boolean hitAnything = applyDamageToZombies(plant, context, lane, isAreaAttack, plantX, range, damage);

        // Phat Beet: spawn sonic visual
        if (hitAnything && plant.getDefinition() != null
            && "phat_beet".equals(plant.getDefinition().getPlantKey())) {
            spawnSonicVisual(plant, context, lane, plantX);
        }

        if (hitAnything && plant.isPlantFoodActive()) {
            context.consumePlantFood(plant);
        }
    }

    /**
     * Wasabi Whip: spawn a WHIP visual projectile toward the front.
     */
    private void spawnWhipVisual(PlantInstance plant, BehaviorContext context, int lane, double plantX) {
        double tileW = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0), 177.0);
        double tileH = asDouble(plant.getRuntimeState().getOrDefault("tileHeight", 234.0), 234.0);
        double py = asDouble(plant.getRuntimeState().getOrDefault("worldY", 0.0), 0.0);
        com.PVZ.model.entity.plants.behavior.impl.Projectile fx = new com.PVZ.model.entity.plants.behavior.impl.Projectile();
        fx.setType(com.PVZ.model.entity.plants.behavior.impl.ProjectileType.WHIP);
        fx.setDamage(0);
        fx.setPierce(0);
        fx.initFreePosition((float) (plantX + tileW * 0.5), (float) (py + tileH * 0.35f), 0f, 0f);
        fx.setFuse(0.4);
        context.spawnProjectile(fx);
    }

    /**
     * Spawns a brief visual WHIP projectile at the plant to show the sonic burst for Phat Beet.
     */
    private void spawnSonicVisual(PlantInstance plant, BehaviorContext context, int lane, double plantX) {
        double tileW = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0), 177.0);
        double tileH = asDouble(plant.getRuntimeState().getOrDefault("tileHeight", 234.0), 234.0);
        double py = asDouble(plant.getRuntimeState().getOrDefault("worldY", 0.0), 0.0);
        com.PVZ.model.entity.plants.behavior.impl.Projectile fx = new com.PVZ.model.entity.plants.behavior.impl.Projectile();
        fx.setType(com.PVZ.model.entity.plants.behavior.impl.ProjectileType.WHIP);
        fx.setDamage(0);
        fx.setPierce(0);
        fx.initFreePosition((float) (plantX + tileW * 1.3), (float) (py + tileH * 0.35f), 0f, 0f);
        fx.setFuse(0.3);
        context.spawnProjectile(fx);
    }

    /**
     * Chomper: instantly swallows the nearest front zombie (taking no damage from it),
     * then enters a 40-second digest period during which it cannot attack again.
     */
    private void performChomperAttack(PlantInstance plant, BehaviorContext context, int lane) {
        // Check if currently digesting
        if (isDigesting(plant)) {
            return;
        }
        double tileWidth = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0), 177.0);
        double plantX = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
        double range = tileWidth * 1.2;

        java.util.List<com.PVZ.model.entity.zombies.base.Zombie> zombies = context.getZombiesInLane(lane);
        for (com.PVZ.model.entity.zombies.base.Zombie z : zombies) {
            if (z == null || z.isDead()) continue;
            if (java.lang.Math.abs(z.getX() - plantX) <= range) {
                // Instantly kill the zombie
                z.takeDamage(Double.MAX_VALUE);
                // Enter digest phase — cannot attack for 40 s
                plant.putRuntimeState("digesting", Boolean.TRUE);
                plant.putRuntimeState("digestTimer", 0.0);
                return;
            }
        }
    }

    /**
     * Checks digest state and timer; after digest period ends, clears the flag.
     */
    private boolean isDigesting(PlantInstance plant) {
        if (!Boolean.TRUE.equals(plant.getRuntimeState().getOrDefault("digesting", Boolean.FALSE))) {
            return false;
        }
        double timer = asDouble(plant.getRuntimeState().getOrDefault("digestTimer", 0.0), 0.0);
        timer += 0.1;  // called once per tick (~0.1s)
        plant.putRuntimeState("digestTimer", timer);
        // Chomper digest period = 40 seconds
        if (timer >= 40.0) {
            plant.putRuntimeState("digesting", Boolean.FALSE);
            plant.putRuntimeState("digestTimer", 0.0);
            return false;
        }
        return true;
    }

    /**
     * Determines if this plant has area melee attack.
     */
    private boolean isAreaMelee(PlantInstance plant) {
        return plant.getStats().getBooleanExtra("areaMelee", false)
            || "phat_beet".equals(plant.getDefinition().getPlantKey())
            || "kiwibeast".equals(plant.getDefinition().getPlantKey());
    }

    /**
     * Wasabi Whip: hits both the front cell AND the back cell in the same lane.
     * The base applyDamageToZombies already handles front zombies; we also hit behind.
     */
    private boolean applyWasabiWhipDamage(PlantInstance plant, BehaviorContext context,
                                           int lane, double plantX, double range, int damage) {
        boolean hit = false;
        // Front (same lane) — zombies in front of the plant
        java.util.List<com.PVZ.model.entity.zombies.base.Zombie> front = context.getZombiesInLane(lane);
        for (com.PVZ.model.entity.zombies.base.Zombie z : front) {
            if (z != null && !z.isDead() && java.lang.Math.abs(z.getX() - plantX) <= range) {
                z.takeDamage(damage, com.PVZ.model.enums.DamageType.NORMAL);
                hit = true;
                break; // whip hits only one zombie per direction
            }
        }
        // Back (same lane, but behind the plant: X < plantX)
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

    /**
     * Computes the final damage including multipliers and plant food bonus.
     */
    private int computeMeleeDamage(PlantInstance plant) {
        int damage = Math.max(0, plant.getStats().getDamage());
        double damageMultiplier = plant.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (plant.isPlantFoodActive()) {
            damageMultiplier = Math.max(damageMultiplier,
                plant.getStats().getDoubleExtra("plantFoodDamageMultiplier", 2.0));
        }
        return (int) Math.min(Integer.MAX_VALUE, Math.round(damage * damageMultiplier));
    }

    /**
     * Applies damage to zombies in the affected lanes.
     * Returns true if at least one zombie was hit.
     */
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
                        // single-target plants only hit once per swing
                        break;
                    }
                }
            }
        }
        return hitAnything;
    }

    // ------------------------------------------------------------------------
    // Utility methods (unchanged)
    // ------------------------------------------------------------------------

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

