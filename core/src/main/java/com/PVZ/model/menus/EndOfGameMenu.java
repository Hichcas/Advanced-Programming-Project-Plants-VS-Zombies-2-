package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.EndOfGameMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.EndOfGameCommandParser;
import com.PVZ.view.output.OutputDTO;

public class EndOfGameMenu extends Menu {

    private final EndOfGameMenuController controller = new EndOfGameMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return EndOfGameCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
