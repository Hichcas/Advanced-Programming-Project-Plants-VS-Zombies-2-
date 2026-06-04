package model.menus;

import view.input.InputDTO;

public class RegisterMenu extends Menu {
    @Override
    public InputDTO parseNextCommand (String command) {
        return view.input.inputCommandParsers.RegisterMenuCommandParser.parseCommand(command);
    }
}
