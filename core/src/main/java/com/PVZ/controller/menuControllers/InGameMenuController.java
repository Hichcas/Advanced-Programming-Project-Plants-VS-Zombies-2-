package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.InGameCommand;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.game.RegularGameEngine;
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

        RegularGameEngine engine = getEngine();
        return switch (dto.getCommand()) {
            case ADVANCE_TIME -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.advanceTimeText(dto.getTickCount()));
            case COLLECT_SUN -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.collectSunAt(dto.getX(), dto.getY()));
            case SHOW_SUN_AMOUNT -> showSunAmount(engine);
            case CHEAT_ADD_SUNS -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.addSunsCheat(dto.getAmount()));
            case CHEAT_REMOVE_COOLDOWN -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.removeCooldownCheat());
            case CHEAT_ADD_PLANT_FOOD -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.addPlantFoodCheat());
            case CHEAT_SPAWN_ZOMBIE -> new OutputDTO(true, "Zombie spawned: " + dto.getZombieType());
            case CHEAT_RELEASE_NUKE -> new OutputDTO(true, "Nuke released.");
            case PLANT_PLANT -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.plantPlant(dto.getPlantType(), dto.getX(), dto.getY()));
            case PLUCK_PLANT -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.pluckPlant(dto.getX(), dto.getY()));
            case FEED_PLANT -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.feedPlant(dto.getX(), dto.getY()));
            case START_ZOMBIE_WAVES -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.startZombieWavesText());
            case SHOW_MAP -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.showMapText());
            case SHOW_PLANTS_STATUS -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.showPlantsStatusText());
            case SHOW_TILE_STATUS -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.showTileStatusText(dto.getX(), dto.getY()));
            case ZOMBIES_INFO -> engine == null
                    ? new OutputDTO(false, "Game engine is not ready.")
                    : new OutputDTO(true, engine.zombiesInfoText());
            case EXIT -> exitToGameMenu();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
        };
    }

    private OutputDTO showSunAmount(RegularGameEngine engine) {
        User user = AppStatus.currentUser;
        if (user == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.showSunAmountText());
    }

    private RegularGameEngine getEngine() {
        if (AppStatus.getGameEngine() instanceof RegularGameEngine regularGameEngine) {
            return regularGameEngine;
        }
        return null;
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Entered Game Menu.");
    }
}
