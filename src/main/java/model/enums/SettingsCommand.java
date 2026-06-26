package model.enums;

import view.input.DTO.SettingsInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SettingsCommand {

    CHANGE_DIFFICULTY(
            "^\s*(?:menu\s+settings\s+)?change-difficulty\s+-l\s+(?<level>\\d+)\s*$"
    ) {
        @Override
        public SettingsInputDTO createDTO(Matcher matcher) {
            return new SettingsInputDTO(this, Integer.parseInt(matcher.group("level")));
        }
    },

    SHOW_CURRENT_MENU("^\s*menu\s+show\s+current\s*$|^\s*show\s+current\s+menu\s*$") {
        @Override
        public SettingsInputDTO createDTO(Matcher matcher) {
            return new SettingsInputDTO(this, null);
        }
    },

    EXIT("^\s*menu\s+exit\s*$") {
        @Override
        public SettingsInputDTO createDTO(Matcher matcher) {
            return new SettingsInputDTO(this, null);
        }
    };

    private final Pattern pattern;

    SettingsCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract SettingsInputDTO createDTO(Matcher matcher);
}
