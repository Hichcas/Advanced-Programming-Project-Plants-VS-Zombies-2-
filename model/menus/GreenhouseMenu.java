package model.menus;

import controller.menuControllers.GreenhouseMenuController;
import view.input.InputDTO;

public class GreenhouseMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.GreenhouseMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        GreenhouseMenuController.handle(input);
    }
}
