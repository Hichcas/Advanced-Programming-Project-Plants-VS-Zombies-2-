package model.menus;

import controller.menuControllers.GreenhouseMenuController;
import view.input.InputDTO;
import view.output.OutputDTO;

public class GreenhouseMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.GreenhouseMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return null;
    }

}
