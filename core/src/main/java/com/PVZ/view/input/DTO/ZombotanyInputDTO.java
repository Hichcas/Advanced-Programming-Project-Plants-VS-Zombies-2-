package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.ZombotanyCommand;
import com.PVZ.view.input.InputDTO;

public class ZombotanyInputDTO implements InputDTO {

    private final ZombotanyCommand command;
    private final int levelId;

    public ZombotanyInputDTO(ZombotanyCommand command, int levelId) {
        this.command = command;
        this.levelId = levelId;
    }

    public static ZombotanyInputDTO invalid() {
        return new ZombotanyInputDTO(null, -1);
    }

    public ZombotanyCommand getCommand() { return command; }
    public int getLevelId() { return levelId; }
}
