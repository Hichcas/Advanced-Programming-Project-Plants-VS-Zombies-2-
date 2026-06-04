package model.menus;

import view.input.InputDTO;

public class TravelLogMenu extends Menu {
    @Override
    public InputDTO parseNextCommand (String command) {
        return view.input.inputCommandParsers.TravelLogMenuCommandParser.parseCommand(command);
    }
}
