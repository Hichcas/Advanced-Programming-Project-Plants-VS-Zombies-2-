package com.PVZ.controller.menuControllers;

import com.PVZ.model.game.Map;
import com.PVZ.model.game.VasebreakerGameEngine;
import com.PVZ.model.minigame.vasebreaker.Vase;
import com.PVZ.model.minigame.vasebreaker.VasebreakerGame;
import com.PVZ.model.minigame.vasebreaker.VasebreakerLevelDefinition;
import com.PVZ.model.minigame.vasebreaker.VasebreakerLevelLoader;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.DTO.VasebreakerInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class VasebreakerMenuController {

    private static VasebreakerGame currentGame;
    private static VasebreakerGameEngine currentEngine;
    private final VasebreakerLevelLoader levelLoader = new VasebreakerLevelLoader();

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof VasebreakerInputDTO dto) || dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        return switch (dto.getCommand()) {
            case START_LEVEL -> startLevel(dto.getLevelId());
            case BREAK_VASE -> breakVase(dto.getRow(), dto.getCol());
            case PLANT_AT -> plantAt(dto.getRow(), dto.getCol());
            case SHOW_BOARD -> showBoard();
            case EXIT -> exit();
        };
    }

    private OutputDTO startLevel(int levelId) {
        VasebreakerLevelDefinition level = levelLoader.loadLevel(levelId);
        currentEngine = new VasebreakerGameEngine();
        currentGame = new VasebreakerGame(level, currentEngine);
        currentEngine.setGame(currentGame);

        Map map = new Map(
                550, 1240, 1600, 1170, currentGame.getRows(), currentGame.getCols());
        currentEngine.setMap(map);

        AppStatus.setGameEngine(currentEngine);
        return new OutputDTO(true, "Vasebreaker level " + levelId + " started. Grid: "
                + currentGame.getRows() + "x" + currentGame.getCols());
    }

    private OutputDTO breakVase(int row, int col) {
        if (currentGame == null) {
            return new OutputDTO(false, "No active Vasebreaker game. Use 'vasebreaker start <id>' first.");
        }
        String message = currentGame.breakVaseAt(row, col);
        if (currentGame.isFinished()) {
            message += "all Vases are broken berid khoonatoon";
        }
        return new OutputDTO(true, message);
    }

    private OutputDTO plantAt(int row, int col) {
        if (currentGame == null) {
            return new OutputDTO(false, "No active Vasebreaker game.");
        }
        boolean planted = currentGame.plantSeedAt(row, col);
        if (!planted) {
            return new OutputDTO(false, "there is no seeds in this place");
        }
        return new OutputDTO(true, "plant is planted mobarak kheilia");
    }

    private OutputDTO showBoard() {
        if (currentGame == null) {
            return new OutputDTO(false, "No active Vasebreaker game.");
        }
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < currentGame.getRows(); r++) {
            for (int c = 0; c < currentGame.getCols(); c++) {
                Vase v = currentGame.getVase(r, c);
                sb.append(v == null || v.isBroken() ? "[ ]" : "[V]");
            }
            sb.append("\n");
        }
        return new OutputDTO(true, sb.toString());
    }

    private OutputDTO exit() {
        currentGame = null;
        currentEngine = null;
        AppStatus.returnToChapterAndLevelSelection(null);
        return new OutputDTO(true, "Exited Vasebreaker. Returning to Chapter Selection.");
    }

    public static VasebreakerGame getCurrentGame() {
        return currentGame;
    }

    public static VasebreakerGameEngine getCurrentEngine() {
        return currentEngine;
    }
}
