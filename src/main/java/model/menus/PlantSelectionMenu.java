package model.menus;

import controller.menuControllers.PlantSelectionMenuController;
import view.input.InputDTO;
import view.output.OutputDTO;

public class PlantSelectionMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.PlantSelectionMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return null;
    }


}
