package model.entity.plants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PlantDatabase {


    private static final Map<String, PlantDefinition> plantMap = new HashMap<>();

    private static final List<PlantDefinition> allPlants = new ArrayList<>();


    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JsonRootWrapper {
        private int count;
        private List<PlantDefinition> plants = new ArrayList<>();

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public List<PlantDefinition> getPlants() { return plants; }
        public void setPlants(List<PlantDefinition> plants) { this.plants = plants; }
    }


    public static void loadPlants(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();


        JsonRootWrapper root = objectMapper.readValue(new File(filePath), JsonRootWrapper.class);


        plantMap.clear();
        allPlants.clear();

        if (root != null && root.getPlants() != null) {
            allPlants.addAll(root.getPlants());
            for (PlantDefinition plant : root.getPlants()) {
                plantMap.put(plant.getName().toLowerCase().trim(), plant);
            }
        }

        System.out.println("🎉 Database initialized! Loaded " + allPlants.size() + " plants successfully.");
    }


    public static PlantDefinition getPlantDefinition(String name) {
        if (name == null) return null;
        return plantMap.get(name.toLowerCase().trim());
    }


    public static List<PlantDefinition> getAllPlants() {
        return new ArrayList<>(allPlants);
    }
}