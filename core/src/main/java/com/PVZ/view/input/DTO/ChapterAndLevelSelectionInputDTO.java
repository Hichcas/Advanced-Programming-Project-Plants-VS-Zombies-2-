package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.ChapterAndLevelSelectionCommand;
import com.PVZ.view.input.InputDTO;

public class ChapterAndLevelSelectionInputDTO implements InputDTO {

    private final ChapterAndLevelSelectionCommand command;
    private final String chapterName;
    private final Integer amount;
    private final String currency;
    private final Integer stage;

    public ChapterAndLevelSelectionInputDTO(ChapterAndLevelSelectionCommand command,
                                            String chapterName,
                                            Integer amount,
                                            String currency,
                                            Integer stage) {
        this.command = command;
        this.chapterName = chapterName;
        this.amount = amount;
        this.currency = currency;
        this.stage = stage;
    }

    public static ChapterAndLevelSelectionInputDTO invalid() {
        return new ChapterAndLevelSelectionInputDTO(null, null, null, null, null);
    }

    public ChapterAndLevelSelectionCommand getCommand() {
        return command;
    }

    public String getChapterName() {
        return chapterName;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Integer getStage() {
        return stage;
    }
}
