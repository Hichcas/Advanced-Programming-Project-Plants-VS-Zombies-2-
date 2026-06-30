package com.PVZ.util;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.config.GameConfig;
import com.PVZ.model.entity.plants.PlantDataFile;
import com.PVZ.model.entity.plants.PlantDataLoader;
import com.PVZ.model.entity.plants.PlantLibrary;

import java.io.IOException;
import java.nio.file.Path;

public class GameInitialization {
    private static final PlantDataLoader PLANT_DATA_LOADER = new PlantDataLoader();

    private GameInitialization() {
    }

    public static synchronized void initialize() throws IOException {
        AppStatus.currentMenuType = MenuType.LOGIN;
        if (GameConfig.isPlantDataLoaded() && GameConfig.getPlantDataFile() != null) {
            return;
        }

        initializeFromResource(GameConfig.PLANTS_DATA_RESOURCE);
    }

    public static synchronized void initializeFromResource(String resourcePath) throws IOException {
        PlantDataFile dataFile = PLANT_DATA_LOADER.loadFromResource(resourcePath);
        bootstrap(dataFile);
    }

    public static synchronized void initializeFromFile(Path path) throws IOException {
        PlantDataFile dataFile = PLANT_DATA_LOADER.loadFromFile(path);
        bootstrap(dataFile);
    }

    private static void bootstrap(PlantDataFile dataFile) {
        if (dataFile == null) {
            throw new IllegalArgumentException("Plant data file cannot be null");
        }

        PlantLibrary.clear();
        PlantLibrary.registerAll(dataFile.getPlants());
        GameConfig.setPlantDataFile(dataFile);
    }

}
