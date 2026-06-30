package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.NetworkMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class NetworkMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return com.PVZ.view.input.inputCommandParsers.NetworkMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return null;
    }


}
