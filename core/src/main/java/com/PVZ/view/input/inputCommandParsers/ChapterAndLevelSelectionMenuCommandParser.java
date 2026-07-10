package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.ChapterAndLevelSelectionCommand;
import com.PVZ.view.input.DTO.ChapterAndLevelSelectionInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class ChapterAndLevelSelectionMenuCommandParser {

    private ChapterAndLevelSelectionMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (ChapterAndLevelSelectionCommand menuCommand : ChapterAndLevelSelectionCommand.values()) {
            Matcher matcher = menuCommand.matcher(command);
            if (matcher.matches()) {
                return menuCommand.createDTO(matcher);
            }
        }
        return ChapterAndLevelSelectionInputDTO.invalid();
    }
}
