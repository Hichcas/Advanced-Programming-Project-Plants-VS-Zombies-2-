package model.menus;

import view.input.InputDTO;

public class LeaderboardMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.LeaderboardMenuCommandParser.parseCommand(command);
    }
}
