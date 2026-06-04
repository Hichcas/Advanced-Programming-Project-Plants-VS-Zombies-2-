package model.menus;

import view.input.InputDTO;

public class MainMenu extends Menu {
    @Override
    public InputDTO parseNextCommand (String command) {
        return view.input.inputCommandParsers.MainMenuCommandParser.parseCommand(command);
    }

}
