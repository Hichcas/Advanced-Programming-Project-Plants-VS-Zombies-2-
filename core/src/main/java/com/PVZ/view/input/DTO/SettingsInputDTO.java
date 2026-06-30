package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.SettingsCommand;
import com.PVZ.view.input.InputDTO;

public class SettingsInputDTO implements InputDTO {

    private final SettingsCommand command;
    private final Integer difficultyLevel;

    public SettingsInputDTO(SettingsCommand command, Integer difficultyLevel) {
        this.command = command;
        this.difficultyLevel = difficultyLevel;
    }

    public static SettingsInputDTO invalid() {
        return new SettingsInputDTO(null, null);
    }

    public SettingsCommand getCommand() {
        return command;
    }

    public Integer getDifficultyLevel() {
        return difficultyLevel;
    }
}
