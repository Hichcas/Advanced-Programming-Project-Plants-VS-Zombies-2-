package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.LeaderboardCommand;
import com.PVZ.view.input.DTO.LeaderboardInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class LeaderboardMenuCommandParser {

    private LeaderboardMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (LeaderboardCommand leaderboardCommand : LeaderboardCommand.values()) {
            Matcher matcher = leaderboardCommand.matcher(command);
            if (matcher.matches()) {
                return leaderboardCommand.createDTO(matcher);
            }
        }
        return LeaderboardInputDTO.invalid();
    }
}
