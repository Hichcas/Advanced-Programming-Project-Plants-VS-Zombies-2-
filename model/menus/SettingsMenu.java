package model.menus;

import controller.menuControllers.SettingsMenuController;
import view.input.InputDTO;

public class SettingsMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.SettingsMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        SettingsMenuController.handle(input);
    }
}
