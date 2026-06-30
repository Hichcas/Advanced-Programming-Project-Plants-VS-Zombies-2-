package model.menus;

import controller.menuControllers.MainMenuController;
import view.input.InputDTO;
import view.input.inputCommandParsers.MainMenuCommandParser;
import view.output.OutputDTO;

public class MainMenu extends Menu {

    private final MainMenuController controller = new MainMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return MainMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
