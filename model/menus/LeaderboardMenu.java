package model.menus;

import controller.menuControllers.LeaderboardMenuController;
import view.input.InputDTO;

public class LeaderboardMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.LeaderboardMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input) {
        LeaderboardMenuController.handle(input);
    }
}
