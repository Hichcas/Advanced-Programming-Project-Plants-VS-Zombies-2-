package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.InGameCommand;
import com.PVZ.view.input.DTO.InGameInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class InGameMenuCommandParser {

    private InGameMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (InGameCommand inGameCommand : InGameCommand.values()) {
            Matcher matcher = inGameCommand.matcher(command);
            if (matcher.matches()) {
                return inGameCommand.createDTO(matcher);
            }
        }
        return InGameInputDTO.invalid();
    }
}
