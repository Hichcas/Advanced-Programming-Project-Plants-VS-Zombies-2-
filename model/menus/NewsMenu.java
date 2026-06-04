package model.menus;

import controller.menuControllers.NewsMenuController;
import view.input.InputDTO;

public class NewsMenu extends Menu {
    @Override
    public InputDTO parseNextCommand (String command) {
        return view.input.inputCommandParsers.NewsMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        NewsMenuController.handle(input);
    }
}
