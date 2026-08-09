package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.enums.DamageType;
import com.PVZ.model.enums.PlantFlag;
import com.PVZ.model.enums.PlantTag;

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
        projectile.setPierce(resolvePierce(plant));
        projectile.setSpeed(resolveSpeed(plant));
        projectile.setFromPlantFood(plant != null && plant.isPlantFoodActive());
        projectile.putExtra("visualKey", resolveVisualKey(plant, projectile.getType()));
        projectile.setAreaDamage(hasAreaDamage(plant));

        if (plant != null) {
            projectile.setLane(asInt(plant.getRuntimeState().get("lane"), 0));
            projectile.setRow(asInt(plant.getRuntimeState().get("row"), 0));
            projectile.putExtra("originCol", asInt(plant.getRuntimeState().get("col"), 0));
            projectile.putExtra("plantType", plant.getType());
            projectile.putExtra("behaviorId", plant.getMainBehaviorId());
            if (projectile.getType() == ProjectileType.ICE_PEA) {
                double chillSeconds = plant.getStats().getChillTimeSeconds();
                if (chillSeconds <= 0) chillSeconds = 3.0;
                projectile.putExtra("chillDuration", chillSeconds);
            }
            boolean poisonAttack = (plant.getDefinition() != null && plant.getDefinition().hasTag(PlantTag.POISON))
                    || plant.getStats().hasFlag(PlantFlag.POISON_ON_HIT)
                    || plant.getStats().getBooleanExtra("poisonAttack", false);
            if (poisonAttack) {
                projectile.putExtra("damageType", DamageType.POISON);
            }
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

        boolean fireAttack = plant.getStats().getBooleanExtra("fireAttack", false)
                || (plant.getDefinition() != null && plant.getDefinition().hasTag(PlantTag.FIRE));
        boolean iceAttack = plant.getStats().getBooleanExtra("iceAttack", false)
                || plant.getStats().hasFlag(PlantFlag.CHILL_ON_HIT)
                || (plant.getDefinition() != null && plant.getDefinition().hasTag(PlantTag.ICE));
        boolean burstShot = plant.getStats().hasFlag(PlantFlag.BURST_SHOT);

        if (iceAttack) {
            return ProjectileType.ICE_PEA;
        }
        if (fireAttack) {
            return ProjectileType.FIRE_PEA;
        }
        if (burstShot) {
            return ProjectileType.BEAM;
        }
        return ProjectileType.PEA;
    }

    /**
     * Base pierce count. Matches the real per-plant behavior from the design doc/Excel:
     * "Strike-through" plants pass through obstacles — Cactus explicitly says "passes
     * through 3 zombies" (base level), while Fume-shroom's smoke has no stated limit so
     * it's treated as effectively unlimited. Everything else defaults to the plant's own
     * configured pierce stat (falling back to 1 = single target) unless overridden.
     */
    private static int resolvePierce(PlantInstance plant) {
        String plantKey = (plant != null && plant.getDefinition() != null)
                ? plant.getDefinition().getPlantKey()
                : null;
        int statPierce = (plant == null || plant.getStats() == null) ? 0 : plant.getStats().getPierce();
        if ("cactus".equals(plantKey)) {
            // Base ability: "passes through 3 zombies"; Lvl2 upgrade adds +1 on top of that.
            return Math.max(3, statPierce);
        }
        if ("fume_shroom".equals(plantKey)) {
            return 999; // "passes through zombies" with no stated limit
        }
        return Math.max(1, statPierce);
    }

    /**
     * Whether this shot deals splash damage to zombies in the target's tile and the lanes
     * above/below — per the design doc, only Pepper-pult's shot and (regular/ice) Watermelon
     * shots have this area effect; everything else is single-target.
     */
    private static boolean hasAreaDamage(PlantInstance plant) {
        String plantKey = (plant != null && plant.getDefinition() != null)
                ? plant.getDefinition().getPlantKey()
                : null;
        return "pepper_pult".equals(plantKey) || "melon_pult".equals(plantKey) || "winter_melon".equals(plantKey);
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

    /**
     * Picks the real PAM visual for this shot. Plant-specific keys (Cabbage-pult, Kernel-pult,
     * Melon-pult, Citron, ...) take priority since those don't share a visual with anything
     * else; everything left over (the plain pea-shooter family) falls back to the resolved
     * {@link ProjectileType} (PEA / FIRE_PEA / ICE_PEA), which they genuinely do share.
     * Returns null if we don't have a real PAM for this plant/type yet — Projectile.draw()
     * falls back to the procedural pixmap shape in that case.
     */
    private static String resolveVisualKey(PlantInstance plant, ProjectileType type) {
        String plantKey = (plant != null && plant.getDefinition() != null)
                ? plant.getDefinition().getPlantKey()
                : null;
        if (plantKey != null) {
            switch (plantKey) {
                case "cabbage_pult": return "CABBAGE";
                case "kernel_pult": return "KERNEL";
                case "melon_pult": return "MELON";
                case "winter_melon": return "WINTER_MELON";
                case "pepper_pult": return "PEPPER";
                case "citron": return "CITRON";
                case "caulipower": return "CAULIPOWER";
                case "electric_blueberry": return "ELECTRIC_BLUEBERRY";
                case "bowling_bulb": return "BOWLING_BULB_1";
                case "starfruit": return "STARFRUIT";
                case "rotobaga": return "ROTOBAGA_1";
                case "grapeshot": return "GRAPESHOT";
                case "ice_shroom": return "ICE_SHROOM";
                case "garlic": return "GARLIC";
                case "goo_peashooter": return "GOO_PEA";
                case "mega_gatling_pea": return "MEGA_GATLING";
                case "puff_shroom": return "PUFF";
                case "sea_shroom": return "SEA_SHROOM";
                default: break;
            }
        }
        if (type == ProjectileType.FIRE_PEA) {
            return "FIRE_PEA";
        }
        if (type == ProjectileType.ICE_PEA) {
            return "SNOW_PEA";
        }
        if (type == ProjectileType.PEA) {
            return "PEA";
        }
        return null;
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
