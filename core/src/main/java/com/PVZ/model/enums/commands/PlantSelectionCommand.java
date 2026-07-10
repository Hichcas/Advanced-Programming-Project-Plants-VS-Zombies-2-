package com.PVZ.model.enums.commands;

import com.PVZ.view.input.DTO.PlantSelectionInputDTO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PlantSelectionCommand {

    SHOW_ALL_PLANTS("^\\s*show\\s+all\\s+plants\\s*$") {
        @Override
        public PlantSelectionInputDTO createDTO(Matcher matcher) {
            return new PlantSelectionInputDTO(this, null);
        }
    },
    SHOW_AVAILABLE_PLANTS("^\\s*show\\s+available\\s+plants\\s*$") {
        @Override
        public PlantSelectionInputDTO createDTO(Matcher matcher) {
            return new PlantSelectionInputDTO(this, null);
        }
    },
    ADD_PLANT("^\\s*add\\s+plant\\s+-t\\s+(?<plantType>.+?)\\s*$") {
        @Override
        public PlantSelectionInputDTO createDTO(Matcher matcher) {
            return new PlantSelectionInputDTO(this, matcher.group("plantType"));
        }
    },
    REMOVE_PLANT("^\\s*remove\\s+plant\\s+-t\\s+(?<plantType>.+?)\\s*$") {
        @Override
        public PlantSelectionInputDTO createDTO(Matcher matcher) {
            return new PlantSelectionInputDTO(this, matcher.group("plantType"));
        }
    },
    BOOST_PLANT("^\\s*boost\\s+plant\\s+-t\\s+(?<plantType>.+?)\\s*$") {
        @Override
        public PlantSelectionInputDTO createDTO(Matcher matcher) {
            return new PlantSelectionInputDTO(this, matcher.group("plantType"));
        }
    },
    START_GAME("^\\s*start\\s+game\\s*$") {
        @Override
        public PlantSelectionInputDTO createDTO(Matcher matcher) {
            return new PlantSelectionInputDTO(this, null);
        }
    },
    SHOW_CURRENT_MENU("^menu\\s+show\\s+current$|^show\\s+current\\s+menu$") {
        @Override
        public PlantSelectionInputDTO createDTO(Matcher matcher) {
            return new PlantSelectionInputDTO(this, null);
        }
    },
    EXIT("^\\s*menu\\s+exit\\s*$") {
        @Override
        public PlantSelectionInputDTO createDTO(Matcher matcher) {
            return new PlantSelectionInputDTO(this, null);
        }
    };

    private final Pattern pattern;

    PlantSelectionCommand(String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public Matcher matcher(String input) {
        return pattern.matcher(input == null ? "" : input);
    }

    public abstract PlantSelectionInputDTO createDTO(Matcher matcher);
}
