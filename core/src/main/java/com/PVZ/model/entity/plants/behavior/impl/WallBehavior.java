package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.plants.behavior.impl.ProjectileType;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.List;

public class WallBehavior implements PlantBehavior {

    @Override
    public void onDamaged(PlantInstance plant, BehaviorContext context, Zombie attacker, int damageAmount, boolean destroyed) {
        if (plant == null || context == null || !destroyed) return;
        String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        if ("explode_o_nut".equals(key) && !Boolean.TRUE.equals(plant.getRuntimeState().get("deathExplosionDone"))) {
            int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
            int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
            int damage = Math.max(plant.getStats().getExplodeDamage(), Math.max(plant.getStats().getAoeDamage(), 1200));
            context.damageAreaAt(row, col, damage, 1, 1);
            Projectile fx = new Projectile();
            fx.setType(ProjectileType.UNKNOWN);
            fx.putExtra("visualKey", "EXPLODEONUT");
            double x = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
            double y = asDouble(plant.getRuntimeState().getOrDefault("worldY", 0.0), 0.0);
            fx.initFreePosition((float)x, (float)(y + 90), 0f, 0f);
            fx.setFuse(1.45);
            context.spawnProjectile(fx);
            plant.putRuntimeState("deathExplosionDone", Boolean.TRUE);
        }
    }

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        Integer lane = asInt(plant.getRuntimeState().getOrDefault("lane", 0), 0);
        Integer row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            return;
        }

        String plantKey = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
        double plantX = plant.getRuntimeState().get("worldX") instanceof Number n ? n.doubleValue() : 0.0;
        double tileW = plant.getRuntimeState().get("tileWidth") instanceof Number n ? n.doubleValue() : 177.0;
        for (Zombie zombie : zombies) {
            if (zombie != null && ("endurian".equals(plantKey) || Math.abs(zombie.getX() - plantX) <= tileW * 0.85)) {
                zombie.stopMoving();
            }
        }

        if (plant.getStats().getSunDropAmount() > 0) {
            Double sunTimer = asDouble(plant.getRuntimeState().getOrDefault("sunDropTimer",
                0.0), 0.0);
            sunTimer += deltaTime;
            if (sunTimer >= 1.0) {
                context.spawnSunAt(row, asInt(plant.getRuntimeState().getOrDefault("col",
                    0), 0), plant.getStats()
                    .getSunDropAmount());
                sunTimer = 0.0;
            }
            plant.putRuntimeState("sunDropTimer", sunTimer);
        }

        if (plant.getStats().getReflectDamage() > 0) {
            String key = plant.getDefinition() == null ? "" : plant.getDefinition().getPlantKey();
            if ("endurian".equals(key)) {
                double px = plant.getRuntimeState().get("worldX") instanceof Number n ? n.doubleValue() : 0.0;
                double range = plant.getRuntimeState().get("tileWidth") instanceof Number n ? n.doubleValue() * 0.9 : 160.0;
                Zombie nearest = null; double best = Double.MAX_VALUE;
                for (Zombie z : zombies) {
                    if (z == null || z.isDead()) continue;
                    double d = Math.abs(z.getX() - px);
                    if (d <= range && d < best) { best = d; nearest = z; }
                }
                if (nearest != null) nearest.takeDamage(plant.getStats().getReflectDamage());
            } else {
                context.damageAreaAt(row, asInt(plant.getRuntimeState().getOrDefault("col", 0), 0), plant.getStats().getReflectDamage(), 0, 1);
            }
        }
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
