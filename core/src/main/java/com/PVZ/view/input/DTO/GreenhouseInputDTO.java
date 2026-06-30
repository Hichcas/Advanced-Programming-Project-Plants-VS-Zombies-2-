package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.GreenhouseCommand;
import com.PVZ.view.input.InputDTO;

public class GreenhouseInputDTO implements InputDTO {

    private final GreenhouseCommand command;
    private final Integer x;
    private final Integer y;
    private final String plantName;

    public GreenhouseInputDTO(GreenhouseCommand command, Integer x, Integer y, String plantName) {
        this.command = command;
        this.x = x;
        this.y = y;
        this.plantName = plantName;
    }

    public static GreenhouseInputDTO invalid() {
        return new GreenhouseInputDTO(null, null, null, null);
    }

    public GreenhouseCommand getCommand() {
        return command;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public String getPlantName() {
        return plantName;
    }
}
