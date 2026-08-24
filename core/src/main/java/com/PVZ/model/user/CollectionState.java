package com.PVZ.model.user;

import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantLibrary;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.status.AppStatus;

import java.util.*;

public class CollectionState {
    private Set<PlantType> unlockedPlants;
    private Set<ZombieType> seenZombies;
    private Map<PlantType, Integer> seedPackets;
    private Map<PlantType, Integer> plantLevels;
    private Set<PlantType> greenhouseBoosts;

    public CollectionState() {
        unlockedPlants = new HashSet<>();
        unlockedPlants.add(PlantType.PEASHOOTER);
        unlockedPlants.add(PlantType.SUNFLOWER);
        seenZombies = new HashSet<>();
        seedPackets = new HashMap<>();
        plantLevels = new HashMap<>();
        greenhouseBoosts = new HashSet<>();
    }

    public Set<PlantType> getUnlockedPlants() {
        return unlockedPlants;
    }

    public void setUnlockedPlants(Set<PlantType> unlockedPlants) {
        this.unlockedPlants = unlockedPlants;
    }

    public Set<ZombieType> getSeenZombies() {
        return seenZombies;
    }

    public void setSeenZombies(Set<ZombieType> seenZombies) {
        this.seenZombies = seenZombies;
    }

    public Map<PlantType, Integer> getSeedPackets() {
        return seedPackets;
    }

    public void setSeedPackets(Map<PlantType, Integer> seedPackets) {
        this.seedPackets = seedPackets;
    }

    public Map<PlantType, Integer> getPlantLevels() {
        return plantLevels;
    }

    public void setPlantLevels(Map<PlantType, Integer> plantLevels) {
        this.plantLevels = plantLevels;
    }

    public Set<PlantType> getGreenhouseBoosts() {
        return greenhouseBoosts;
    }

    public void setGreenhouseBoosts(Set<PlantType> greenhouseBoosts) {
        this.greenhouseBoosts = greenhouseBoosts;
    }

    public boolean isPlantUnlocked(PlantType plant) {
        return unlockedPlants.contains(plant);
    }

    public void unlockPlant(PlantType plant) {
        if (unlockedPlants.add(plant)) {
            // فقط در صورتی که گیاه قبلاً باز نشده بود، خبر ثبت شود
            User user = AppStatus.getCurrentUser();
            if (user != null && user.newsState != null) {
                user.newsState.addNews("New plant unlocked: " + plant.getDisplayName() + "!");
            }
        }
    }

    public void seeZombie(ZombieType zombie) {
        if (seenZombies.add(zombie)) {
            User user = AppStatus.getCurrentUser();
            if (user != null && user.newsState != null) {
                user.newsState.addNews("New zombie spotted: " + zombie.name() + "!");
                System.err.println("New zombie spotted: " + zombie.name() + "!");
            }
        }
    }

    public int getSeedPacketCount(PlantType plant) {
        return seedPackets.getOrDefault(plant, 0);
    }

    public void addSeedPackets(PlantType plant, int amount) {
        if (amount <= 0) return;
        seedPackets.merge(plant, amount, Integer::sum);
    }

    public boolean spendSeedPackets(PlantType plant, int amount) {
        int current = getSeedPacketCount(plant);
        if (current < amount) return false;
        seedPackets.put(plant, current - amount);
        return true;
    }

    public int getPlantLevel(PlantType plant) {
        return plantLevels.getOrDefault(plant, 0);
    }

    public void setPlantLevel(PlantType plant, int level) {
        if (level < 0) level = 0;
        plantLevels.put(plant, level);
    }

    public int upgradePlant(PlantType plant) {
        int maxRawLevel = PlantLibrary.findByType(plant)
            .map(PlantDefinition::getMaxLevel)
            .orElse(4) - 1;
        int newLevel = Math.min(getPlantLevel(plant) + 1, maxRawLevel);
        plantLevels.put(plant, newLevel);
        return newLevel;
    }

    public void setAllPlantsUnlocked() {
        // برای باز شدن همهٔ گیاهان هم خبر ارسال می‌کنیم
        for (PlantType type : PlantType.values()) {
            if (!unlockedPlants.contains(type)) {
                unlockPlant(type);  // اینجا خبر ثبت می‌شود
            }
        }
    }

    public void unlockAllZombies() {
        for (com.PVZ.model.enums.ZombieType type : com.PVZ.model.enums.ZombieType.values()) {
            seeZombie(type);
        }
    }

    public boolean hasGreenhouseBoost(PlantType plant) {
        return greenhouseBoosts.contains(plant);
    }

    public void addGreenhouseBoost(PlantType plant) {
        greenhouseBoosts.add(plant);
    }

    public boolean consumeGreenhouseBoost(PlantType plant) {
        return greenhouseBoosts.remove(plant);
    }
}
