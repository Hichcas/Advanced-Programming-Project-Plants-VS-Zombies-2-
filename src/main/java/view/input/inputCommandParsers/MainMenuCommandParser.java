package view.input.inputCommandParsers;

import model.enums.MainMenuCommand;
import view.input.DTO.MainMenuInputDTO;
import view.input.InputDTO;

import java.util.regex.Matcher;

public class MainMenuCommandParser {

    private MainMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        for (MainMenuCommand mainMenuCommand : MainMenuCommand.values()) {
            Matcher matcher = mainMenuCommand.matcher(command);
            if (matcher.matches()) {
                return mainMenuCommand.createDTO(matcher);
            }
        }
        return MainMenuInputDTO.invalid();
    }
}
