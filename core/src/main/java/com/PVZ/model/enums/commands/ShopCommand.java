package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.ShopInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ShopCommand {

    LIST("^\\s*shop\\s+list\\s*$") {
        @Override
        public ShopInputDTO createDTO(Matcher matcher) {
            return new ShopInputDTO(this, null, null, null);
        }
    },
    DAILY("^\\s*shop\\s+daily\\s*$") {
        @Override
        public ShopInputDTO createDTO(Matcher matcher) {
            return new ShopInputDTO(this, null, null, null);
        }
    },
    BUY("^\\s*shop\\s+buy\\s+-i\\s+(?<itemId>\\S+)\\s+-n\\s+(?<count>\\d+)(?:\\s+-t\\s+(?<plantType>.+?))?\\s*$") {
        @Override
        public ShopInputDTO createDTO(Matcher matcher) {
            return new ShopInputDTO(
                    this,
                    matcher.group("itemId"),
                    Integer.parseInt(matcher.group("count")),
                    matcher.group("plantType"));
        }
    },
    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public ShopInputDTO createDTO(Matcher matcher) {
            return new ShopInputDTO(this, null, null, null);
        }
    },
    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public ShopInputDTO createDTO(Matcher matcher) {
            return new ShopInputDTO(this, null, null, null);
        }
    },
    COINS ("show coins") {
        @Override
        public ShopInputDTO createDTO(Matcher matcher) {
            return new ShopInputDTO(this, null, null, null);
        }
    },
    UNLOCK_ALL_PLANTS("unlock all plants") {
        @Override
        public ShopInputDTO createDTO(Matcher matcher) {
            return new ShopInputDTO(this, null, null, null);
        }
    },
    UNLOCK_PLANT("^\\s*unlock\\s+plant\\s+-t\\s+(?<plantType>.+?)\\s*$") {
        @Override
        public ShopInputDTO createDTO(Matcher matcher) {
            return new ShopInputDTO(this, null, null, matcher.group("plantType"));
        }
    };

    private final Pattern pattern;

    ShopCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract ShopInputDTO createDTO(Matcher matcher);
}
