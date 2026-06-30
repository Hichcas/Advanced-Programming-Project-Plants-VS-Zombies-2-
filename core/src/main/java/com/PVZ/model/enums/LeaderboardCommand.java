package com.PVZ.model.enums;

import com.PVZ.view.input.DTO.LeaderboardInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum LeaderboardCommand {

    SHOW_LEADERBOARD("^\\s*show\\s+leaderboard\\s*$") {
        @Override
        public LeaderboardInputDTO createDTO(Matcher matcher) {
            return new LeaderboardInputDTO(this);
        }
    },
    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public LeaderboardInputDTO createDTO(Matcher matcher) {
            return new LeaderboardInputDTO(this);
        }
    },
    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public LeaderboardInputDTO createDTO(Matcher matcher) {
            return new LeaderboardInputDTO(this);
        }
    };

    private final Pattern pattern;

    LeaderboardCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract LeaderboardInputDTO createDTO(Matcher matcher);
}
