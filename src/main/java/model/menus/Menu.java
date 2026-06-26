package model.menus;

import view.input.InputDTO;
import view.output.OutputDTO;

public abstract class Menu {

    public abstract InputDTO parseNextCommand(String command);

    public abstract OutputDTO handleInput(InputDTO input);

}