package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.IZombieCommand;
import com.PVZ.view.input.InputDTO;

public class IZombieInputDTO implements InputDTO {

    private final IZombieCommand command;
    private final int levelId;
    private final int row;
    private final int col;
    private final String zombieAlias;

    public IZombieInputDTO(IZombieCommand command, int levelId, int row, int col, String zombieAlias) {
        this.command = command;
        this.levelId = levelId;
        this.row = row;
        this.col = col;
        this.zombieAlias = zombieAlias;
    }

    public static IZombieInputDTO invalid() {
        return new IZombieInputDTO(null, -1, -1, -1, null);
    }

    public IZombieCommand getCommand() { return command; }
    public int getLevelId() { return levelId; }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public String getZombieAlias() { return zombieAlias; }
}
