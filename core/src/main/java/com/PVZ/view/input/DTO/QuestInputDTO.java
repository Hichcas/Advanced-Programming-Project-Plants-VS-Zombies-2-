package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.QuestCommand;
import com.PVZ.view.input.InputDTO;

public class QuestInputDTO implements InputDTO {

    private final QuestCommand command;
    private final String questId;
    private final String parameter;

    public QuestInputDTO(QuestCommand command, String questId, String parameter) {
        this.command = command;
        this.questId = questId;
        this.parameter = parameter;
    }

    public static QuestInputDTO invalid() {
        return new QuestInputDTO(null, null, null);
    }

    public QuestCommand getCommand() {
        return command;
    }

    public String getQuestId() {
        return questId;
    }

    public String getParameter() {
        return parameter;
    }
}
