package com.PVZ.model.entity.plants;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantDataFile {

    @JsonAlias({"schemaVersion", "version"})
    private int schemaVersion;

    @JsonAlias({"source", "sourceFile", "origin"})
    private Map<String, Object> sourceFile = new HashMap<>();

    @JsonAlias({"notes", "description"})
    private List<String> notes = new ArrayList<>();

    private List<PlantDefinition> plants = new ArrayList<>();

    public PlantDataFile() {
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Map<String, Object> getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(Map<String, Object> sourceFile) {
        this.sourceFile = sourceFile;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public List<PlantDefinition> getPlants() {
        return plants;
    }

    public void setPlants(List<PlantDefinition> plants) {
        this.plants = plants == null
                ? new ArrayList<>()
                : new ArrayList<>(plants);
    }

    public int getCount() {
        return plants == null ? 0 : plants.size();
    }
}
