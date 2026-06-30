package model.menus;

import controller.menuControllers.LoginMenuController;
import view.input.InputDTO;
import view.input.inputCommandParsers.LoginMenuCommandParser;
import view.output.OutputDTO;

public class LoginMenu extends Menu {

    private final LoginMenuController controller = new LoginMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return LoginMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
