package model.menus;

import controller.menuControllers.ProfileMenuController;
import view.input.InputDTO;

public class ProfileMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.ProfileMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        ProfileMenuController.handle(input);
    }
}
