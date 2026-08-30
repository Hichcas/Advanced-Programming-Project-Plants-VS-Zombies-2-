package com.PVZ.model.menus;

import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class OnlineLeaderboardMenu extends Menu {

    @Override
    public InputDTO parseNextCommand(String command) {
        return null;
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return new OutputDTO(true, "");
    }
}
