package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.EndOfGameInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum EndOfGameCommand {

    SHOW_STATS("^\\s*show\\s+stats\\s*$") {
        @Override
        public EndOfGameInputDTO createDTO(Matcher matcher) {
            return new EndOfGameInputDTO(this);
        }
    },

    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public EndOfGameInputDTO createDTO(Matcher matcher) {
            return new EndOfGameInputDTO(this);
        }
    },

    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public EndOfGameInputDTO createDTO(Matcher matcher) {
            return new EndOfGameInputDTO(this);
        }
    };

    private final Pattern pattern;

    EndOfGameCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract EndOfGameInputDTO createDTO(Matcher matcher);
}
