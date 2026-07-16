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
