package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.LoginInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum LoginCommand {

    LOGIN(
            "^\\s*login\\s+" +
                    "-u\\s+(?<username>\\S+)\\s+" +
                    "-p\\s+(?<password>\\S+)" +
                    "(?:\\s+(?<stay>-stay-logged-in))?\\s*$"
    ) {
        @Override
        public LoginInputDTO createDTO(Matcher matcher) {
            return new LoginInputDTO(
                    this,
                    matcher.group("username"),
                    matcher.group("password"),
                    matcher.group("stay") != null,
                    null,
                    null,
                    null
            );
        }
    },

    FORGET_PASSWORD(
            "^\\s*forget\\s+password\\s+" +
                    "-u\\s+(?<username>\\S+)\\s+" +
                    "-e\\s+(?<email>\\S+)\\s*$"
    ) {
        @Override
        public LoginInputDTO createDTO(Matcher matcher) {
            return new LoginInputDTO(
                    this,
                    matcher.group("username"),
                    null,
                    false,
                    matcher.group("email"),
                    null,
                    null
            );
        }
    },

    ANSWER(
            "^\\s*answer\\s+-a\\s+(?<answer>.+?)\\s*$"
    ) {
        @Override
        public LoginInputDTO createDTO(Matcher matcher) {
            return new LoginInputDTO(
                    this,
                    null,
                    null,
                    false,
                    null,
                    matcher.group("answer"),
                    null
            );
        }
    },

    NEW_PASSWORD(".*") {
        @Override
        public LoginInputDTO createDTO(Matcher matcher) {
            return new LoginInputDTO(this, null, null, false, null, null, matcher.group());
        }
    },

    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public LoginInputDTO createDTO(Matcher matcher) {
            return new LoginInputDTO(this, null, null, false, null, null, null);
        }
    },

    EXIT("^menu\\s+exit$") {
        @Override
        public LoginInputDTO createDTO(Matcher matcher) {
            return new LoginInputDTO(this, null, null, false, null, null, null);
        }
    };

    private final Pattern pattern;

    LoginCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract LoginInputDTO createDTO(Matcher matcher);
}
