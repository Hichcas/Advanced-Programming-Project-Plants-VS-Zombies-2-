package model.menus;

import controller.menuControllers.PlantSelectionMenuController;
import view.input.InputDTO;

public class PlantSelectionMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.PlantSelectionMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        PlantSelectionMenuController.handle(input);
    }

}
