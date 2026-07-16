package com.PVZ.view.input.inputCommandParsers;

import com.PVZ.model.enums.commands.QuestCommand;
import com.PVZ.view.input.DTO.QuestInputDTO;
import com.PVZ.view.input.InputDTO;

import java.util.regex.Matcher;

public class QuestMenuCommandParser {

    private QuestMenuCommandParser() {}

    public static InputDTO parseCommand(String command) {
        for (QuestCommand cmd : QuestCommand.values()) {
            Matcher matcher = cmd.matcher(command);
            if (matcher.matches()) {
                return cmd.createDTO(matcher);
            }
        }
        return QuestInputDTO.invalid();
    }
}
