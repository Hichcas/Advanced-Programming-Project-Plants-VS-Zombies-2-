package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.ChapterAndLevelSelectionInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ChapterAndLevelSelectionCommand {

    ENTER_CHAPTER("^\\s*menu\\s+enter\\s+chapter\\s+-c\\s+(?<chapterName>[A-Za-z0-9_-]+)(\\s+-s\\s+(?<stage>\\d+))?\\s*$") {
        @Override
        public ChapterAndLevelSelectionInputDTO createDTO(Matcher matcher) {
            String stageStr = matcher.group("stage");
            Integer stage = stageStr != null ? Integer.parseInt(stageStr) : null;
            return new ChapterAndLevelSelectionInputDTO(this, matcher.group("chapterName"), null, null, stage);
        }
    },

    ENTER_COLLECTION("^\\s*menu\\s+enter\\s+collection\\s*$|^\\s*enter\\s+menu\\s*$") {
        @Override
        public ChapterAndLevelSelectionInputDTO createDTO(Matcher matcher) {
            return new ChapterAndLevelSelectionInputDTO(this, null, null, null, null);
        }
    },

    ENTER_GREENHOUSE("^\\s*menu\\s+greenhouse\\s*$") {
        @Override
        public ChapterAndLevelSelectionInputDTO createDTO(Matcher matcher) {
            return new ChapterAndLevelSelectionInputDTO(this, null, null, null, null);
        }
    },

    ENTER_TRAVEL_LOG("^\\s*menu\\s+travel-log\\s*$") {
        @Override
        public ChapterAndLevelSelectionInputDTO createDTO(Matcher matcher) {
            return new ChapterAndLevelSelectionInputDTO(this, null, null, null, null);
        }
    },

    ENTER_LEADERBOARD("^\\s*menu\\s+leaderboard\\s*$") {
        @Override
        public ChapterAndLevelSelectionInputDTO createDTO(Matcher matcher) {
            return new ChapterAndLevelSelectionInputDTO(this, null, null, null, null);
        }
    },

    SHOW_COIN_WALLET("^\\s*menu\\s+coin-wallet\\s*$") {
        @Override
        public ChapterAndLevelSelectionInputDTO createDTO(Matcher matcher) {
            return new ChapterAndLevelSelectionInputDTO(this, null, null, null, null);
        }
    },

    SHOW_GEM_WALLET("^\\s*menu\\s+gem-wallet\\s*$") {
        @Override
        public ChapterAndLevelSelectionInputDTO createDTO(Matcher matcher) {
            return new ChapterAndLevelSelectionInputDTO(this, null, null, null, null);
        }
    },

    CHEAT_ADD("^\\s*menu\\s+cheat\\s+add\\s+(?<amount>\\d+)\\s+(?<currency>coin|diamond)s?\\s*$") {
        @Override
        public ChapterAndLevelSelectionInputDTO createDTO(Matcher matcher) {
            return new ChapterAndLevelSelectionInputDTO(
                    this,
                    null,
                    Integer.parseInt(matcher.group("amount")),
                    matcher.group("currency"),
                    null);
        }
    },

    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public ChapterAndLevelSelectionInputDTO createDTO(Matcher matcher) {
            return new ChapterAndLevelSelectionInputDTO(this, null, null, null, null);
        }
    },

    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public ChapterAndLevelSelectionInputDTO createDTO(Matcher matcher) {
            return new ChapterAndLevelSelectionInputDTO(this, null, null, null, null);
        }
    };

    private final Pattern pattern;

    ChapterAndLevelSelectionCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract ChapterAndLevelSelectionInputDTO createDTO(Matcher matcher);
}
