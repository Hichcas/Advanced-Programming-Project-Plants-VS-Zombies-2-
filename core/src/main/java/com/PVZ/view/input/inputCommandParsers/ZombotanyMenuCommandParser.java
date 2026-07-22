package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.ZombotanyCommand;
import com.PVZ.view.input.DTO.ZombotanyInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class ZombotanyMenuCommandParser {

    private ZombotanyMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (ZombotanyCommand zombotanyCommand : ZombotanyCommand.values()) {
            Matcher matcher = zombotanyCommand.matcher(command);
            if (matcher.matches()) {
                return zombotanyCommand.createDTO(matcher);
            }
        }
        return ZombotanyInputDTO.invalid();
    }
}
