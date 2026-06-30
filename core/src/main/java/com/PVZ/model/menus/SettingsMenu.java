package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.SettingsMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.SettingsMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class SettingsMenu extends Menu {

    private final SettingsMenuController controller = new SettingsMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return SettingsMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
