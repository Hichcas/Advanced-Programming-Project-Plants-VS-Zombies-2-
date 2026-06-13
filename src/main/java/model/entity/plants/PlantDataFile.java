package model.entity.plants;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlantDataFile {
    @JsonAlias({"schemaVersion", "version"})
    private int schemaVersion;

    @JsonAlias({"source", "sourceFile", "origin"})
    private String sourceFile;

    @JsonAlias({"notes", "description"})
    private String notes;

    private List<PlantDefinition> plants = new ArrayList<>();

    public PlantDataFile() {
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PlantDefinition> getPlants() {
        return plants;
    }

    public void setPlants(List<PlantDefinition> plants) {
        this.plants = plants == null ? new ArrayList<>() : new ArrayList<>(plants);
    }

    public int getCount() {
        return plants == null ? 0 : plants.size();
    }
}