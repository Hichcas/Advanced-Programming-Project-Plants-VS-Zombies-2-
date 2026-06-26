package view.input.DTO;

import model.enums.MainMenuCommand;
import view.input.InputDTO;

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
