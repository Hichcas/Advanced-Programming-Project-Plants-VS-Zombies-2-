package model.menus;

import controller.menuControllers.TravelLogMenuController;
import view.input.InputDTO;
import view.output.OutputDTO;

public class TravelLogMenu extends Menu {
    @Override
    public InputDTO parseNextCommand(String command) {
        return view.input.inputCommandParsers.TravelLogMenuCommandParser.parseCommand(command);
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
      return  null;
    }


}
