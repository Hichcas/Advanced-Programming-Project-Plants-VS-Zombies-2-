package model.menus;

import controller.menuControllers.NewsMenuController;
import view.input.InputDTO;
import view.input.inputCommandParsers.NewsMenuCommandParser;
import view.output.OutputDTO;

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
