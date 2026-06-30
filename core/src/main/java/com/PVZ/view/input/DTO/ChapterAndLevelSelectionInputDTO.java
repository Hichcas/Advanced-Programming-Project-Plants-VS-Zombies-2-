package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.ChapterAndLevelSelectionCommand;
import com.PVZ.view.input.InputDTO;

public class ChapterAndLevelSelectionInputDTO implements InputDTO {

    private final ChapterAndLevelSelectionCommand command;
    private final String chapterName;
    private final Integer amount;
    private final String currency;

    public ChapterAndLevelSelectionInputDTO(ChapterAndLevelSelectionCommand command,
                                            String chapterName,
                                            Integer amount,
                                            String currency) {
        this.command = command;
        this.chapterName = chapterName;
        this.amount = amount;
        this.currency = currency;
    }

    public static ChapterAndLevelSelectionInputDTO invalid() {
        return new ChapterAndLevelSelectionInputDTO(null, null, null, null);
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
}
