package model.menus;

import controller.menuControllers.CollectionMenuController;
import view.input.InputDTO;

public class CollectionMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.CollectionMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        CollectionMenuController.handle(input);
    }
}
