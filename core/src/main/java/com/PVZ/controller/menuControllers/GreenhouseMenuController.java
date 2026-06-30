
package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.GreenhouseCommand;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.greenhouse.GreenhouseState;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.GreenhouseInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class GreenhouseMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof GreenhouseInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        return switch (dto.getCommand()) {
            case SHOW_GREENHOUSE -> showGreenhouse();
            case PLANT_POT_AT -> plantPot(dto.getX(), dto.getY(), dto.getPlantName());
            case COLLECT -> collect(dto.getX(), dto.getY());
            case GROW -> grow(dto.getX(), dto.getY());
            case ENTER_SHOP -> enterShop();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToGameMenu();
        };
    }

    private User currentUser() {
        return AppStatus.currentUser;
    }

    private OutputDTO showGreenhouse() {
        User user = currentUser();
        if (user == null || user.greenhouseState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        GreenhouseState state = user.greenhouseState;
        StringBuilder builder = new StringBuilder();
        for (int y = 1; y <= 4; y++) {
            for (int x = 1; x <= 5; x++) {
                GreenhouseState.Pot pot = state.getPot(x, y);
                builder.append(pot.isUnlocked() ? "[O]" : "[X]");
                if (pot.getPlantType() != null) {
                    builder.append(pot.getPlantType().getDisplayName());
                }
                builder.append(' ');
            }
            builder.append("\n");
        }
        return new OutputDTO(true, builder.toString().trim());
    }

    private OutputDTO plantPot(Integer x, Integer y, String plantName) {
        User user = currentUser();
        if (user == null || user.greenhouseState == null || user.collectionState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        try {
            PlantType type = plantName == null ? null : PlantType.fromName(plantName);
            if (type == null) {
                return new OutputDTO(false, "Invalid plant.");
            }
            user.greenhouseState.plantInPot(x, y, type, System.currentTimeMillis());
            return new OutputDTO(true, "Plant placed in greenhouse.");
        } catch (Exception e) {
            return new OutputDTO(false, e.getMessage());
        }
    }

    private OutputDTO collect(Integer x, Integer y) {
        User user = currentUser();
        if (user == null || user.greenhouseState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        try {
            PlantType harvested = user.greenhouseState.collectFromPot(x, y);
            if (harvested == null) {
                return new OutputDTO(false, "Nothing to collect.");
            }
            if (user.collectionState.isPlantUnlocked(harvested)) {
                user.collectionState.getGreenhouseBoosts().add(harvested);
                return new OutputDTO(true, "Greenhouse boost stored for " + harvested.getDisplayName() + ".");
            }
            user.userStats.addCoins(500);
            return new OutputDTO(true, "Collected 500 coins.");
        } catch (Exception e) {
            return new OutputDTO(false, e.getMessage());
        }
    }

    private OutputDTO grow(Integer x, Integer y) {
        User user = currentUser();
        if (user == null || user.greenhouseState == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        try {
            user.greenhouseState.accelerateGrowth(x, y);
            return new OutputDTO(true, "Growth accelerated.");
        } catch (Exception e) {
            return new OutputDTO(false, e.getMessage());
        }
    }

    private OutputDTO enterShop() {
        AppStatus.currentMenuType = MenuType.SHOP;
        return new OutputDTO(true, "Entered Shop Menu.");
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Entered Game Menu.");
    }
}
