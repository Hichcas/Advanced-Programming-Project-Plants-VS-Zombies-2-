package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.DTO.NetworkInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class NetworkMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof NetworkInputDTO networkInput)) {
            return new OutputDTO(false, "Invalid input.");
        }

        if (networkInput.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        return switch (networkInput.getCommand()) {
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToMain();
        };
    }

    private OutputDTO exitToMain() {
        AppStatus.currentMenuType = MenuType.MAIN;
        return new OutputDTO(true, "Entered Main Menu.");
    }
}
