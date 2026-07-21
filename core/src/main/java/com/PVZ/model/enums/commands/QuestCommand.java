package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.QuestInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum QuestCommand {

    LIST("^\\s*quest\\s+list\\s*$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, null, null);
        }
    },
    CLAIM("^\\s*quest\\s+claim\\s+-i\\s+(?<questId>\\S+)\\s*$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, m.group("questId"), null);
        }
    },

    // ==================== DEBUG COMMANDS ====================
    DEBUG_SUN("^\\s*quest\\s+debug\\s+sun\\s+(?<amount>\\d+)\\s*$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, "sun", m.group("amount"));
        }
    },
    DEBUG_KILL("^\\s*quest\\s+debug\\s+kill\\s+(?<count>\\d+)\\s*$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, "kill", m.group("count"));
        }
    },
    DEBUG_PLANT("^\\s*quest\\s+debug\\s+plant\\s+(?<plant>\\S+)\\s*$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, "plant", m.group("plant"));
        }
    },
    DEBUG_KILLBY("^\\s*quest\\s+debug\\s+killby\\s+(?<plant>\\S+)\\s+(?<count>\\d+)\\s*$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, "killby", m.group("plant") + " " + m.group("count"));
        }
    },
    DEBUG_SPEEDKILL("^\\s*quest\\s+debug\\s+speedkill\\s+(?<count>\\d+)\\s*$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, "speedkill", m.group("count"));
        }
    },
    DEBUG_LAWNMOWER("^\\s*quest\\s+debug\\s+lawnmower\\s+(?<count>\\d+)\\s*$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, "lawnmower", m.group("count"));
        }
    },
    DEBUG_WIN("^\\s*quest\\s+debug\\s+win\\s*(?<args>.*)$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, "win", m.group("args").trim());
        }
    },
    DEBUG_RESET_DAILY("^\\s*quest\\s+debug\\s+reset\\s+daily\\s*$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, "reset_daily", null);
        }
    },

    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, null, null);
        }
    },
    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override public QuestInputDTO createDTO(Matcher m) {
            return new QuestInputDTO(this, null, null);
        }
    };

    private final Pattern pattern;
    QuestCommand(String regex) { this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE); }
    public Matcher matcher(String input) { return pattern.matcher(input == null ? "" : input); }
    public abstract QuestInputDTO createDTO(Matcher matcher);
}
