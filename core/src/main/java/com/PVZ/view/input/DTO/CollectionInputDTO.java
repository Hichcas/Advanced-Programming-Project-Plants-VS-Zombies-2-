package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.CollectionCommand;
import com.PVZ.view.input.InputDTO;

public class CollectionInputDTO implements InputDTO {

    private final CollectionCommand command;
    private final String plantName;
    private final String zombieName;

    public CollectionInputDTO(CollectionCommand command, String plantName, String zombieName) {
        this.command = command;
        this.plantName = plantName;
        this.zombieName = zombieName;
    }

    public static CollectionInputDTO invalid() {
        return new CollectionInputDTO(null, null, null);
    }

    public CollectionCommand getCommand() {
        return command;
    }

    public String getPlantName() {
        return plantName;
    }

    public String getZombieName() {
        return zombieName;
    }
}
