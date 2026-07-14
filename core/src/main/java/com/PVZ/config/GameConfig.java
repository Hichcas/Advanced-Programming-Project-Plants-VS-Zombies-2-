package com.PVZ.config;

import com.PVZ.model.entity.plants.PlantDataFile;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantLibrary;

import java.util.Collections;
import java.util.List;

public final class GameConfig {
    public static final String PLANTS_DATA_RESOURCE = "data/plants_structured_v6.json";
    public static final String CHAPTERS_DATA_RESOURCE = "data/chapters.json";

    private static PlantDataFile plantDataFile;
    private static boolean plantDataLoaded;

    private GameConfig() {
    }

    public static synchronized boolean isPlantDataLoaded() {
        return plantDataLoaded;
    }

    public static synchronized PlantDataFile getPlantDataFile() {
        return plantDataFile;
    }

    public static synchronized void setPlantDataFile(PlantDataFile dataFile) {
        plantDataFile = dataFile;
        plantDataLoaded = dataFile != null;
    }

    public static synchronized void resetPlantData() {
        plantDataFile = null;
        plantDataLoaded = false;
        PlantLibrary.clear();
    }

    public static synchronized int getPlantCount() {
        return plantDataFile == null ? PlantLibrary.size() : plantDataFile.getCount();
    }

    public static synchronized List<PlantDefinition> getLoadedPlants() {
        if (plantDataFile == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(plantDataFile.getPlants());
    }
}
