package model.menus;

import view.input.InputDTO;

public class SettingsMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.SettingsMenuCommandParser.parseCommand(command);
    }
}
