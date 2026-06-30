package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.ProfileMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.ProfileMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class ProfileMenu extends Menu {

    private final ProfileMenuController controller = new ProfileMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return ProfileMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
