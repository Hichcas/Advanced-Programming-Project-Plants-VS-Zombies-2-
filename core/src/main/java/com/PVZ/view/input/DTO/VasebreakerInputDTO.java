package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.VasebreakerCommand;
import com.PVZ.view.input.InputDTO;

public class VasebreakerInputDTO implements InputDTO {

    private final VasebreakerCommand command;
    private final int levelId;
    private final int row;
    private final int col;

    public VasebreakerInputDTO(VasebreakerCommand command, int levelId, int row, int col) {
        this.command = command;
        this.levelId = levelId;
        this.row = row;
        this.col = col;
    }

    public static VasebreakerInputDTO invalid() {
        return new VasebreakerInputDTO(null, -1, -1, -1);
    }

    public VasebreakerCommand getCommand() { return command; }
    public int getLevelId() { return levelId; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}
