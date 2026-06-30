package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.SettingsCommand;
import com.PVZ.view.input.DTO.SettingsInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class SettingsMenuCommandParser {

    private SettingsMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (SettingsCommand settingsCommand : SettingsCommand.values()) {
            Matcher matcher = settingsCommand.matcher(command);
            if (matcher.matches()) {
                return settingsCommand.createDTO(matcher);
            }
        }
        return SettingsInputDTO.invalid();
    }
}
