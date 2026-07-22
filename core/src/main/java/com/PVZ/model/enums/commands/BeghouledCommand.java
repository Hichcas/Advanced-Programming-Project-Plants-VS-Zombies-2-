package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.BeghouledInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum BeghouledCommand {

    START_LEVEL("^\\s*beghouled\\s+start\\s+(?<id>\\d+)\\s*$") {
        @Override
        public BeghouledInputDTO createDTO(Matcher matcher) {
            return new BeghouledInputDTO(this, Integer.parseInt(matcher.group("id")));
        }
    },

    SELECT_LEVEL("^\\s*beghouled\\s+select\\s+(?<id>\\d+)\\s*$") {
        @Override
        public BeghouledInputDTO createDTO(Matcher matcher) {
            return new BeghouledInputDTO(this, Integer.parseInt(matcher.group("id")));
        }
    },

    SHOW_LEVELS("^\\s*beghouled\\s+levels\\s*$") {
        @Override
        public BeghouledInputDTO createDTO(Matcher matcher) {
            return new BeghouledInputDTO(this, -1);
        }
    },

    SHOW_BOARD("^\\s*beghouled\\s+show\\s*$") {
        @Override
        public BeghouledInputDTO createDTO(Matcher matcher) {
            return new BeghouledInputDTO(this, -1);
        }
    },

    EXIT("^\\s*(?:beghouled\\s+exit|menu\\s+exit)\\s*$") {
        @Override
        public BeghouledInputDTO createDTO(Matcher matcher) {
            return new BeghouledInputDTO(this, -1);
        }
    };

    private final Pattern pattern;

    BeghouledCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract BeghouledInputDTO createDTO(Matcher matcher);
}
