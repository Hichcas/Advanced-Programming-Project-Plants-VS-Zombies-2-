package model.menus;

import view.input.InputDTO;

public class InGameMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.InGameMenuCommandParser.parseCommand(command);
    }
}
