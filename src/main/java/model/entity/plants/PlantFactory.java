package model.entity.plants;

import model.entity.Plant;
import model.enums.PlantType;

public final class PlantFactory {

    private PlantFactory() {
    }

    public static PlantInstance create(PlantDefinition definition, int level) {
        if (definition == null) {
            throw new IllegalArgumentException("Plant definition cannot be null");
        }

        int safeLevel = Math.max(1, level);
        PlantStats stats = UpgradeResolver.resolveStats(definition, safeLevel);
        return new PlantInstance(definition, stats, safeLevel);
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