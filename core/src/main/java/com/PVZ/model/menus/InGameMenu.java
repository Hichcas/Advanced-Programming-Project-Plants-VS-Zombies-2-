package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.InGameMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class InGameMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return com.PVZ.view.input.inputCommandParsers.InGameMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return null;
    }

}
