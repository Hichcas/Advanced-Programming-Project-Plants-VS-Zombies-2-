package model.menus;

import view.input.InputDTO;

public class NetworkMenu extends Menu {
    @Override
    public InputDTO parseNextCommand (String command) {
        return view.input.inputCommandParsers.NetworkMenuCommandParser.parseCommand(command);
    }
}
