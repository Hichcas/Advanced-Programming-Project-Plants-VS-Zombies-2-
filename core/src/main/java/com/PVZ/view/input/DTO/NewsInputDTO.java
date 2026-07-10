package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.NewsCommand;
import com.PVZ.view.input.InputDTO;

public class NewsInputDTO implements InputDTO {

    private final NewsCommand command;

    public NewsInputDTO(NewsCommand command) {
        this.command = command;
    }

    public static NewsInputDTO invalid() {
        return new NewsInputDTO(null);
    }

    public NewsCommand getCommand() {
        return command;
    }
}
