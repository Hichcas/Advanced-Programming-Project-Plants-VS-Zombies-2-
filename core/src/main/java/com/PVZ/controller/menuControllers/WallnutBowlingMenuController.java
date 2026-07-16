package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.game.Map;
import com.PVZ.model.game.WallnutBowlingGameEngine;
import com.PVZ.model.minigame.wallnutbowling.BowlingNut;
import com.PVZ.model.minigame.wallnutbowling.NutType;
import com.PVZ.model.minigame.wallnutbowling.WallnutBowlingGame;
import com.PVZ.model.minigame.wallnutbowling.WallnutBowlingLevelDefinition;
import com.PVZ.model.minigame.wallnutbowling.WallnutBowlingLevelLoader;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.DTO.WallnutBowlingInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class WallnutBowlingMenuController {

    private static WallnutBowlingGame currentGame;
    private static WallnutBowlingGameEngine currentEngine;
    private final WallnutBowlingLevelLoader levelLoader = new WallnutBowlingLevelLoader();

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof WallnutBowlingInputDTO dto) || dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        return switch (dto.getCommand()) {
            case START_LEVEL -> startLevel(dto.getLevelId());
            case LAUNCH -> launch(dto.getRow(), dto.getCol());
            case NEXT_NUT -> nextNut();
            case SHOW_BOARD -> showBoard();
            case EXIT -> exit();
        };
    }

    private OutputDTO startLevel(int levelId) {
        WallnutBowlingLevelDefinition level = levelLoader.loadLevel(levelId);
        currentEngine = new WallnutBowlingGameEngine();
        currentGame = new WallnutBowlingGame(level);
        currentEngine.setGame(currentGame);

        Map map = new Map(550, 1240, 1600, 1170, currentGame.getRows(), currentGame.getCols());
        currentEngine.setMap(map);

        AppStatus.setGameEngine(currentEngine);
        return new OutputDTO(true, "Wallnut Bowling level " + levelId + " started. Grid: "
                + currentGame.getRows() + "x" + currentGame.getCols()
                + ". Red line at col " + currentGame.getRedLineCol()
                + ". Held nut: " + currentGame.getHeldNut()
                + ". Use 'wallnutbowling launch <row> <col>' to bowl it.");
    }

    private OutputDTO launch(int row, int col) {
        if (currentGame == null || currentEngine == null) {
            return new OutputDTO(false, "No active Wallnut Bowling game. Use 'wallnutbowling start <id>' first.");
        }
        NutType held = currentGame.getHeldNut();
        if (currentGame.getCooldownRemaining() > 0.0) {
            return new OutputDTO(false, String.format(
                    "Still reloading, wait %.1fs before launching the next nut.", currentGame.getCooldownRemaining()));
        }
        boolean launched = currentEngine.launchHeldNut(row, col);
        if (!launched) {
            return new OutputDTO(false, "Can't launch there. Col must be between 0 and "
                    + currentGame.getRedLineCol() + " (left of the red line).");
        }
        currentGame.drawNextNut();
        return new OutputDTO(true, held + " nut launched at row " + row + ", col " + col
                + ". Next nut: " + currentGame.getHeldNut());
    }

    private OutputDTO nextNut() {
        if (currentGame == null) {
            return new OutputDTO(false, "No active Wallnut Bowling game.");
        }
        return new OutputDTO(true, "Currently held nut: " + currentGame.getHeldNut());
    }

    private OutputDTO showBoard() {
        if (currentGame == null || currentEngine == null) {
            return new OutputDTO(false, "No active Wallnut Bowling game.");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Held nut: ").append(currentGame.getHeldNut())
                .append("  Zombies: ").append(currentGame.getZombiesSpawned())
                .append("/").append(currentGame.getTotalZombies())
                .append("\n");
        for (int r = 0; r < currentGame.getRows(); r++) {
            for (int c = 0; c < currentGame.getCols(); c++) {
                boolean redLine = c == currentGame.getRedLineCol();
                boolean occupied = false;
                for (BowlingNut nut : currentGame.getNuts()) {
                    Map map = currentEngine.getMap();
                    if (map == null) continue;
                    if (map.worldToRow((float) nut.getY()) == r && map.worldToCol((float) nut.getX()) == c) {
                        occupied = true;
                        break;
                    }
                }
                if (occupied) sb.append("[N]");
                else sb.append(redLine ? "[|]" : "[ ]");
            }
            sb.append("\n");
        }
        if (currentGame.isWon()) sb.append("LEVEL COMPLETE!\n");
        if (currentGame.isLost()) sb.append("GAME OVER!\n");
        return new OutputDTO(true, sb.toString());
    }

    private OutputDTO exit() {
        AppStatus.currentMenuType = MenuType.TRAVEL_LOG;
        return new OutputDTO(true, "Exited Wallnut Bowling. Returning to Travel Log.");
    }

    public static WallnutBowlingGame getCurrentGame() {
        return currentGame;
    }

    public static WallnutBowlingGameEngine getCurrentEngine() {
        return currentEngine;
    }
}
