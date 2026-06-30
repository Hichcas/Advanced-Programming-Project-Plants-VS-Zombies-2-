package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.NetworkMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.NetworkMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class NetworkMenu extends Menu {

    private final NetworkMenuController controller = new NetworkMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return NetworkMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
