package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.InGameInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum InGameCommand {

    ADVANCE_TIME("^\\s*advance\\s+time\\s+-t\\s+(?<ticks>\\d+)\\s+ticks\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, Integer.parseInt(matcher.group("ticks")), null, null, null, null, null);
        }
    },
    COLLECT_SUN("^\\s*collect\\s+sun\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, Integer.parseInt(matcher.group("x")), Integer
                    .parseInt(matcher.group("y")));
        }
    },
    SHOW_SUN_AMOUNT("^\\s*show\\s+sun\\s+amount\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    CHEAT_ADD_SUNS("^\\s*cheat\\s+add\\s+-n\\s+(?<amount>\\d+)\\s+suns\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, Integer.parseInt(matcher.group("amount")), null, null, null, null);
        }
    },
    CHEAT_REMOVE_COOLDOWN("^\\s*cheat\\s+remove-cooldown\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    CHEAT_ADD_PLANT_FOOD("^\\s*cheat\\s+add-plant-food\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    CHEAT_SPAWN_ZOMBIE(
            "^\\s*cheat\\s+spawn-zombie\\s+-t\\s+(?<zombieType>.+?)\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, matcher.group("zombieType"), Integer.parseInt(matcher
                    .group("x")), Integer.parseInt(matcher.group("y")));
        }
    },
    CHEAT_SET_WATER("^\\s*cheat\\s+set-water\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, Integer.parseInt(matcher.group("x")), Integer
                    .parseInt(matcher.group("y")));
        }
    },
    CHEAT_SET_DRY("^\\s*cheat\\s+set-dry\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, Integer.parseInt(matcher.group("x")), Integer
                    .parseInt(matcher.group("y")));
        }
    },
    CHEAT_RELEASE_NUKE("^\\s*release\\s+the\\s+nuke\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    PLANT_PLANT(
            "^\\s*plant\\s+plant\\s+-t\\s+(?<plantType>.+?)\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$")
            {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            int x = Integer.parseInt(matcher.group("x")) ;
            int y = Integer.parseInt(matcher.group("y")) ;
            return new InGameInputDTO(this, null, null, matcher.group("plantType"), null, x, y);
        }
    },
    PLUCK_PLANT("^\\s*pluck\\s+plant\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, Integer.parseInt(matcher.group("x")), Integer
                    .parseInt(matcher.group("y")));
        }
    },
    FEED_PLANT("^\\s*feed\\s+plant\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, Integer.parseInt(matcher.group("x")), Integer
                    .parseInt(matcher.group("y")));
        }
    },
    START_ZOMBIE_WAVES("^\\s*start\\s+zombie\\s+waves\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    SHOW_MAP("^\\s*show\\s+map\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    SHOW_PLANTS_STATUS("^\\s*show\\s+plants\\s+status\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    SHOW_TILE_STATUS("^\\s*show\\s+tile\\s+status\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, Integer.parseInt(matcher.group("x")), Integer
                    .parseInt(matcher.group("y")));
        }
    },
    ZOMBIES_INFO("^\\s*zombies\\s+info\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    SHOW_TILE_DEBUG("^\\s*show\\s+tile\\s+debug\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    HIDE_TILE_DEBUG("^\\s*hide\\s+tile\\s+debug\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    FREEZE_ZOMBIE("^\\s*freeze\\s+zombie\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, Integer.parseInt(matcher.group("x")), Integer
                    .parseInt(matcher.group("y")));
        }
    },
    POISON_ZOMBIE("^\\s*poison\\s+zombie\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, Integer.parseInt(matcher.group("x")), Integer
                    .parseInt(matcher.group("y")));
        }
    },
    KILL_ZOMBIE("^\\s*kill\\s+zombie\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, Integer.parseInt(matcher.group("x")), Integer
                    .parseInt(matcher.group("y")));
        }
    },
    HYPNOTIZE_ZOMBIE("^\\s*hypnotize\\s+zombie\\s+-l\\s*\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, Integer.parseInt(matcher.group("x")), Integer
                    .parseInt(matcher.group("y")));
        }
    },
    KILL_ALL_ZOMBIES("^\\s*kill\\-all\\s+zombies\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    },
    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public InGameInputDTO createDTO(Matcher matcher) {
            return new InGameInputDTO(this, null, null, null, null, null, null);
        }
    };

    private final Pattern pattern;

    InGameCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract InGameInputDTO createDTO(Matcher matcher);
}
