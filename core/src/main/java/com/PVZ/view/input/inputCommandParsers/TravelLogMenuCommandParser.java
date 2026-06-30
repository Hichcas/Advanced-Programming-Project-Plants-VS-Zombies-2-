package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.TravelLogCommand;
import com.PVZ.view.input.DTO.TravelLogInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class TravelLogMenuCommandParser {

    private TravelLogMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (TravelLogCommand travelLogCommand : TravelLogCommand.values()) {
            Matcher matcher = travelLogCommand.matcher(command);
            if (matcher.matches()) {
                return travelLogCommand.createDTO(matcher);
            }
        }
        return TravelLogInputDTO.invalid();
    }
}
