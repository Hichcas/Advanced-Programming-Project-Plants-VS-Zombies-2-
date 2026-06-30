package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.PlantSelectionMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class PlantSelectionMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return com.PVZ.view.input.inputCommandParsers.PlantSelectionMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return null;
    }


}
