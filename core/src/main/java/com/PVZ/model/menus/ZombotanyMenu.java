package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.ZombotanyMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.ZombotanyMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class ZombotanyMenu extends Menu {

    private final ZombotanyMenuController controller = new ZombotanyMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return ZombotanyMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
