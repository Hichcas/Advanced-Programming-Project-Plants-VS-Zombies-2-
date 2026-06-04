package model.menus;

import view.input.InputDTO;

public class PlantSelectionMenu extends Menu {
    @Override
    public InputDTO parseNextCommand (String command) {
        return view.input.inputCommandParsers.PlantSelectionMenuCommandParser.parseCommand(command);
    }
}
