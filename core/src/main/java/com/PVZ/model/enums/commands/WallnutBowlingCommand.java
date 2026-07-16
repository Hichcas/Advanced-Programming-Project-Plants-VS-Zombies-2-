package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.WallnutBowlingInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum WallnutBowlingCommand {

    START_LEVEL("^\\s*wallnutbowling\\s+start\\s+(?<id>\\d+)\\s*$") {
        @Override
        public WallnutBowlingInputDTO createDTO(Matcher matcher) {
            return new WallnutBowlingInputDTO(this, Integer.parseInt(matcher.group("id")), -1, -1);
        }
    },

    LAUNCH("^\\s*wallnutbowling\\s+launch\\s+(?<row>\\d+)\\s+(?<col>\\d+)\\s*$") {
        @Override
        public WallnutBowlingInputDTO createDTO(Matcher matcher) {
            return new WallnutBowlingInputDTO(this, -1,
                    Integer.parseInt(matcher.group("row")),
                    Integer.parseInt(matcher.group("col")));
        }
    },

    NEXT_NUT("^\\s*wallnutbowling\\s+next\\s*$") {
        @Override
        public WallnutBowlingInputDTO createDTO(Matcher matcher) {
            return new WallnutBowlingInputDTO(this, -1, -1, -1);
        }
    },

    SHOW_BOARD("^\\s*wallnutbowling\\s+show\\s*$") {
        @Override
        public WallnutBowlingInputDTO createDTO(Matcher matcher) {
            return new WallnutBowlingInputDTO(this, -1, -1, -1);
        }
    },

    EXIT("^\\s*wallnutbowling\\s+exit\\s*$") {
        @Override
        public WallnutBowlingInputDTO createDTO(Matcher matcher) {
            return new WallnutBowlingInputDTO(this, -1, -1, -1);
        }
    };

    private final Pattern pattern;

    WallnutBowlingCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract WallnutBowlingInputDTO createDTO(Matcher matcher);
}
