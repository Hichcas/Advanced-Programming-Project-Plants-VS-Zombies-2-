package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.ZombotanyInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ZombotanyCommand {

    START_LEVEL("^\\s*zombotany\\s+start\\s+(?<id>\\d+)\\s*$") {
        @Override
        public ZombotanyInputDTO createDTO(Matcher matcher) {
            return new ZombotanyInputDTO(this, Integer.parseInt(matcher.group("id")));
        }
    },

    SELECT_LEVEL("^\\s*zombotany\\s+select\\s+(?<id>\\d+)\\s*$") {
        @Override
        public ZombotanyInputDTO createDTO(Matcher matcher) {
            return new ZombotanyInputDTO(this, Integer.parseInt(matcher.group("id")));
        }
    },

    SHOW_LEVELS("^\\s*zombotany\\s+levels\\s*$") {
        @Override
        public ZombotanyInputDTO createDTO(Matcher matcher) {
            return new ZombotanyInputDTO(this, -1);
        }
    },

    SHOW_BOARD("^\\s*zombotany\\s+show\\s*$") {
        @Override
        public ZombotanyInputDTO createDTO(Matcher matcher) {
            return new ZombotanyInputDTO(this, -1);
        }
    },

    EXIT("^\\s*(?:zombotany\\s+exit|menu\\s+exit)\\s*$") {
        @Override
        public ZombotanyInputDTO createDTO(Matcher matcher) {
            return new ZombotanyInputDTO(this, -1);
        }
    };

    private final Pattern pattern;

    ZombotanyCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract ZombotanyInputDTO createDTO(Matcher matcher);
}
