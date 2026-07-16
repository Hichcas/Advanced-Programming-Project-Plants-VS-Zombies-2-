package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.WallnutBowlingCommand;
import com.PVZ.view.input.InputDTO;

public class WallnutBowlingInputDTO implements InputDTO {

    private final WallnutBowlingCommand command;
    private final int levelId;
    private final int row;
    private final int col;

    public WallnutBowlingInputDTO(WallnutBowlingCommand command, int levelId, int row, int col) {
        this.command = command;
        this.levelId = levelId;
        this.row = row;
        this.col = col;
    }

    public static WallnutBowlingInputDTO invalid() {
        return new WallnutBowlingInputDTO(null, -1, -1, -1);
    }

    public WallnutBowlingCommand getCommand() { return command; }
    public int getLevelId() { return levelId; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}
