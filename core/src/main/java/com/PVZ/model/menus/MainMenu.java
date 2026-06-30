package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.MainMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.MainMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class MainMenu extends Menu {

    private final MainMenuController controller = new MainMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return MainMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
