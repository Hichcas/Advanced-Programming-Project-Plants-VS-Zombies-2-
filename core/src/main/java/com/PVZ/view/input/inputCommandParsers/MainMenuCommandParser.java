package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.MainMenuCommand;
import com.PVZ.view.input.DTO.MainMenuInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class MainMenuCommandParser {

    private MainMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (MainMenuCommand mainMenuCommand : MainMenuCommand.values()) {
            Matcher matcher = mainMenuCommand.matcher(command);
            if (matcher.matches()) {
                return mainMenuCommand.createDTO(matcher);
            }
        }
        return MainMenuInputDTO.invalid();
    }
}
