package model.menus;

import view.input.InputDTO;

public class ShopMenu extends Menu {
    @Override
    public InputDTO parseNextCommand (String command) {
        return view.input.inputCommandParsers.ShopMenuCommandParser.parseCommand(command);
    }
}
