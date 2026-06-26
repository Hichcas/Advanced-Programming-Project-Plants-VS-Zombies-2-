package model.menus;

import controller.menuControllers.InGameMenuController;
import view.input.InputDTO;
import view.output.OutputDTO;

public class InGameMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.InGameMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return null;
    }

}
