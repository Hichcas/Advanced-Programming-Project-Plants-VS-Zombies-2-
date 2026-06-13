package model.entity.plants.behavior.impl;

import model.entity.plants.PlantInstance;
import model.enums.PlantFlag;

import java.util.Locale;

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
            projectile.putExtra("projectileEffect", plant.getStats().getExtra("projectileEffect"));
            projectile.putExtra("projectileType", plant.getStats().getExtra("projectileType"));
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

        Object explicitType = plant.getStats().getExtra("projectileType");
        if (explicitType != null) {
            ProjectileType resolved = resolveTypeFromString(String.valueOf(explicitType));
            if (resolved != ProjectileType.UNKNOWN) {
                return resolved;
            }
        }

        Object explicitEffect = plant.getStats().getExtra("projectileEffect");
        if (explicitEffect != null) {
            ProjectileType resolved = resolveTypeFromEffect(String.valueOf(explicitEffect));
            if (resolved != ProjectileType.UNKNOWN) {
                return resolved;
            }
        }

        String behaviorId = plant.getMainBehaviorId();
        if (behaviorId != null) {
            ProjectileType resolved = resolveTypeFromString(behaviorId);
            if (resolved != ProjectileType.UNKNOWN) {
                return resolved;
            }
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

    private static ProjectileType resolveTypeFromEffect(String effect) {
        String value = normalize(effect);
        return switch (value) {
            case "fire" -> ProjectileType.FIRE_PEA;
            case "ice", "freeze", "chill" -> ProjectileType.ICE_PEA;
            case "hypnotize", "magic" -> ProjectileType.SEED;
            case "plasma", "beam" -> ProjectileType.BEAM;
            case "bomb", "explosion" -> ProjectileType.BOMB;
            case "sun" -> ProjectileType.SUN;
            default -> ProjectileType.UNKNOWN;
        };
    }

    private static ProjectileType resolveTypeFromString(String raw) {
        String value = normalize(raw);
        return switch (value) {
            case "fire_pea", "fire_pult", "fire_shot" -> ProjectileType.FIRE_PEA;
            case "ice_pea", "ice_shot", "freeze_shot" -> ProjectileType.ICE_PEA;
            case "lob", "lobber", "cabbage_pult", "kernel", "melon", "pult" -> ProjectileType.LOB;
            case "seed", "hypno", "hypnotize", "magic" -> ProjectileType.SEED;
            case "bomb", "explode", "explosion" -> ProjectileType.BOMB;
            case "beam", "plasma", "laser" -> ProjectileType.BEAM;
            case "sun" -> ProjectileType.SUN;
            case "pea", "direct_shot", "burst_shot", "homing_shot", "wall_defense", "poison" -> ProjectileType.PEA;
            default -> ProjectileType.UNKNOWN;
        };
    }

    private static double resolveSpeed(PlantInstance plant) {
        if (plant == null || plant.getStats() == null) {
            return 1.0;
        }

        if (plant.getStats().hasFlag(PlantFlag.BURST_SHOT)) {
            return 2.0;
        }

        Object explicitSpeed = plant.getStats().getExtra("projectileSpeed");
        if (explicitSpeed instanceof Number number) {
            return number.doubleValue();
        }

        Object projectileType = plant.getStats().getExtra("projectileType");
        if (projectileType != null && "BEAM".equalsIgnoreCase(String.valueOf(projectileType))) {
            return 2.5;
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

    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
