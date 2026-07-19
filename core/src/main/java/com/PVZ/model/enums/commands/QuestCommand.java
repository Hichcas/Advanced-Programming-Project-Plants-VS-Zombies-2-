package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.QuestInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum QuestCommand {

    LIST("^\\s*quest\\s+list\\s*$") {
        @Override
        public QuestInputDTO createDTO(Matcher matcher) {
            return new QuestInputDTO(this, null, null);
        }
    },
    CLAIM("^\\s*quest\\s+claim\\s+-i\\s+(?<questId>\\S+)\\s*$") {
        @Override
        public QuestInputDTO createDTO(Matcher matcher) {
            return new QuestInputDTO(this, matcher.group("questId"), null);
        }
    },

    // ---------- Debug Commands ----------
    DEBUG_SUN("^\\s*quest\\s+debug\\s+sun\\s+(?<amount>\\d+)\\s*$") {
        @Override
        public QuestInputDTO createDTO(Matcher matcher) {
            return new QuestInputDTO(this, "sun", matcher.group("amount"));
        }
    },
    DEBUG_KILL("^\\s*quest\\s+debug\\s+kill\\s+(?<count>\\d+)\\s*$") {
        @Override
        public QuestInputDTO createDTO(Matcher matcher) {
            return new QuestInputDTO(this, "kill", matcher.group("count"));
        }
    },
    DEBUG_PLANT("^\\s*quest\\s+debug\\s+plant\\s+(?<plant>\\S+)\\s*$") {
        @Override
        public QuestInputDTO createDTO(Matcher matcher) {
            return new QuestInputDTO(this, "plant", matcher.group("plant"));
        }
    },
    DEBUG_WIN("^\\s*quest\\s+debug\\s+win\\s*$") {
        @Override
        public QuestInputDTO createDTO(Matcher matcher) {
            return new QuestInputDTO(this, "win", null);
        }
    },
    DEBUG_RESET_DAILY("^\\s*quest\\s+debug\\s+reset\\s+daily\\s*$") {
        @Override
        public QuestInputDTO createDTO(Matcher matcher) {
            return new QuestInputDTO(this, "reset_daily", null);
        }
    },

    // ---------- عمومی ----------
    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public QuestInputDTO createDTO(Matcher matcher) {
            return new QuestInputDTO(this, null, null);
        }
    },
    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public QuestInputDTO createDTO(Matcher matcher) {
            return new QuestInputDTO(this, null, null);
        }
    };

    private final Pattern pattern;

    QuestCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract QuestInputDTO createDTO(Matcher matcher);
}
