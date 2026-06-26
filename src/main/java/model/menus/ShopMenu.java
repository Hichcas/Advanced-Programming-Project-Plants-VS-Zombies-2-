package model.menus;

import controller.menuControllers.ShopMenuController;
import view.input.InputDTO;
import view.output.OutputDTO;

public class ShopMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.ShopMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return null;
    }

}
