package com.PVZ.model.menus;

import com.PVZ.controller.menuControllers.ChapterAndLevelSelectionMenuController;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.inputCommandParsers.ChapterAndLevelSelectionMenuCommandParser;
import com.PVZ.view.output.OutputDTO;

public class ChapterAndLevelSelectionMenu extends Menu {

    private final ChapterAndLevelSelectionMenuController controller = new ChapterAndLevelSelectionMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return ChapterAndLevelSelectionMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
