package view.input.inputCommandParsers;

import model.enums.NewsCommand;
import view.input.DTO.NewsInputDTO;
import view.input.InputDTO;

public class NewsMenuCommandParser {
    private NewsMenuCommandParser() {
    }

    public static InputDTO parseCommand(String command) {
        if (command == null) {
            return NewsInputDTO.invalid();
        }

        for (NewsCommand newsCommand : NewsCommand.values()) {
            if (newsCommand.matcher(command).matches()) {
                return newsCommand.createDTO(newsCommand.matcher(command));
            }
        }

        return NewsInputDTO.invalid();
    }
}
