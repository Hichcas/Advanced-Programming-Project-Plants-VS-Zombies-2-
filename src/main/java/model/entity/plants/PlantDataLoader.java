package model.entity.plants;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlantDataLoader {
    private final ObjectMapper objectMapper;

    public PlantDataLoader() {
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public PlantDataFile loadFromResource(String resourcePath) throws IOException {
        InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IOException("Plant data resource not found: " + resourcePath);
        }

        try (InputStream in = inputStream) {
            return objectMapper.readValue(in, PlantDataFile.class);
        }
    }

    public PlantDataFile loadFromFile(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return objectMapper.readValue(in, PlantDataFile.class);
        }
    }

    public PlantDataFile loadFromString(String json) throws IOException {
        return objectMapper.readValue(json, PlantDataFile.class);
    }

    public PlantDefinition[] loadPlantsFromResource(String resourcePath) throws IOException {
        return loadFromResource(resourcePath).getPlants().toArray(new PlantDefinition[0]);
    }

    public PlantDefinition[] loadPlantsFromFile(Path path) throws IOException {
        return loadFromFile(path).getPlants().toArray(new PlantDefinition[0]);
    }
}