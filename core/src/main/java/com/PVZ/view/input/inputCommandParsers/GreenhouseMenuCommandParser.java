package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.GreenhouseCommand;
import com.PVZ.view.input.DTO.GreenhouseInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class GreenhouseMenuCommandParser {

    private GreenhouseMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (GreenhouseCommand greenhouseCommand : GreenhouseCommand.values()) {
            Matcher matcher = greenhouseCommand.matcher(command);
            if (matcher.matches()) {
                return greenhouseCommand.createDTO(matcher);
            }
        }
        return GreenhouseInputDTO.invalid();
    }
}
