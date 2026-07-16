
package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.DTO.TravelLogInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class TravelLogMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof TravelLogInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        return switch (dto.getCommand()) {
            case SHOW_PAGE -> new OutputDTO(true, "Travel log page: " + dto.getPageName());
            case ENTER_VASEBREAKER -> enterVasebreaker();
            case ENTER_WALLNUT_BOWLING -> enterWallnutBowling();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToGameMenu();
        };
    }

    private OutputDTO enterVasebreaker() {
        AppStatus.currentMenuType = MenuType.VASEBREAKER;
        return new OutputDTO(true, "Entered Vasebreaker menu. Use 'vasebreaker start <id>' to begin a level.");
    }

    private OutputDTO enterWallnutBowling() {
        AppStatus.currentMenuType = MenuType.WALLNUT_BOWLING;
        return new OutputDTO(true, "Entered Wallnut Bowling menu. Use 'wallnutbowling start <id>' to begin a level.");
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Entered Game Menu.");
    }
}
