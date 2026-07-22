package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.BeghouledMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.BeghouledMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class BeghouledMenu extends Menu {

    private final BeghouledMenuController controller = new BeghouledMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return BeghouledMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
