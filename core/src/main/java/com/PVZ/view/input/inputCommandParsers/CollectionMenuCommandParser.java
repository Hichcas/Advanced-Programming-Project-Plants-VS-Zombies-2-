package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.CollectionCommand;
import com.PVZ.view.input.DTO.CollectionInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class CollectionMenuCommandParser {

    private CollectionMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (CollectionCommand collectionCommand : CollectionCommand.values()) {
            Matcher matcher = collectionCommand.matcher(command);
            if (matcher.matches()) {
                return collectionCommand.createDTO(matcher);
            }
        }
        return CollectionInputDTO.invalid();
    }
}
