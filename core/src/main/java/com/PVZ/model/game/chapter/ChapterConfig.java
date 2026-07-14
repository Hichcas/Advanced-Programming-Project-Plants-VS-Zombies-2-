package com.PVZ.model.game.chapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChapterConfig {
    private String name;
    private String displayName;
    private EnvironmentConfig environment;
    private List<SetupAction> setup;
    private List<UpdateAction> update;
    private List<StageConfig> stages;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EnvironmentConfig {
        private boolean disableFallingSun;
        private String description;

        public boolean isDisableFallingSun() { return disableFallingSun; }
        public void setDisableFallingSun(boolean disableFallingSun) { this.disableFallingSun = disableFallingSun; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SetupAction {
        private String action;
        private String description;

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateAction {
        private String action;
        private String description;

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public EnvironmentConfig getEnvironment() { return environment; }
    public void setEnvironment(EnvironmentConfig environment) { this.environment = environment; }
    public List<SetupAction> getSetup() { return setup; }
    public void setSetup(List<SetupAction> setup) { this.setup = setup; }
    public List<UpdateAction> getUpdate() { return update; }
    public void setUpdate(List<UpdateAction> update) { this.update = update; }
    public List<StageConfig> getStages() { return stages; }
    public void setStages(List<StageConfig> stages) { this.stages = stages; }
}
