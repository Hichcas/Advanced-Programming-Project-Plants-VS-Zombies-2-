package com.PVZ.model.enums;

import com.PVZ.view.input.DTO.NetworkInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum NetworkCommand {

    SHOW_CURRENT_MENU("^\\s*menu\\s+show\\s+current\\s*$|^\\s*show\\s+current\\s+menu\\s*$") {
        @Override
        public NetworkInputDTO createDTO(Matcher matcher) {
            return new NetworkInputDTO(this);
        }
    },

    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public NetworkInputDTO createDTO(Matcher matcher) {
            return new NetworkInputDTO(this);
        }
    };

    private final Pattern pattern;

    NetworkCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract NetworkInputDTO createDTO(Matcher matcher);
}
