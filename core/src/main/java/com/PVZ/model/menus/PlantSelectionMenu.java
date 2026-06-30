package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.PlantSelectionMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.PlantSelectionMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class PlantSelectionMenu extends Menu {

    private final PlantSelectionMenuController controller = new PlantSelectionMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return PlantSelectionMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
