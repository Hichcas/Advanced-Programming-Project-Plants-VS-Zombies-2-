package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.game.UpdateHandler;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.EndOfGameInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class EndOfGameMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof EndOfGameInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        return switch (dto.getCommand()) {
            case SHOW_STATS -> showStats();
            case EXIT -> exitEndOfGame();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
        };
    }

    private OutputDTO showStats() {
        boolean won = AppStatus.lastGameResultWin;
        StringBuilder sb = new StringBuilder();
        if (won) {
            sb.append("LEVEL COMPLETE! ");
        } else {
            sb.append("GAME OVER. ");
        }
        String chapterName = AppStatus.currentChapterName;
        int stage = AppStatus.currentStageNumber;
        if (chapterName != null) {
            ChapterEnum chapter = AppStatus.getCurrentChapterEnum();
            String display = chapter != null ? chapter.getDisplayName() : chapterName;
            sb.append("Chapter: ").append(display).append(", Stage ").append(stage);
        }
        if (AppStatus.currentUser != null) {
            int completed = AppStatus.currentUser.userStats.getStagesCompleted();
            sb.append(" | Total stages completed: ").append(completed);
        }
        return new OutputDTO(true, sb.toString());
    }

    private OutputDTO exitEndOfGame() {
        RegularGameEngine engine = getRegularEngine();
        if (engine != null) {
            UpdateHandler.resetBoardAfterGameOver(engine);
            AppStatus.setGameEngine(null);
        }
        if (AppStatus.lastGameResultWin) {
            AppStatus.returnToTravelLog();
        } else {
            AppStatus.returnToMainMenu("GAME OVER");
        }
        return new OutputDTO(true, "Returning to menu...");
    }

    private RegularGameEngine getRegularEngine() {
        if (AppStatus.getGameEngine() instanceof RegularGameEngine reg) {
            return reg;
        }
        return null;
    }
}
