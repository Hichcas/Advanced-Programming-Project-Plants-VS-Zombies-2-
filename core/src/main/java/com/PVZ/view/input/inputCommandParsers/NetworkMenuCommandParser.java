package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.NetworkCommand;
import com.PVZ.view.input.DTO.NetworkInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class NetworkMenuCommandParser {

    private NetworkMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (NetworkCommand networkCommand : NetworkCommand.values()) {
            Matcher matcher = networkCommand.matcher(command);
            if (matcher.matches()) {
                return networkCommand.createDTO(matcher);
            }
        }
        return NetworkInputDTO.invalid();
    }
}
