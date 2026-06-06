package model.entity.plants;


public class PlantFactory {

    public static Plant createPlant(String name, int userLevel) {

        PlantDefinition definition = PlantDatabase.getPlantDefinition(name);

        if (definition == null) {
            System.err.println("⚠️ Factory Warning: Cannot create plant. Unknown plant name: " + name);
            return null;
        }

        if (userLevel < 1) {
            userLevel = 1;
        }

        Plant newPlant = new Plant(definition, userLevel);

        System.out.println("🌱 Factory: Created " + newPlant.getName() + " (Level " + userLevel + ") for the battlefield.");
        return newPlant;
    }
}