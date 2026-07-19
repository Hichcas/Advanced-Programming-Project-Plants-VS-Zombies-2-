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
        String key = definition.getPlantKey();
        if (key == null) {
            return;
        }

        DamageSpec damageSpec = definition.getDamageSpec();
        if (damageSpec != null) {
            if (damageSpec.getKindEnum() == DamageSpec.DamageKind.MULTI_PROJECTILE
                    && damageSpec.getDamagePerProjectile() != null) {
                stats.setDamage(damageSpec.getDamagePerProjectile());
                if (damageSpec.getProjectiles() != null && damageSpec.getProjectiles() > 1) {
                    switch (key) {
                        case "rotobaga", "threepeater", "split_pea", "starfruit", "cat_tail", "bowling_bulb" -> {
                        }
                        default -> stats.putExtra("projectileCount", damageSpec.getProjectiles());
                    }
                }
            } else if (damageSpec.getKindEnum() == DamageSpec.DamageKind.TIERED
                    && damageSpec.getTiers() != null && !damageSpec.getTiers().isEmpty()) {
                stats.setDamage(damageSpec.getTiers().get(0));
            }
        }

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

        switch (key) {
            case "sun_bean" -> {
                if (stats.getSunDropAmount() <= 0) {
                    stats.setSunDropAmount(5);
                }
            }
            case "endurian" -> {
                if (stats.getReflectDamage() <= 0) {
                    stats.setReflectDamage(Math.max(20, stats.getDamage()));
                }
            }
            case "explode_o_nut" -> {
                stats.putExtra("explodeOnDeath", Boolean.TRUE);
            }
            case "potato_mine" -> {
                if (stats.getArmTimeSeconds() <= 0) {
                    stats.setArmTimeSeconds(15.0);
                }
            }
            case "primal_potato_mine" -> {
                if (stats.getArmTimeSeconds() <= 0) {
                    stats.setArmTimeSeconds(5.0);
                }
            }
            case "cactus" -> {
                if (stats.getPierce() <= 0) {
                    stats.setPierce(3);
                }
            }
            case "fume_shroom" -> {
                if (stats.getPierce() <= 0) {
                    stats.setPierce(99);
                }
            }
            default -> { }
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
