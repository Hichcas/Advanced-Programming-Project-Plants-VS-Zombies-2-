package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.LoginMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.LoginMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class LoginMenu extends Menu {

    private final LoginMenuController controller = new LoginMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return LoginMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
