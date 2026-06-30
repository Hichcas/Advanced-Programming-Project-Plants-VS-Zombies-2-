
package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.LeaderboardCommand;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.input.DTO.LeaderboardInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.Comparator;
import java.util.stream.Collectors;

public class LeaderboardMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof LeaderboardInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        return switch (dto.getCommand()) {
            case SHOW_LEADERBOARD -> showLeaderboard();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToGameMenu();
        };
    }

    private OutputDTO showLeaderboard() {
        String result = UserRegistry.allUsers().stream()
                .filter(u -> u != null && u.profile != null)
                .sorted(Comparator.comparingInt((User u) -> u.userStats == null ? 0 : u.userStats.getHighestScore()).reversed())
                .limit(10)
                .map(u -> u.profile.getUsername() + " : " + (u.userStats == null ? 0 : u.userStats.getHighestScore()))
                .collect(Collectors.joining("\n"));
        if (result.isBlank()) {
            return new OutputDTO(true, "No leaderboard data.");
        }
        return new OutputDTO(true, result);
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Entered Game Menu.");
    }
}
