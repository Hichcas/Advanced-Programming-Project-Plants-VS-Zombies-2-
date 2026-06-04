package model.menus;

import controller.menuControllers.TravelLogMenuController;
import view.input.InputDTO;

public class TravelLogMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.TravelLogMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        TravelLogMenuController.handle(input);
    }
}
