package model.menus;

import view.input.InputDTO;

public abstract class Menu {
    public abstract InputDTO parseNextCommand (String command);
}
