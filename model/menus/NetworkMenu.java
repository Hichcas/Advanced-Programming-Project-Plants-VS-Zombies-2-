package model.menus;

import controller.menuControllers.NetworkMenuController;
import view.input.InputDTO;

public class NetworkMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.NetworkMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        NetworkMenuController.handle(input);
    }
}
