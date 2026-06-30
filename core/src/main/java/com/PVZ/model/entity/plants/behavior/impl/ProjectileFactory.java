package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.enums.PlantFlag;

public final class ProjectileFactory {
    private ProjectileFactory() {
    }

    public static Projectile createProjectile(
            PlantInstance plant,
            int damage
    ) {
        Projectile projectile = new Projectile();

        projectile.setType(resolveType(plant));
        projectile.setDamage(Math.max(0, damage));
        projectile.setPierce(plant == null ? 1 : Math.max(1, plant.getStats().getPierce()));
        projectile.setSpeed(resolveSpeed(plant));
        projectile.setFromPlantFood(plant != null && plant.isPlantFoodActive());

        if (plant != null) {
            projectile.setLane(asInt(plant.getRuntimeState().get("lane"), 0));
            projectile.setRow(asInt(plant.getRuntimeState().get("row"), 0));
            projectile.putExtra("plantType", plant.getType());
            projectile.putExtra("behaviorId", plant.getMainBehaviorId());
        }

        return projectile;
    }

    public static Projectile createLobProjectile(
            PlantInstance plant,
            int damage
    ) {
        Projectile projectile = createProjectile(plant, damage);
        projectile.setType(ProjectileType.LOB);
        projectile.putExtra("lob", Boolean.TRUE);
        return projectile;
    }

    public static Projectile createSunProjectile(
            PlantInstance plant,
            int sunAmount
    ) {
        Projectile projectile = new Projectile();
        projectile.setType(ProjectileType.SUN);
        projectile.setDamage(0);
        projectile.setPierce(0);
        projectile.setSpeed(0.0);
        projectile.setFromPlantFood(plant != null && plant.isPlantFoodActive());
        projectile.putExtra("sunAmount", Math.max(0, sunAmount));
        if (plant != null) {
            projectile.setLane(asInt(plant.getRuntimeState().get("lane"), 0));
            projectile.setRow(asInt(plant.getRuntimeState().get("row"), 0));
            projectile.putExtra("plantType", plant.getType());
        }
        return projectile;
    }

    private static ProjectileType resolveType(PlantInstance plant) {
        if (plant == null || plant.getStats() == null) {
            return ProjectileType.UNKNOWN;
        }

        if (plant.getStats().hasFlag(PlantFlag.CHILL_ON_HIT)) {
            return ProjectileType.ICE_PEA;
        }
        if (plant.getStats().hasFlag(PlantFlag.POISON_ON_HIT)) {
            return ProjectileType.PEA;
        }
        if (plant.getStats().hasFlag(PlantFlag.BURST_SHOT)) {
            return ProjectileType.BEAM;
        }

        return ProjectileType.PEA;
    }

    private static double resolveSpeed(PlantInstance plant) {
        if (plant == null || plant.getStats() == null) {
            return 1.0;
        }

        if (plant.getStats().hasFlag(PlantFlag.BURST_SHOT)) {
            return 2.0;
        }

        return 1.0;
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
}
