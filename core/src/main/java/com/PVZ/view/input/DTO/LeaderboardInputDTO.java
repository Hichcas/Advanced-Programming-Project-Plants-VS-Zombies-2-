package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.LeaderboardCommand;
import com.PVZ.view.input.InputDTO;

public class LeaderboardInputDTO implements InputDTO {

    private final LeaderboardCommand command;

    public LeaderboardInputDTO(LeaderboardCommand command) {
        this.command = command;
    }

    public static LeaderboardInputDTO invalid() {
        return new LeaderboardInputDTO(null);
    }

    public LeaderboardCommand getCommand() {
        return command;
    }
}
