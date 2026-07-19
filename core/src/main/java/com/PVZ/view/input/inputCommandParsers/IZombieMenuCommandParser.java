package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.IZombieCommand;
import com.PVZ.view.input.DTO.IZombieInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class IZombieMenuCommandParser {

    private IZombieMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (IZombieCommand izombieCommand : IZombieCommand.values()) {
            Matcher matcher = izombieCommand.matcher(command);
            if (matcher.matches()) {
                return izombieCommand.createDTO(matcher);
            }
        }
        return IZombieInputDTO.invalid();
    }
}
