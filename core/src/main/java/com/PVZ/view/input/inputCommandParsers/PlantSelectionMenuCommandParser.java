package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.PlantSelectionCommand;
import com.PVZ.view.input.DTO.PlantSelectionInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class PlantSelectionMenuCommandParser {

    private PlantSelectionMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (PlantSelectionCommand plantSelectionCommand : PlantSelectionCommand.values()) {
            Matcher matcher = plantSelectionCommand.matcher(command);
            if (matcher.matches()) {
                return plantSelectionCommand.createDTO(matcher);
            }
        }
        return PlantSelectionInputDTO.invalid();
    }
}
