package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.VasebreakerMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.VasebreakerMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class VasebreakerMenu extends Menu {

    private final VasebreakerMenuController controller = new VasebreakerMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return VasebreakerMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
