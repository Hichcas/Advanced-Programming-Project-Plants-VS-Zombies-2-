package model.menus;

import controller.menuControllers.ChapterAndLevelSelectionMenuController;
import view.input.InputDTO;

public class ChapterAndLevelSelectionMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.ChapterAndLevelSelectionMenuCommandParser.parseCommand(command);
    }

     @Override
    public void handleInput(InputDTO input) {
        ChapterAndLevelSelectionMenuController.handle(input);
    }
}
