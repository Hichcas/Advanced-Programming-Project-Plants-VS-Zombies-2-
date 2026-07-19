package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.IZombieMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.IZombieMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class IZombieMenu extends Menu {

    private final IZombieMenuController controller = new IZombieMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return IZombieMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
