package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.WallnutBowlingMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.WallnutBowlingMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class WallnutBowlingMenu extends Menu {

    private final WallnutBowlingMenuController controller = new WallnutBowlingMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return WallnutBowlingMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
