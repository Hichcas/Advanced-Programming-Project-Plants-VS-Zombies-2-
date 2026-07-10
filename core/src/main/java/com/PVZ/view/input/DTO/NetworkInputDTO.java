package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.NetworkCommand;
import com.PVZ.view.input.InputDTO;

public class NetworkInputDTO implements InputDTO {

    private final NetworkCommand command;

    public NetworkInputDTO(NetworkCommand command) {
        this.command = command;
    }

    public static NetworkInputDTO invalid() {
        return new NetworkInputDTO(null);
    }

    public NetworkCommand getCommand() {
        return command;
    }
}
