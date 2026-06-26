package model.enums;

import view.input.DTO.RegisterInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum RegisterCommand {

    REGISTER(
            "^\\s*register\\s+" +
                    "-u\\s+(?<username>\\S+)\\s+" +
                    "-p\\s+(?<password>\\S+)\\s+(?<confirmPassword>\\S+)\\s+" +
                    "-n\\s+(?<nickname>\\S+)\\s+" +
                    "-e\\s+(?<email>\\S+)\\s+" +
                    "-g\\s+(?<gender>male|female)\\s*$"
    ) {
        @Override
        public RegisterInputDTO createDTO(Matcher matcher) {

            return new RegisterInputDTO(
                    this,
                    matcher.group("username"),
                    matcher.group("password"),
                    matcher.group("confirmPassword"),
                    matcher.group("nickname"),
                    matcher.group("email"),
                    matcher.group("gender"),
                    null,
                    null,
                    null
            );
        }
    },

    PICK_QUESTION(
            "^\\s*pick\\s+question\\s+" +
                    "-q\\s+(?<question>\\d+)\\s+" +
                    "-a\\s+(?<answer>.+?)\\s+" +
                    "-c\\s+(?<confirm>.+?)\\s*$"
    ) {
        @Override
        public RegisterInputDTO createDTO(Matcher matcher) {
            return new RegisterInputDTO(
                    this,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Integer.parseInt(matcher.group("question")),
                    matcher.group("answer"),
                    matcher.group("confirm")
            );
        }
    },

    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public RegisterInputDTO createDTO(Matcher matcher) {
            return new RegisterInputDTO(this, null, null, null, null, null, null, null, null, null);
        }
    },

    ENTER_LOGIN("^menu\\s+enter\\s+login$") {
        @Override
        public RegisterInputDTO createDTO(Matcher matcher) {
            return new RegisterInputDTO(this, null, null, null, null, null, null, null, null, null);
        }
    },

    EXIT("^menu\\s+exit$") {
        @Override
        public RegisterInputDTO createDTO(Matcher matcher) {
            return new RegisterInputDTO(this, null, null, null, null, null, null, null, null, null);
        }
    };

    private final Pattern pattern;

    RegisterCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input);
    }

    public abstract RegisterInputDTO createDTO(Matcher matcher);

}
