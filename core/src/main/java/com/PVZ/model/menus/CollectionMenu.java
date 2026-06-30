package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.CollectionMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.CollectionMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class CollectionMenu extends Menu {

    private final CollectionMenuController controller = new CollectionMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return CollectionMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
