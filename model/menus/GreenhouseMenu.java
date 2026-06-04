package model.menus;

import view.input.InputDTO;

public class GreenhouseMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.GreenhouseMenuCommandParser.parseCommand(command);
    }
}
