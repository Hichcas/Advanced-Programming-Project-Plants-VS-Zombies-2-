package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.TravelLogCommand;
import com.PVZ.view.input.InputDTO;

public class TravelLogInputDTO implements InputDTO {

    private final TravelLogCommand command;
    private final String pageName;

    public TravelLogInputDTO(TravelLogCommand command, String pageName) {
        this.command = command;
        this.pageName = pageName;
    }

    public static TravelLogInputDTO invalid() {
        return new TravelLogInputDTO(null, null);
    }

    public TravelLogCommand getCommand() {
        return command;
    }

    public String getPageName() {
        return pageName;
    }
}
