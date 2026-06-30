package model.menus;

import controller.menuControllers.NetworkMenuController;
import view.input.InputDTO;
import view.output.OutputDTO;

public class NetworkMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.NetworkMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return null;
    }


}
