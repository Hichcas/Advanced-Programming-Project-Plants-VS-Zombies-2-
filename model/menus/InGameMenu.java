package model.menus;

import controller.menuControllers.InGameMenuController;
import view.input.InputDTO;

public class InGameMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.InGameMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        InGameMenuController.handle(input);
    }
}
