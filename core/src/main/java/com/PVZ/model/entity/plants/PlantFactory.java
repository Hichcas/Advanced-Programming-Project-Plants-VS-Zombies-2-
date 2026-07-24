package com.PVZ.model.entity.plants;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.enums.PlantTag;
import com.PVZ.model.enums.PlantType;

public final class PlantFactory {

    private PlantFactory() {
    }

    public static PlantInstance create(PlantDefinition definition, int level) {
        if (definition == null) {
            throw new IllegalArgumentException("Plant definition cannot be null");
        }

        int safeLevel = Math.max(1, level);
        PlantStats stats = UpgradeResolver.resolveStats(definition, safeLevel);
        applyInnateSpecials(definition, stats);
        return new PlantInstance(definition, stats, safeLevel);
    }

    private static void applyInnateSpecials(PlantDefinition definition, PlantStats stats) {
        if (definition.getPlantKey() == null) {
            return;
        }
        applyDamageSpec(definition, stats);
        applyPlantTags(definition, stats);
        applySpecialPlantKeys(definition, stats);
    }

    private static void applyDamageSpec(PlantDefinition definition, PlantStats stats) {
        DamageSpec damageSpec = definition.getDamageSpec();
        if (damageSpec == null) {
            return;
        }

        if (damageSpec.getKindEnum() == DamageSpec.DamageKind.MULTI_PROJECTILE
            && damageSpec.getDamagePerProjectile() != null) {
            int current = stats.getDamage();
            int basePerProj = damageSpec.getDamagePerProjectile();
            int baseTotal = damageSpec.getTotalDamage() != null ? damageSpec.getTotalDamage() : basePerProj;
            int upgradeDelta = Math.max(0, current - baseTotal);
            int projCount = Math.max(1, damageSpec.getProjectiles() != null ? damageSpec.getProjectiles() : 1);
            int perProjDmg = basePerProj + (projCount > 0 ? upgradeDelta / projCount : 0);
            stats.setDamage(perProjDmg);
            if (projCount > 1) {
                String key = definition.getPlantKey();
                if (!isMultiLanePlant(key)) {
                    stats.putExtra("projectileCount", projCount);
                }
            }
        } else if (damageSpec.getKindEnum() == DamageSpec.DamageKind.TIERED
            && damageSpec.getTiers() != null && !damageSpec.getTiers().isEmpty()) {
            int current = stats.getDamage();
            int tier0 = damageSpec.getTiers().get(0);
            int upgradeDelta = Math.max(0, current - tier0);
            stats.setDamage(tier0 + upgradeDelta);
        }
    }

    private static boolean isMultiLanePlant(String key) {
        return "rotobaga".equals(key) || "threepeater".equals(key) || "split_pea".equals(key)
            || "starfruit".equals(key) || "cat_tail".equals(key) || "bowling_bulb".equals(key);
    }

    private static void applyPlantTags(PlantDefinition definition, PlantStats stats) {
        if (definition.hasTag(PlantTag.FIRE)) {
            stats.putExtra("fireAttack", Boolean.TRUE);
            if (stats.getDoubleExtra("damageMultiplier", 1.0) <= 1.0) {
                stats.putExtra("damageMultiplier", 2.0);
            }
        }
        if (definition.hasTag(PlantTag.ICE)) {
            stats.putExtra("iceAttack", Boolean.TRUE);
            stats.putExtra("freezeAttack", Boolean.TRUE);
        }
        if (definition.hasTag(PlantTag.POISON)) {
            stats.putExtra("poisonAttack", Boolean.TRUE);
        }
        if (definition.hasTag(PlantTag.CHARGE) && stats.getChargeTimeSeconds() <= 0) {
            stats.setChargeTimeSeconds(2.0);
        }
    }

    private static void applySpecialPlantKeys(PlantDefinition definition, PlantStats stats) {
        String key = definition.getPlantKey();
        switch (key) {
            case "sun_bean":
                if (stats.getSunDropAmount() <= 0) {
                    stats.setSunDropAmount(5);
                }
                break;
            case "endurian":
                if (stats.getReflectDamage() <= 0) {
                    stats.setReflectDamage(Math.max(20, stats.getDamage()));
                }
                break;
            case "explode_o_nut":
                stats.putExtra("explodeOnDeath", Boolean.TRUE);
                break;
            case "potato_mine":
                if (stats.getArmTimeSeconds() <= 0) {
                    stats.setArmTimeSeconds(15.0);
                }
                break;
            case "primal_potato_mine":
                if (stats.getArmTimeSeconds() <= 0) {
                    stats.setArmTimeSeconds(5.0);
                }
                break;
            case "cactus":
                if (stats.getPierce() <= 0) {
                    stats.setPierce(3);
                }
                break;
            case "fume_shroom":
                if (stats.getPierce() <= 0) {
                    stats.setPierce(99);
                }
                break;
            default:
                break;
        }
    }

    public static Plant createPlant(PlantDefinition definition, int userLevel) {
        PlantInstance instance = create(definition, userLevel);
        return new Plant(instance);
    }

    public static Plant createPlant(String name, int userLevel) {
        PlantDefinition definition = PlantLibrary.findByName(name).orElse(null);

        if (definition == null) {
            System.err.println(" Factory Warning: Cannot create plant. Unknown plant name: " + name);
            return null;
        }

        PlantInstance instance = create(definition, userLevel);
        Plant created = new Plant(instance);
        System.out.println(" Factory: Created " + created.getType() + " (Level " + instance.getLevel() + ")");
        return created;
    }

    public static Plant createPlant(PlantType type, int userLevel) {
        if (type == null) {
            return null;
        }

        PlantDefinition definition = type.getDefinition();
        if (definition == null) {
            System.err.println(" Factory Warning: Cannot create plant. Unknown plant type: " + type);
            return null;
        }

        return createPlant(definition, userLevel);
    }
}
