package model.menus;

import controller.menuControllers.CollectionMenuController;
import view.input.InputDTO;
import view.output.OutputDTO;

public class CollectionMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.CollectionMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return null;
    }


}
