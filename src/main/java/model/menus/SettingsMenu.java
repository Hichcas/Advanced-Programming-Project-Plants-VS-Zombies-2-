package model.menus;

import controller.menuControllers.SettingsMenuController;
import view.input.InputDTO;
import view.input.inputCommandParsers.SettingsMenuCommandParser;
import view.output.OutputDTO;

public class SettingsMenu extends Menu {

    private final SettingsMenuController controller = new SettingsMenuController();

    @Override
    public InputDTO parseNextCommand(String command) {
        return SettingsMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return controller.handle(input);
    }
}
