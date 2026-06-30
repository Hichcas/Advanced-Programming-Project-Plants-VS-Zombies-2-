package model.menus;

import controller.menuControllers.ProfileMenuController;
import view.input.InputDTO;
import view.input.inputCommandParsers.ProfileMenuCommandParser;
import view.output.OutputDTO;

public class ProfileMenu extends Menu {

    private final ProfileMenuController controller = new ProfileMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return ProfileMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
