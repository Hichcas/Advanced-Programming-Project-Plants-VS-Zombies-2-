package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.BeghouledCommand;
import com.PVZ.view.input.DTO.BeghouledInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class BeghouledMenuCommandParser {

    private BeghouledMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (BeghouledCommand beghouledCommand : BeghouledCommand.values()) {
            Matcher matcher = beghouledCommand.matcher(command);
            if (matcher.matches()) {
                return beghouledCommand.createDTO(matcher);
            }
        }
        return BeghouledInputDTO.invalid();
    }
}
