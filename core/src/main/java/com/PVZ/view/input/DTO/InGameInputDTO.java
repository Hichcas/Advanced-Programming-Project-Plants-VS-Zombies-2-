package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.InGameCommand;
import com.PVZ.view.input.InputDTO;

public class InGameInputDTO implements InputDTO {

    private final InGameCommand command;
    private final Integer tickCount;
    private final Integer amount;
    private final String plantType;
    private final String zombieType;
    private final Integer x;
    private final Integer y;

    public InGameInputDTO(InGameCommand command,
                          Integer tickCount,
                          Integer amount,
                          String plantType,
                          String zombieType,
                          Integer x,
                          Integer y) {
        this.command = command;
        this.tickCount = tickCount;
        this.amount = amount;
        this.plantType = plantType;
        this.zombieType = zombieType;
        this.x = x;
        this.y = y;
    }

    public static InGameInputDTO invalid() {
        return new InGameInputDTO(null, null, null, null, null, null, null);
    }

    public InGameCommand getCommand() {
        return command;
    }

    public Integer getTickCount() {
        return tickCount;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getPlantType() {
        return plantType;
    }

    public String getZombieType() {
        return zombieType;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }
}
