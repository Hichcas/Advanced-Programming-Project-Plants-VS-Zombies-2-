package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.SettingsInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class SettingsMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof SettingsInputDTO settingsInput)) {
            return new OutputDTO(false, "Invalid input.");
        }

        if (settingsInput.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        return switch (settingsInput.getCommand()) {
            case CHANGE_DIFFICULTY -> changeDifficulty(settingsInput.getDifficultyLevel());
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToMain();
        };
    }

    private OutputDTO changeDifficulty(Integer difficultyLevel) {
        User currentUser = AppStatus.currentUser;
        if (currentUser == null || currentUser.appStats == null) {
            return new OutputDTO(false, "You must be logged in.");
        }

        if (difficultyLevel == null || difficultyLevel < 1 || difficultyLevel > 5) {
            return new OutputDTO(false, "Invalid difficulty level.");
        }

        currentUser.appStats.setDifficultyLevel(difficultyLevel);
        return new OutputDTO(true, "Difficulty level set to " + currentUser.appStats.getDifficultyLevel() + ".");
    }

    private OutputDTO exitToMain() {
        AppStatus.currentMenuType = MenuType.MAIN;
        return new OutputDTO(true, "Entered Main Menu.");
    }
}
