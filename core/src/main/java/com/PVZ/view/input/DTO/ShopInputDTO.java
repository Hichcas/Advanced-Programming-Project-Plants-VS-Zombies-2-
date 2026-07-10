package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.ShopCommand;
import com.PVZ.view.input.InputDTO;

public class ShopInputDTO implements InputDTO {

    private final ShopCommand command;
    private final String itemId;
    private final Integer count;
    private final String plantType;

    public ShopInputDTO(ShopCommand command, String itemId, Integer count, String plantType) {
        this.command = command;
        this.itemId = itemId;
        this.count = count;
        this.plantType = plantType;
    }

    public static ShopInputDTO invalid() {
        return new ShopInputDTO(null, null, null, null);
    }

    public ShopCommand getCommand() {
        return command;
    }

    public String getItemId() {
        return itemId;
    }

    public Integer getCount() {
        return count;
    }

    public String getPlantType() {
        return plantType;
    }
}
