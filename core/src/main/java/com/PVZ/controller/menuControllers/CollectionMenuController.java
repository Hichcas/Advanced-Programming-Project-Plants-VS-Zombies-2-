package com.PVZ.controller.menuControllers;

import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantLibrary;
import com.PVZ.model.entity.plants.UpgradeCostPolicy;
import com.PVZ.model.enums.commands.CollectionCommand;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.CollectionState;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.input.DTO.CollectionInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.StringJoiner;

public class CollectionMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof CollectionInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        return switch (dto.getCommand()) {
            case SHOW_PLANTS -> showPlants(false);
            case SHOW_ALL_PLANTS -> showPlants(true);
            case SHOW_ZOMBIES -> showZombies(false);
            case SHOW_ALL_ZOMBIES -> showZombies(true);
            case SHOW_PLANT -> showPlant(dto.getPlantName());
            case SHOW_ZOMBIE -> showZombie(dto.getZombieName());
            case UPGRADE_PLANT -> upgradePlant(dto.getPlantName());
            case PURCHASE_PLANT -> purchasePlant(dto.getPlantName());
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToGameMenu();
        };
    }

    private CollectionState getState() {
        User user = AppStatus.currentUser;
        return user == null ? null : user.collectionState;
    }

    private OutputDTO showPlants(boolean all) {
        CollectionState state = getState();
        if (state == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        StringJoiner joiner = new StringJoiner("\n");
        if (all) {
            for (PlantType type : PlantType.values()) {
                joiner.add(type.getDisplayName());
            }
            return new OutputDTO(true, joiner.toString());
        }
        if (state.getUnlockedPlants().isEmpty()) {
            return new OutputDTO(true, "No unlocked plants yet.");
        }
        for (PlantType type : state.getUnlockedPlants()) {
            joiner.add(type.getDisplayName());
        }
        return new OutputDTO(true, joiner.toString());
    }

    private OutputDTO showZombies(boolean all) {
        CollectionState state = getState();
        if (state == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        StringJoiner joiner = new StringJoiner("\n");
        if (all) {
            for (ZombieType type : ZombieType.values()) {
                joiner.add(type.alias);
            }
            return new OutputDTO(true, joiner.toString());
        }
        if (state.getSeenZombies().isEmpty()) {
            return new OutputDTO(true, "No seen zombies yet.");
        }
        for (ZombieType type : state.getSeenZombies()) {
            joiner.add(type.alias);
        }
        return new OutputDTO(true, joiner.toString());
    }

    private OutputDTO showPlant(String plantName) {
        if (plantName == null) {
            return new OutputDTO(false, "Invalid plant.");
        }
        User user = AppStatus.currentUser;
        if (user == null || user.collectionState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        try {
            PlantType type = PlantType.fromName(plantName);
            user.collectionState.unlockPlant(type);
            UserRegistry.touch(user.profile.getUsername());
            return new OutputDTO(true, type.getDisplayName());
        } catch (Exception e) {
            return new OutputDTO(false, "Unknown plant.");
        }
    }

    private OutputDTO showZombie(String zombieName) {
        if (zombieName == null) {
            return new OutputDTO(false, "Invalid zombie.");
        }
        User user = AppStatus.currentUser;
        if (user == null || user.collectionState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        try {
            ZombieType type = ZombieType.fromAlias(zombieName);
            user.collectionState.seeZombie(type);
            UserRegistry.touch(user.profile.getUsername());
            return new OutputDTO(true, type.name());
        } catch (Exception e) {
            return new OutputDTO(false, "Unknown zombie.");
        }
    }

    private OutputDTO upgradePlant(String plantName) {
        if (plantName == null) {
            return new OutputDTO(false, "Invalid plant.");
        }
        try {
            PlantType type = PlantType.fromName(plantName);
            User user = AppStatus.currentUser;
            if (user == null || user.collectionState == null) {
                return new OutputDTO(false, "You must be logged in.");
            }
            int currentLevel = user.collectionState.getPlantLevel(type);

            // plants_structured_v6.json only defines 3 upgrade tiers per plant
            // (levels 2/3/4 - a couple of plants have none at all), so the raw
            // stored level (0-based) must never pass maxJsonLevel - 1. Without
            // this the collection screen let you spend seed packets forever
            // past the plant's real cap.
            int maxRawLevel = PlantLibrary.findByType(type)
                .map(PlantDefinition::getMaxLevel)
                .orElse(4) - 1;
            if (currentLevel >= maxRawLevel) {
                return new OutputDTO(false, "This plant is already at its max level.");
            }

            int currentDisplayLevel = currentLevel + 1;
            int requiredSeedPackets = UpgradeCostPolicy.currentUpgradeRequirement(
                currentDisplayLevel,
                maxRawLevel + 1
            );
            if (!user.collectionState.spendSeedPackets(type, requiredSeedPackets)) {
                return new OutputDTO(false,
                    "Not enough seed packets. Need " + requiredSeedPackets
                        + " to reach Level " + (currentDisplayLevel + 1) + "."
                );
            }
            user.collectionState.setPlantLevel(type, currentLevel + 1);
            UserRegistry.touch(user.profile.getUsername());
            return new OutputDTO(true, "Plant upgraded successfully.");
        } catch (Exception e) {
            return new OutputDTO(false, "Unknown plant.");
        }
    }

    private OutputDTO purchasePlant(String plantName) {
        if (plantName == null) {
            return new OutputDTO(false, "Invalid plant.");
        }
        User user = AppStatus.currentUser;
        if (user == null || user.userStats == null || user.collectionState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        try {
            PlantType type = PlantType.fromName(plantName);
            if (!user.userStats.spendCoins(2000)) {
                return new OutputDTO(false, "Not enough coins.");
            }
            user.collectionState.unlockPlant(type);
            UserRegistry.touch(user.profile.getUsername());
            return new OutputDTO(true, "Plant purchased successfully.");
        } catch (Exception e) {
            return new OutputDTO(false, "Unknown plant.");
        }
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Entered Game Menu.");
    }
}
