
package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.leaderboard.Leaderboard;
import com.PVZ.model.leaderboard.LeaderboardEntry;
import com.PVZ.model.leaderboard.LeaderboardSortField;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.DTO.LeaderboardInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.List;

public class LeaderboardMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof LeaderboardInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        return switch (dto.getCommand()) {
            case SHOW_LEADERBOARD -> showLeaderboard(null, true);
            case SHOW_LEADERBOARD_SORTED -> showLeaderboard(dto.getSortField(), dto.isAscending());
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToGameMenu();
        };
    }

    private OutputDTO showLeaderboard(LeaderboardSortField sort, boolean ascending) {
        List<LeaderboardEntry> entries = Leaderboard.getEntries(sort, ascending);
        if (entries.isEmpty()) {
            return new OutputDTO(true, "No leaderboard data.");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s %-25s %-10s %-8s %-10s %-10s%n",
            "Username", "Last Stage", "Minigames", "Daily", "Non-Daily", "Score"));
        sb.append("-".repeat(80)).append("\n");
        for (LeaderboardEntry e : entries) {
            sb.append(String.format("%-15s %-25s %-10d %-8d %-10d %-10d%n",
                e.getUsername(), e.getLastStageInfo(), e.getMinigamesCompleted(),
                e.getDailyQuestsCompleted(), e.getNonDailyQuestsCompleted(), e.getHighestScore()));
        }

        return new OutputDTO(true, sb.toString());
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Entered Game Menu.");
    }
}
