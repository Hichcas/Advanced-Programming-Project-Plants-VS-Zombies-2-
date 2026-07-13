package com.PVZ.model.entity.plants;

import com.PVZ.model.entity.Plant;
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

    /**
     * A handful of plants have a base-kit passive that isn't represented anywhere in the
     * generic JSON ability schema (WallBehavior already knows how to *use* sunDropAmount /
     * reflectDamage once set — via UpgradeResolver's REFLECT_DAMAGE/SUN_DROP upgrade kind —
     * but nothing was ever setting a base value at level 1). This fills those in.
     */
    private static void applyInnateSpecials(PlantDefinition definition, PlantStats stats) {
        String key = definition.getPlantKey();
        if (key == null) {
            return;
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
