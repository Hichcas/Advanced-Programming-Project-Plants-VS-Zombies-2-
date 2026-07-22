package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.BeghouledCommand;
import com.PVZ.view.input.InputDTO;

public class BeghouledInputDTO implements InputDTO {

    private final BeghouledCommand command;
    private final int levelId;

    public BeghouledInputDTO(BeghouledCommand command, int levelId) {
        this.command = command;
        this.levelId = levelId;
    }

    public static BeghouledInputDTO invalid() {
        return new BeghouledInputDTO(null, -1);
    }

    public BeghouledCommand getCommand() { return command; }
    public int getLevelId() { return levelId; }
}
