package com.PVZ.model.enums;

import com.PVZ.view.input.DTO.CollectionInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum CollectionCommand {

    SHOW_PLANTS("^\\s*(?:menu\\s+collection\\s+)?show-plants\\s*$") {
        @Override
        public CollectionInputDTO createDTO(Matcher matcher) {
            return new CollectionInputDTO(this, null, null);
        }
    },
    SHOW_ALL_PLANTS("^\\s*(?:menu\\s+collection\\s+)?show-all-plants\\s*$") {
        @Override
        public CollectionInputDTO createDTO(Matcher matcher) {
            return new CollectionInputDTO(this, null, null);
        }
    },
    SHOW_ZOMBIES("^\\s*(?:menu\\s+collection\\s+)?show-zombies\\s*$") {
        @Override
        public CollectionInputDTO createDTO(Matcher matcher) {
            return new CollectionInputDTO(this, null, null);
        }
    },
    SHOW_ALL_ZOMBIES("^\\s*(?:menu\\s+collection\\s+)?show-all-zombies\\s*$") {
        @Override
        public CollectionInputDTO createDTO(Matcher matcher) {
            return new CollectionInputDTO(this, null, null);
        }
    },
    SHOW_PLANT("^\\s*(?:menu\\s+collection\\s+)?show-plant\\s+-p\\s+(?<plantName>.+?)\\s*$") {
        @Override
        public CollectionInputDTO createDTO(Matcher matcher) {
            return new CollectionInputDTO(this, matcher.group("plantName"), null);
        }
    },
    SHOW_ZOMBIE("^\\s*(?:menu\\s+collection\\s+)?show-zombie\\s+-z\\s+(?<zombieName>.+?)\\s*$") {
        @Override
        public CollectionInputDTO createDTO(Matcher matcher) {
            return new CollectionInputDTO(this, null, matcher.group("zombieName"));
        }
    },
    UPGRADE_PLANT("^\\s*(?:menu\\s+collection\\s+)?upgrade-plant\\s+-p\\s+(?<plantName>.+?)\\s*$") {
        @Override
        public CollectionInputDTO createDTO(Matcher matcher) {
            return new CollectionInputDTO(this, matcher.group("plantName"), null);
        }
    },
    PURCHASE_PLANT("^\\s*(?:menu\\s+collection\\s+)?purchase-plant\\s+-p\\s+(?<plantName>.+?)\\s*$") {
        @Override
        public CollectionInputDTO createDTO(Matcher matcher) {
            return new CollectionInputDTO(this, matcher.group("plantName"), null);
        }
    },
    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public CollectionInputDTO createDTO(Matcher matcher) {
            return new CollectionInputDTO(this, null, null);
        }
    },
    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public CollectionInputDTO createDTO(Matcher matcher) {
            return new CollectionInputDTO(this, null, null);
        }
    };

    private final Pattern pattern;

    CollectionCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract CollectionInputDTO createDTO(Matcher matcher);
}
