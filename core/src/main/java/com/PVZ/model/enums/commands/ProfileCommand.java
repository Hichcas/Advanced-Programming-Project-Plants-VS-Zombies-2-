package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.ProfileInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ProfileCommand {

    CHANGE_USERNAME("^\\s*menu\\s+profile\\s+change-username\\s+-u\\s+(?<username>\\S+)\\s*$") {
        @Override
        public ProfileInputDTO createDTO(Matcher matcher) {
            return new ProfileInputDTO(this, matcher.group("username"), null, null, null, null);
        }
    },

    CHANGE_NICKNAME("^\\s*menu\\s+profile\\s+change-nickname\\s+-u\\s+(?<nickname>\\S+)\\s*$") {
        @Override
        public ProfileInputDTO createDTO(Matcher matcher) {
            return new ProfileInputDTO(this, null, matcher.group("nickname"), null, null, null);
        }
    },

    CHANGE_EMAIL("^\\s*menu\\s+profile\\s+change-email\\s+-e\\s+(?<email>\\S+)\\s*$") {
        @Override
        public ProfileInputDTO createDTO(Matcher matcher) {
            return new ProfileInputDTO(this, null, null, matcher.group("email"), null, null);
        }
    },

    CHANGE_PASSWORD(
            "^\\s*menu\\s+profile\\s+change-password\\s+-p\\s+(?<newPassword>\\S+)\\s+-o\\s+(?<oldPassword>\\S+)\\s*$")
            {
        @Override
        public ProfileInputDTO createDTO(Matcher matcher) {
            return new ProfileInputDTO(
                    this,
                    null,
                    null,
                    null,
                    matcher.group("newPassword"),
                    matcher.group("oldPassword")
            );
        }
    },

    SHOW_INFO("^\\s*menu\\s+profile\\s+show-info\\s*$") {
        @Override
        public ProfileInputDTO createDTO(Matcher matcher) {
            return new ProfileInputDTO(this, null, null, null, null, null);
        }
    },

    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public ProfileInputDTO createDTO(Matcher matcher) {
            return new ProfileInputDTO(this, null, null, null, null, null);
        }
    },

    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public ProfileInputDTO createDTO(Matcher matcher) {
            return new ProfileInputDTO(this, null, null, null, null, null);
        }
    };

    private final Pattern pattern;

    ProfileCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract ProfileInputDTO createDTO(Matcher matcher);
}
