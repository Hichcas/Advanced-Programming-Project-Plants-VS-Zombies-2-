package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.RegisterMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.RegisterMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class RegisterMenu extends Menu {

    private final RegisterMenuController controller =
            new RegisterMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return RegisterMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }

}
