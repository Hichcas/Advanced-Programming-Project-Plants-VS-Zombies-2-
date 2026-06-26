package view.input.inputCommandParsers;

import model.enums.RegisterCommand;
import view.input.DTO.RegisterInputDTO;

import java.util.regex.Matcher;

public class RegisterMenuCommandParser {

    private RegisterMenuCommandParser() {
    }

    public static RegisterInputDTO parseCommand(String input) {

        for (RegisterCommand command : RegisterCommand.values()) {

            Matcher matcher = command.matcher(input);

            if (matcher.matches()) {
                return command.createDTO(matcher);
            }
        }

        return RegisterInputDTO.invalid();
    }

}