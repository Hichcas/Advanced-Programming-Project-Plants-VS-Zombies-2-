package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.WallnutBowlingCommand;
import com.PVZ.view.input.DTO.WallnutBowlingInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class WallnutBowlingMenuCommandParser {

    private WallnutBowlingMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (WallnutBowlingCommand wallnutBowlingCommand : WallnutBowlingCommand.values()) {
            Matcher matcher = wallnutBowlingCommand.matcher(command);
            if (matcher.matches()) {
                return wallnutBowlingCommand.createDTO(matcher);
            }
        }
        return WallnutBowlingInputDTO.invalid();
    }
}
