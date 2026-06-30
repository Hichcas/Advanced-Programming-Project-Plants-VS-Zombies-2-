package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.InGameCommand;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.InGameInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class InGameMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof InGameInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        return switch (dto.getCommand()) {
            case ADVANCE_TIME -> new OutputDTO(true, "Advanced time by " + dto.getTickCount() + " ticks.");
            case COLLECT_SUN -> new OutputDTO(true, "Sun collected at the given location.");
            case SHOW_SUN_AMOUNT -> showSunAmount();
            case CHEAT_ADD_SUNS -> cheatAddSuns(dto.getAmount());
            case CHEAT_REMOVE_COOLDOWN -> new OutputDTO(true, "Cooldowns removed.");
            case CHEAT_ADD_PLANT_FOOD -> new OutputDTO(true, "Added one plant food.");
            case CHEAT_SPAWN_ZOMBIE -> new OutputDTO(true, "Zombie spawned: " + dto.getZombieType());
            case CHEAT_RELEASE_NUKE -> new OutputDTO(true, "Nuke released.");
            case PLANT_PLANT -> new OutputDTO(true, "Plant placed: " + dto.getPlantType());
            case PLUCK_PLANT -> new OutputDTO(true, "Plant plucked.");
            case FEED_PLANT -> new OutputDTO(true, "Plant fed.");
            case START_ZOMBIE_WAVES -> new OutputDTO(true, "Zombie waves started.");
            case SHOW_MAP -> new OutputDTO(true, "Map shown.");
            case SHOW_PLANTS_STATUS -> new OutputDTO(true, "Plants status shown.");
            case SHOW_TILE_STATUS -> new OutputDTO(true, "Tile status shown.");
            case ZOMBIES_INFO -> new OutputDTO(true, "Zombies info shown.");
            case EXIT -> exitToGameMenu();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
        };
    }

    private OutputDTO showSunAmount() {
        User user = AppStatus.currentUser;
        if (user == null || user.userStats == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        return new OutputDTO(true, "Sun amount: 0");
    }

    private OutputDTO cheatAddSuns(Integer amount) {
        if (amount == null || amount <= 0) {
            return new OutputDTO(false, "Invalid amount.");
        }
        return new OutputDTO(true, "Added " + amount + " suns.");
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Entered Game Menu.");
    }
}
