package com.PVZ.model.menus;

import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class MinigameSelectionMenu extends Menu {

    @Override
    public InputDTO parseNextCommand(String command) {
        return null; // GUI-only menu
    }

    @Override
    public OutputDTO handleInput(InputDTO input) {
        return new OutputDTO(true, "Minigame selection is available in the GUI.");
    }
}
