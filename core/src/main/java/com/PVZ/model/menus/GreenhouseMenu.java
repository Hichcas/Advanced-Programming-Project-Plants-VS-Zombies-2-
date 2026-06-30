package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.GreenhouseMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.GreenhouseMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class GreenhouseMenu extends Menu {

    private final GreenhouseMenuController controller = new GreenhouseMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return GreenhouseMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
