package com.PVZ.model.enums;

import com.PVZ.view.input.DTO.GreenhouseInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum GreenhouseCommand {

    SHOW_GREENHOUSE("^\\s*show\\s+greenhouse\\s*$") {
        @Override
        public GreenhouseInputDTO createDTO(Matcher matcher) {
            return new GreenhouseInputDTO(this, null, null, null);
        }
    },
    PLANT_POT_AT("^\\s*plant\\s+pot\\s+at\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public GreenhouseInputDTO createDTO(Matcher matcher) {
            return new GreenhouseInputDTO(
                    this,
                    Integer.parseInt(matcher.group("x")),
                    Integer.parseInt(matcher.group("y")),
                    null);
        }
    },
    COLLECT("^\\s*collect\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public GreenhouseInputDTO createDTO(Matcher matcher) {
            return new GreenhouseInputDTO(this, Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y")), null);
        }
    },
    GROW("^\\s*grow\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public GreenhouseInputDTO createDTO(Matcher matcher) {
            return new GreenhouseInputDTO(this, Integer.parseInt(matcher.group("x")), Integer.parseInt(matcher.group("y")), null);
        }
    },
    ENTER_SHOP("^\\s*enter\\s+shop\\s*$") {
        @Override
        public GreenhouseInputDTO createDTO(Matcher matcher) {
            return new GreenhouseInputDTO(this, null, null, null);
        }
    },
    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public GreenhouseInputDTO createDTO(Matcher matcher) {
            return new GreenhouseInputDTO(this, null, null, null);
        }
    },
    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public GreenhouseInputDTO createDTO(Matcher matcher) {
            return new GreenhouseInputDTO(this, null, null, null);
        }
    };

    private final Pattern pattern;

    GreenhouseCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract GreenhouseInputDTO createDTO(Matcher matcher);
}
