package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.TravelLogMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.TravelLogMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class TravelLogMenu extends Menu {

    private final TravelLogMenuController controller = new TravelLogMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return TravelLogMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
