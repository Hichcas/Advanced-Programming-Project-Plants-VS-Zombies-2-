package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.ShopMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.ShopMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class ShopMenu extends Menu {

    private final ShopMenuController controller = new ShopMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return ShopMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
