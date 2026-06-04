package model.menus;

import controller.menuControllers.ShopMenuController;
import view.input.InputDTO;

public class ShopMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.ShopMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        ShopMenuController.handle(input);
    }
}
