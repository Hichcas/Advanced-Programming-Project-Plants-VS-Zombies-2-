package com.PVZ.model.enums.commands;

import com.PVZ.model.leaderboard.LeaderboardSortField;
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
    SHOW_LEADERBOARD_SORTED(
        "^\\s*show\\s+leaderboard\\s+-s\\s+(?<field>score|stages|minigames|daily|nondaily)\\s+(?<order>asc|desc)\\s*$"
    ) {
        @Override
        public LeaderboardInputDTO createDTO(Matcher matcher) {
            LeaderboardSortField field = switch (matcher.group("field")) {
                case "score" -> LeaderboardSortField.HIGHEST_SCORE;
                case "stages" -> LeaderboardSortField.LAST_STAGE;
                case "minigames" -> LeaderboardSortField.MINIGAMES;
                case "daily" -> LeaderboardSortField.DAILY_QUESTS;
                case "nondaily" -> LeaderboardSortField.NON_DAILY_QUESTS;
                default -> LeaderboardSortField.USERNAME;
            };
            boolean asc = matcher.group("order").equals("asc");
            return new LeaderboardInputDTO(this, field, asc);
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
