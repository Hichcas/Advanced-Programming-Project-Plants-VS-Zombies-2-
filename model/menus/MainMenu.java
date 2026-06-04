package model.menus;

import controller.menuControllers.MainMenuController;
import view.input.InputDTO;

public class MainMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.MainMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        MainMenuController.handle(input);
    }

}
