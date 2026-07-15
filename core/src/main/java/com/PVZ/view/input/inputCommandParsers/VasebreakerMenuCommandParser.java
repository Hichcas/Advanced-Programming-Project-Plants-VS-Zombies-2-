package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.VasebreakerCommand;
import com.PVZ.view.input.DTO.VasebreakerInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class VasebreakerMenuCommandParser {

    private VasebreakerMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (VasebreakerCommand vasebreakerCommand : VasebreakerCommand.values()) {
            Matcher matcher = vasebreakerCommand.matcher(command);
            if (matcher.matches()) {
                return vasebreakerCommand.createDTO(matcher);
            }
        }
        return VasebreakerInputDTO.invalid();
    }
}
