package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.MainMenuInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class MainMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof MainMenuInputDTO mainMenuInput)) {
            return new OutputDTO(false, "Invalid input.");
        }

        if (mainMenuInput.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        return switch (mainMenuInput.getCommand()) {
            case ENTER_MENU -> enterMenu(mainMenuInput.getMenuName());
            case LOGOUT, EXIT -> logout();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
        };
    }

    private OutputDTO enterMenu(String menuName) {
        if (menuName == null) {
            return new OutputDTO(false, "Invalid menu.");
        }

        return switch (menuName.toLowerCase()) {
            case "play" -> {
                AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
                yield new OutputDTO(true, "Entered Play Menu.");
            }
            case "collection" -> {
                AppStatus.currentMenuType = MenuType.COLLECTION;
                yield new OutputDTO(true, "Entered Collection Menu.");
            }
            case "settings" -> {
                AppStatus.currentMenuType = MenuType.SETTINGS;
                yield new OutputDTO(true, "Entered Settings Menu.");
            }
            case "news" -> {
                AppStatus.currentMenuType = MenuType.NEWS;
                yield new OutputDTO(true, "Entered News Menu.");
            }
            case "profile" -> {
                AppStatus.currentMenuType = MenuType.PROFILE;
                yield new OutputDTO(true, "Entered Profile Menu.");
            }
            default -> new OutputDTO(false, "Invalid menu.");
        };
    }

    private OutputDTO logout() {
        User currentUser = AppStatus.currentUser;
        if (currentUser != null) {
            currentUser.setStayLoggedIn(false);
        }
        AppStatus.currentUser = null;
        AppStatus.currentMenuType = MenuType.REGISTER;
        return new OutputDTO(true, "Logged out successfully.\nEntered Register Menu.");
    }
}
