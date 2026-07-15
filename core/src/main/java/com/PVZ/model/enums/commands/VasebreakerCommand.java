package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.VasebreakerInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum VasebreakerCommand {

    START_LEVEL("^\\s*vasebreaker\\s+start\\s+(?<id>\\d+)\\s*$") {
        @Override
        public VasebreakerInputDTO createDTO(Matcher matcher) {
            return new VasebreakerInputDTO(this, Integer.parseInt(matcher.group("id")), -1, -1);
        }
    },

    BREAK_VASE("^\\s*vasebreaker\\s+break\\s+(?<row>\\d+)\\s+(?<col>\\d+)\\s*$") {
        @Override
        public VasebreakerInputDTO createDTO(Matcher matcher) {
            return new VasebreakerInputDTO(this, -1,
                    Integer.parseInt(matcher.group("row")),
                    Integer.parseInt(matcher.group("col")));
        }
    },

    PLANT_AT("^\\s*vasebreaker\\s+plant\\s+(?<row>\\d+)\\s+(?<col>\\d+)\\s*$") {
        @Override
        public VasebreakerInputDTO createDTO(Matcher matcher) {
            return new VasebreakerInputDTO(this, -1,
                    Integer.parseInt(matcher.group("row")),
                    Integer.parseInt(matcher.group("col")));
        }
    },

    SHOW_BOARD("^\\s*vasebreaker\\s+show\\s*$") {
        @Override
        public VasebreakerInputDTO createDTO(Matcher matcher) {
            return new VasebreakerInputDTO(this, -1, -1, -1);
        }
    },

    EXIT("^\\s*vasebreaker\\s+exit\\s*$") {
        @Override
        public VasebreakerInputDTO createDTO(Matcher matcher) {
            return new VasebreakerInputDTO(this, -1, -1, -1);
        }
    };

    private final Pattern pattern;

    VasebreakerCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract VasebreakerInputDTO createDTO(Matcher matcher);
}
