package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.enums.PlantCategory;
import com.PVZ.model.enums.PlantTag;

public class ShooterBehavior implements PlantBehavior {
    private static final double DEFAULT_BURST_GAP_SECONDS = 0.12;
    private static final double MIN_BURST_GAP_SECONDS = 0.06;
    private static final double MAX_BURST_GAP_SECONDS = 0.16;

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        if (handlePuffLifespan(plant, context, deltaTime)) {
            return;
        }
        drainBurst(plant, context, deltaTime);

        Double attackTimer = asDouble(plant.getRuntimeState().getOrDefault("attackTimer", 0.0), 0.0);
        attackTimer += deltaTime;

        double cooldown = getCooldown(plant);
        if (attackTimer < cooldown) {
            plant.putRuntimeState("attackTimer", attackTimer);
            return;
        }
        attackTimer = 0.0;

        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
        if (!hasTargets(plant, context, lane)) {
            plant.putRuntimeState("attackTimer", attackTimer);
            return;
        }

        int damage = calculateDamage(plant);
        int projectileCount = calculateProjectileCount(plant);
        startBurst(plant, damage, projectileCount);

        plant.putRuntimeState("attackTimer", attackTimer);
    }

    /**
     * Whether this plant currently has a valid target to fire at. Threepeater checks its
     * own lane plus the lane above/below (it fires into all 3 the moment any one has a
     * zombie); Split Pea only needs its own lane, since its backward shot always
     * accompanies the forward one. Everything else just checks its own lane.
     */
    private boolean hasTargets(PlantInstance plant, BehaviorContext context, int lane) {
        if (!context.getZombiesInLane(lane).isEmpty()) {
            return true;
        }
        int plantCol = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        if (context.hasObstacleAheadInLane(lane, plantCol)) {
            return true;
        }
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        if ("threepeater".equals(key)) {
            int top = Math.max(0, lane - 1);
            int bottom = Math.min(context.getRowCount() - 1, lane + 1);
            if (!context.getZombiesInLane(top).isEmpty() || context.hasObstacleAheadInLane(top, plantCol)) return true;
            if (!context.getZombiesInLane(bottom).isEmpty() || context.hasObstacleAheadInLane(bottom, plantCol)) return true;
        }
        return false;
    }


    private double getCooldown(PlantInstance plant) {
        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }
        if (plant.getDefinition() != null && plant.getDefinition().hasTag(PlantTag.CHARGE)) {
            double chargeTime = plant.getStats().getChargeTimeSeconds();
            if (chargeTime <= 0) {
                chargeTime = 2.0;
            }
            cooldown = Math.max(cooldown, Math.max(1.0, chargeTime));
        }
        return cooldown;
    }

    private int calculateDamage(PlantInstance plant) {
        int damage = Math.max(0, plant.getStats().getDamage());
        double multiplier = plant.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (plant.isPlantFoodActive()) {
            double pfMultiplier = plant.getStats().getDoubleExtra("plantFoodDamageMultiplier", 2.0);
            multiplier = Math.max(multiplier, pfMultiplier);
        }
        return (int) Math.round(damage * multiplier);
    }

    private int calculateProjectileCount(PlantInstance plant) {
        int count = Math.max(1, plant.getStats().getIntExtra("projectileCount", 1));
        if (plant.isPlantFoodActive()) {
            int pfCount = plant.getStats().getIntExtra("plantFoodProjectileCount", count);
            count = Math.max(count, pfCount);
        }
        return count;
    }


    private void startBurst(PlantInstance plant, int damage, int projectileCount) {
        int count = Math.max(1, projectileCount);
        plant.putRuntimeState("burstRemaining", count);
        plant.putRuntimeState("burstDamage", damage);

        // Start the shooting PAM once per volley. The old code restarted it for every
        // projectile, so a 3/4/5-shot volley could never stay synchronized with the clip.
        double animationDuration = com.PVZ.model.entity.PlantAnimation
            .resolveDuration(plant, "shooting", 0.5);
        double gap;
        String plantKey = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        if ("mega_gatling_pea".equals(plantKey)) {
            gap = 0.06;
        } else if ("repeater".equals(plantKey) && count >= 2) {
            // Repeater's two peas are a very tight consecutive pair.  The old 0.16s
            // clamp made them visually look like a single shot.
            gap = 0.09;
        } else {
            gap = count <= 1
                ? DEFAULT_BURST_GAP_SECONDS
                : animationDuration / Math.max(1, count - 1);
            gap = Math.max(MIN_BURST_GAP_SECONDS, Math.min(MAX_BURST_GAP_SECONDS, gap));
        }
        plant.putRuntimeState("burstGapSeconds", gap);
        plant.putRuntimeState("burstTimer", gap);
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", animationDuration);
    }

    private void drainBurst(PlantInstance plant, BehaviorContext context, double deltaTime) {
        int remaining = asInt(plant.getRuntimeState().getOrDefault("burstRemaining", 0), 0);
        if (remaining <= 0) {
            return;
        }
        double timer = asDouble(plant.getRuntimeState().getOrDefault("burstTimer", 0.0), 0.0);
        timer += deltaTime;
        double gap = asDouble(plant.getRuntimeState().getOrDefault("burstGapSeconds", DEFAULT_BURST_GAP_SECONDS),
            DEFAULT_BURST_GAP_SECONDS);
        if (timer < gap) {
            plant.putRuntimeState("burstTimer", timer);
            return;
        }
        int damage = asInt(plant.getRuntimeState().getOrDefault("burstDamage", 0), 0);
        fireVolley(plant, context, damage);
        plant.putRuntimeState("burstRemaining", remaining - 1);
        plant.putRuntimeState("burstTimer", 0.0);
    }

    /**
     * Fires one "volley" for this plant's shot pattern:
     * - Threepeater: three peas in its own lane, side-by-side at the muzzle.
     * - Split Pea: one pea forward (its own lane, normal direction) plus two peas backward
     *   (same lane, reversed direction) — per its real ability: "1 shot forward, 2 backward".
     * - Everything else: a single forward pea, same as before.
     */
    private void fireVolley(PlantInstance plant, BehaviorContext context, int damage) {
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        if ("threepeater".equals(key)) {
            int count = 3;
            for (int i = 0; i < count; i++) {
                spawnOne(plant, context, damage, null, false, spreadOffset(i, count));
            }
        } else if ("mega_gatling_pea".equals(key)) {
            int count = 4;
            for (int i = 0; i < count; i++) {
                spawnOne(plant, context, damage, null, false, 0.0);
            }
        } else if ("split_pea".equals(key)) {
            // One shot forward + one shot backward.
            spawnOne(plant, context, damage, null, false, 0.0);
            spawnOne(plant, context, damage, null, true, 0.0);
        } else {
            spawnOne(plant, context, damage, null, false, 0.0);
        }
    }

    private double spreadOffset(int index, int count) {
        if (count <= 1) return 0.0;
        // Same lane, visually separated at the muzzle. Positive values move the
        // projectile slightly ahead in world X while preserving its row/lane.
        return (index - (count - 1) / 2.0) * 18.0;
    }

    private void spawnOne(PlantInstance plant, BehaviorContext context, int damage,
                           Integer rowOverride, boolean reverse, double spawnXOffset) {
        Projectile projectile = ProjectileFactory.createProjectile(plant, damage);
        if (rowOverride != null) {
            projectile.setRow(rowOverride);
            projectile.setLane(rowOverride);
        }
        if (reverse) {
            projectile.putExtra("reverseDirection", Boolean.TRUE);
        }

        if (Math.abs(spawnXOffset) > 0.001) {
            projectile.putExtra("spawnXOffset", spawnXOffset);
        }

        // "fireAttack" / "iceAttack" / "passThrough" / "pierceBoost" are the extras that
        // Plant Food behaviors (ManualPlantFoodBehavior) stamp onto the plant's stats when
        // its temporary effect fires - e.g. handleFireBurst, handleCactus, handlePultFamily.
        // Those calls never clear the extra afterwards; the *only* thing that tells us the
        // Plant Food window is still open is plant.isPlantFoodActive(). Without gating on
        // it here, a single Plant Food use permanently mutates the plant (e.g. Cactus would
        // pierce forever after one use) even though its own idle/attack animation and
        // ProjectileFactory's type resolution correctly fall back to normal once it expires.
        // Innately fire/ice plants aren't affected: those come from PlantTag on the
        // definition, resolved separately in ProjectileFactory.resolveType().
        boolean plantFoodActive = plant.isPlantFoodActive();
        if (plantFoodActive && plant.getStats().getBooleanExtra("fireAttack", false)) {
            projectile.setType(ProjectileType.FIRE_PEA);
        }
        if (plantFoodActive && plant.getStats().getBooleanExtra("iceAttack", false)) {
            projectile.setType(ProjectileType.ICE_PEA);
        }

        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        if ("fume_shroom".equals(key)) {
            projectile.setType(ProjectileType.FUME);
        }
        boolean shouldPierce = (plantFoodActive && plant.getStats().getBooleanExtra("passThrough", false))
            || (plant.getDefinition() != null
            && plant.getDefinition().getCategoryEnum() == PlantCategory.THROUGH_STRIKE);
        if (shouldPierce) {
            int pierceBoost = plant.getStats().getIntExtra("pierceBoost", 3);
            projectile.setPierce(Math.max(projectile.getPierce(), pierceBoost));
        }

        context.spawnProjectile(projectile);
    }

    private boolean handlePuffLifespan(PlantInstance plant, BehaviorContext context, double deltaTime) {
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        if (!"puff_shroom".equals(key)) {
            return false;
        }
        double life = asDouble(plant.getRuntimeState().getOrDefault("lifespanTimer", 0.0), 0.0);
        life += deltaTime;
        double maxLife = plant.getStats().getLifespanSeconds();
        if (maxLife <= 0.0) maxLife = 60.0;
        plant.putRuntimeState("lifespanTimer", life);
        if (life >= maxLife) {
            int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
            int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            context.removePlant(row, col);
            return true;
        }
        return false;
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
