package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.EndOfGameCommand;
import com.PVZ.view.input.DTO.EndOfGameInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class EndOfGameCommandParser {

    private EndOfGameCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (EndOfGameCommand menuCommand : EndOfGameCommand.values()) {
            Matcher matcher = menuCommand.matcher(command);
            if (matcher.matches()) {
                return menuCommand.createDTO(matcher);
            }
        }
        return EndOfGameInputDTO.invalid();
    }
}
