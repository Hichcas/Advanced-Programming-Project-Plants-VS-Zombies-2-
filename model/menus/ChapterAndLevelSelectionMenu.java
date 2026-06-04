package model.menus;

import view.input.InputDTO;

public class ChapterAndLevelSelectionMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.ChapterAndLevelSelectionMenuCommandParser.parseCommand(command);
    }
}
