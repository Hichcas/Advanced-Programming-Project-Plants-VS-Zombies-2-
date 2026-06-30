package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.NewsMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.NewsMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class NewsMenu extends Menu {

    private final NewsMenuController controller = new NewsMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return NewsMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
