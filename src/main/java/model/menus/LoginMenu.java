package model.menus;

import view.input.InputDTO;

public class LoginMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.LoginMenuCommandParser.parseCommand(command);
    }

    @Override
    public void handleInput(InputDTO input){
        controller.menuControllers.LoginMenuController.handle(input);
    }

}
