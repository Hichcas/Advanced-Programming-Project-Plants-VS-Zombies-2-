package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.MainMenuCommand;
import com.PVZ.view.input.InputDTO;

public class MainMenuInputDTO implements InputDTO {

    private final MainMenuCommand command;
    private final String menuName;

    public MainMenuInputDTO(MainMenuCommand command, String menuName) {
        this.command = command;
        this.menuName = menuName;
    }

    public static MainMenuInputDTO invalid() {
        return new MainMenuInputDTO(null, null);
    }

    public MainMenuCommand getCommand() {
        return command;
    }

    public String getMenuName() {
        return menuName;
    }
}
