package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.IZombieInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum IZombieCommand {

    START_LEVEL("^\\s*izombie\\s+start\\s+(?<id>\\d+)\\s*$") {
        @Override
        public IZombieInputDTO createDTO(Matcher matcher) {
            return new IZombieInputDTO(this, Integer.parseInt(matcher.group("id")), -1, -1, null);
        }
    },

    DEPLOY("^\\s*izombie\\s+deploy\\s+(?<alias>[A-Za-z0-9_]+)\\s+(?<row>\\d+)\\s+(?<col>\\d+)\\s*$") {
        @Override
        public IZombieInputDTO createDTO(Matcher matcher) {
            return new IZombieInputDTO(this, -1,
                    Integer.parseInt(matcher.group("row")),
                    Integer.parseInt(matcher.group("col")),
                    matcher.group("alias"));
        }
    },

    SHOW_ROSTER("^\\s*izombie\\s+roster\\s*$") {
        @Override
        public IZombieInputDTO createDTO(Matcher matcher) {
            return new IZombieInputDTO(this, -1, -1, -1, null);
        }
    },

    SHOW_BOARD("^\\s*izombie\\s+show\\s*$") {
        @Override
        public IZombieInputDTO createDTO(Matcher matcher) {
            return new IZombieInputDTO(this, -1, -1, -1, null);
        }
    },

    EXIT("^\\s*izombie\\s+exit\\s*$") {
        @Override
        public IZombieInputDTO createDTO(Matcher matcher) {
            return new IZombieInputDTO(this, -1, -1, -1, null);
        }
    },

    CHEAT_ADD_SUNS("^\\s*(?:izombie\\s+)?cheat\\s+add\\s+-n\\s+(?<amount>\\d+)\\s+suns?\\s*$") {
        @Override
        public IZombieInputDTO createDTO(Matcher matcher) {
            return new IZombieInputDTO(this, -1, -1, -1, null, Integer.parseInt(matcher.group("amount")));
        }
    };

    private final Pattern pattern;

    IZombieCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract IZombieInputDTO createDTO(Matcher matcher);
}
