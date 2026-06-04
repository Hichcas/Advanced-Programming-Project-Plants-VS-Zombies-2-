package model.menus;

import view.input.InputDTO;

public class CollectionMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.CollectionMenuCommandParser.parseCommand(command);
    }
}
