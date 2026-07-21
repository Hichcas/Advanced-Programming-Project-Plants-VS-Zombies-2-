package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.QuestMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.QuestMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class QuestMenu extends Menu {

    private final QuestMenuController controller = new QuestMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return QuestMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
