package model.menus;

import controller.menuControllers.RegisterMenuController;
import view.input.InputDTO;
import view.input.inputCommandParsers.RegisterMenuCommandParser;
import view.output.OutputDTO;

public class RegisterMenu extends Menu {

    private final RegisterMenuController controller =
            new RegisterMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return RegisterMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }

}