package model.menus;

import controller.menuControllers.RegisterMenuController;
import view.input.InputDTO;

public class RegisterMenu extends Menu {
    @Override
    public InputDTO parseNextCommand (String command) {
        return view.input.inputCommandParsers.RegisterMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        RegisterMenuController.handle(input);
    }
}
