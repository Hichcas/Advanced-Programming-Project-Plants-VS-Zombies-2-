package model.menus;

import view.input.InputDTO;

public class NewsMenu extends Menu {
    @Override
    public InputDTO parseNextCommand (String command) {
        return view.input.inputCommandParsers.NewsMenuCommandParser.parseCommand(command);
    }
}
