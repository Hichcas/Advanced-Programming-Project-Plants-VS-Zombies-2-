package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.EndOfGameCommand;
import com.PVZ.view.input.InputDTO;

public class EndOfGameInputDTO implements InputDTO {

    private final EndOfGameCommand command;

    public EndOfGameInputDTO(EndOfGameCommand command) {
        this.command = command;
    }

    public static EndOfGameInputDTO invalid() {
        return new EndOfGameInputDTO(null);
    }

    public EndOfGameCommand getCommand() {
        return command;
    }
}
