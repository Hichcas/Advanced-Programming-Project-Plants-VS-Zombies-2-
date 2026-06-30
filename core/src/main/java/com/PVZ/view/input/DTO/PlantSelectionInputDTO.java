package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.PlantSelectionCommand;
import com.PVZ.view.input.InputDTO;

public class PlantSelectionInputDTO implements InputDTO {

    private final PlantSelectionCommand command;
    private final String plantType;

    public PlantSelectionInputDTO(PlantSelectionCommand command, String plantType) {
        this.command = command;
        this.plantType = plantType;
    }

    public static PlantSelectionInputDTO invalid() {
        return new PlantSelectionInputDTO(null, null);
    }

    public PlantSelectionCommand getCommand() {
        return command;
    }

    public String getPlantType() {
        return plantType;
    }
}
