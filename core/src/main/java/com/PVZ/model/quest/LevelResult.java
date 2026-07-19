package com.PVZ.model.quest;

import com.PVZ.model.game.Map;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.PlantFamily;
import java.util.List;
import java.util.Set;

public class LevelResult {
    private Map finalMap;
    private int finalSunCount;
    private int plantsLost;
    private int zombiesKilledByLawnmower;
    private boolean won;
    private Set<PlantFamily> plantFamiliesUsed;
    private List<PlantType> plantTypesUsed;
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

    public Set<PlantFamily> getPlantFamiliesUsed() { return plantFamiliesUsed; }
    public void setPlantFamiliesUsed(Set<PlantFamily> plantFamiliesUsed) { this.plantFamiliesUsed = plantFamiliesUsed; }

    public List<PlantType> getPlantTypesUsed() { return plantTypesUsed; }
    public void setPlantTypesUsed(List<PlantType> plantTypesUsed) { this.plantTypesUsed = plantTypesUsed; }

    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public boolean isDayLevel() { return isDayLevel; }
    public void setDayLevel(boolean dayLevel) { isDayLevel = dayLevel; }
}
