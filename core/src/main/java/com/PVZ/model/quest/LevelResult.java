package com.PVZ.model.quest;

import com.PVZ.model.game.Map;
import java.util.List;
import java.util.Set;

public class LevelResult {
    private Map finalMap;
    private int finalSunCount;
    private int plantsLost;
    private int zombiesKilledByLawnmower;
    private boolean won;
    private Set<String> plantFamiliesUsed;   // e.g., "pepper-mint", "explosive", "sun-producer"
    private List<String> plantTypesUsed;     // plant type names (e.g., "PEASHOOTER")
    private int difficultyLevel;
    private boolean isDayLevel;

    public LevelResult() {}

    public Map getFinalMap() { return finalMap; }
    public void setFinalMap(Map finalMap) { this.finalMap = finalMap; }

    public int getFinalSunCount() { return finalSunCount; }
    public void setFinalSunCount(int finalSunCount) { this.finalSunCount = finalSunCount; }

    public int getPlantsLost() { return plantsLost; }
    public void setPlantsLost(int plantsLost) { this.plantsLost = plantsLost; }

    public int getZombiesKilledByLawnmower() { return zombiesKilledByLawnmower; }
    public void setZombiesKilledByLawnmower(int zombiesKilledByLawnmower) { this.zombiesKilledByLawnmower = zombiesKilledByLawnmower; }

    public boolean isWon() { return won; }
    public void setWon(boolean won) { this.won = won; }

    public Set<String> getPlantFamiliesUsed() { return plantFamiliesUsed; }
    public void setPlantFamiliesUsed(Set<String> plantFamiliesUsed) { this.plantFamiliesUsed = plantFamiliesUsed; }

    public List<String> getPlantTypesUsed() { return plantTypesUsed; }
    public void setPlantTypesUsed(List<String> plantTypesUsed) { this.plantTypesUsed = plantTypesUsed; }

    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public boolean isDayLevel() { return isDayLevel; }
    public void setDayLevel(boolean dayLevel) { isDayLevel = dayLevel; }
}
