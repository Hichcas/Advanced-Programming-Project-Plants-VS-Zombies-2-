package com.PVZ.model.enums;

import com.PVZ.view.input.DTO.NewsInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum NewsCommand {

    SHOW_UNREAD("^\\s*(?:menu\\s+news\\s+)?show-unread\\s*$") {
        @Override
        public NewsInputDTO createDTO(Matcher matcher) {
            return new NewsInputDTO(this);
        }
    },

    SHOW_ALL("^\\s*(?:menu\\s+news\\s+)?show-all\\s*$") {
        @Override
        public NewsInputDTO createDTO(Matcher matcher) {
            return new NewsInputDTO(this);
        }
    },

    SHOW_CURRENT_MENU("^\\s*menu\\s+show\\s+current\\s*$|^\\s*show\\s+current\\s+menu\\s*$") {
        @Override
        public NewsInputDTO createDTO(Matcher matcher) {
            return new NewsInputDTO(this);
        }
    },

    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public NewsInputDTO createDTO(Matcher matcher) {
            return new NewsInputDTO(this);
        }
    };

    private final Pattern pattern;

    NewsCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract NewsInputDTO createDTO(Matcher matcher);
}
