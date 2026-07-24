package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.LeaderboardCommand;
import com.PVZ.model.leaderboard.LeaderboardSortField;
import com.PVZ.view.input.InputDTO;

public class LeaderboardInputDTO implements InputDTO {

    private final LeaderboardCommand command;
    private final LeaderboardSortField sortField;
    private final boolean ascending;

    public LeaderboardInputDTO(LeaderboardCommand command) {
        this(command, null, true);
    }

    public LeaderboardInputDTO(LeaderboardCommand command, LeaderboardSortField sortField, boolean ascending) {
        this.command = command;
        this.sortField = sortField;
        this.ascending = ascending;
    }

    public static LeaderboardInputDTO invalid() {
        return new LeaderboardInputDTO(null, null, true);
    }

    public LeaderboardCommand getCommand() {
        return command;
    }

    public LeaderboardSortField getSortField() {
        return sortField;
    }

    public boolean isAscending() {
        return ascending;
    }
}
