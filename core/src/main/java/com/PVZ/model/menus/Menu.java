package com.PVZ.model.menus;

import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public abstract class Menu {

    public abstract InputDTO parseNextCommand(String command);

    public abstract OutputDTO handleInput(InputDTO input);

}
