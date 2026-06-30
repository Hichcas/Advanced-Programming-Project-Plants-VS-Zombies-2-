package model.menus;

import controller.menuControllers.LeaderboardMenuController;
import view.input.InputDTO;
import view.output.OutputDTO;

public class LeaderboardMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.LeaderboardMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return null;
    }


}
