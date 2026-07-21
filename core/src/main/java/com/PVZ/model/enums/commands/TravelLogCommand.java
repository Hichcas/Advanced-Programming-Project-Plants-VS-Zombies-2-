package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.TravelLogInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TravelLogCommand {

    SHOW_QUESTS("^\\s*travel\\s+log\\s+(?:show|page)\\s+quests\\s*$") {
        @Override
        public TravelLogInputDTO createDTO(Matcher matcher) {
            return new TravelLogInputDTO(this, "quests");
        }
    },
    SHOW_PROGRESS("^\\s*travel\\s+log\\s+(?:show|page)\\s+progress\\s*$") {
        @Override
        public TravelLogInputDTO createDTO(Matcher matcher) {
            return new TravelLogInputDTO(this, "progress");
        }
    },
    SHOW_MINIGAMES("^\\s*travel\\s+log\\s+(?:show|page)\\s+minigames\\s*$") {
        @Override
        public TravelLogInputDTO createDTO(Matcher matcher) {
            return new TravelLogInputDTO(this, "minigames");
        }
    },
    SHOW_PAGE("^\\s*travel\\s+log\\s+page\\s+(?<pageName>.+?)\\s*$") {
        @Override
        public TravelLogInputDTO createDTO(Matcher matcher) {
            return new TravelLogInputDTO(this, matcher.group("pageName"));
        }
    },
    ENTER_VASEBREAKER("^\\s*travel\\s+log\\s+enter\\s+vasebreaker\\s*$") {
        @Override
        public TravelLogInputDTO createDTO(Matcher matcher) {
            return new TravelLogInputDTO(this, null);
        }
    },
    ENTER_WALLNUT_BOWLING("^\\s*travel\\s+log\\s+enter\\s+wallnutbowling\\s*$") {
        @Override
        public TravelLogInputDTO createDTO(Matcher matcher) {
            return new TravelLogInputDTO(this, null);
        }
    },
    ENTER_IZOMBIE("^\\s*travel\\s+log\\s+enter\\s+izombie\\s*$") {
        @Override
        public TravelLogInputDTO createDTO(Matcher matcher) {
            return new TravelLogInputDTO(this, null);
        }
    },
    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public TravelLogInputDTO createDTO(Matcher matcher) {
            return new TravelLogInputDTO(this, null);
        }
    },
    ENTER_QUEST("^\\s*enter\\s+quest\\s*$") {
        @Override
        public TravelLogInputDTO createDTO(Matcher matcher) {
            return new TravelLogInputDTO(this, null);
        }
    },
    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public TravelLogInputDTO createDTO(Matcher matcher) {
            return new TravelLogInputDTO(this, null);
        }
    };

    private final Pattern pattern;

    TravelLogCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract TravelLogInputDTO createDTO(Matcher matcher);
}
