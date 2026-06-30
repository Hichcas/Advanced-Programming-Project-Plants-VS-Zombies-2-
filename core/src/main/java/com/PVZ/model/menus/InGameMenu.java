package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.InGameMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.InGameMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class InGameMenu extends Menu {

    private final InGameMenuController controller = new InGameMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return InGameMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
