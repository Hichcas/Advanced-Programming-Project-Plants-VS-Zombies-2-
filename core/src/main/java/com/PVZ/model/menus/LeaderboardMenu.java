package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.LeaderboardMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.LeaderboardMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class LeaderboardMenu extends Menu {

    private final LeaderboardMenuController controller = new LeaderboardMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return LeaderboardMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
