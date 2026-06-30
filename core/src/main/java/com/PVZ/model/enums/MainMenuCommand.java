package com.PVZ.model.enums;

import com.PVZ.view.input.DTO.MainMenuInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum MainMenuCommand {

    ENTER_MENU("^\\s*menu\\s+enter\\s+(?<menu>play|collection|settings|news|profile)\\s*$") {
        @Override
        public MainMenuInputDTO createDTO(Matcher matcher) {
            return new MainMenuInputDTO(this, matcher.group("menu"));
        }
    },

    LOGOUT("^\\s*menu\\s+logout\\s*$") {
        @Override
        public MainMenuInputDTO createDTO(Matcher matcher) {
            return new MainMenuInputDTO(this, null);
        }
    },

    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public MainMenuInputDTO createDTO(Matcher matcher) {
            return new MainMenuInputDTO(this, null);
        }
    },

    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public MainMenuInputDTO createDTO(Matcher matcher) {
            return new MainMenuInputDTO(this, null);
        }
    };

    private final Pattern pattern;

    MainMenuCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract MainMenuInputDTO createDTO(Matcher matcher);
}
