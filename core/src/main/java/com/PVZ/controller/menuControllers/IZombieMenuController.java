package com.PVZ.controller.menuControllers;

import com.PVZ.model.game.IZombieGameEngine;
import com.PVZ.model.game.Map;
import com.PVZ.model.minigame.izombie.IZombieGame;
import com.PVZ.model.minigame.izombie.IZombieLevelDefinition;
import com.PVZ.model.minigame.izombie.IZombieLevelLoader;
import com.PVZ.model.minigame.izombie.ZombieOption;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.DTO.IZombieInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class IZombieMenuController {

    private static IZombieGame currentGame;
    private static IZombieGameEngine currentEngine;
    private final IZombieLevelLoader levelLoader = new IZombieLevelLoader();

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof IZombieInputDTO dto) || dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        if (currentGame != null && currentGame.isFinished()) {
            String message = currentGame.isWon()
                    ? "All brains eaten! Level complete! Returning to Chapter Selection."
                    : "Out of sun and zombies. GAME OVER! Returning to Chapter Selection.";
            currentGame = null;
            currentEngine = null;
            AppStatus.returnToChapterAndLevelSelection(null);
            return new OutputDTO(true, message);
        }

        return switch (dto.getCommand()) {
            case START_LEVEL -> startLevel(dto.getLevelId());
            case DEPLOY -> deploy(dto.getZombieAlias(), dto.getRow(), dto.getCol());
            case SHOW_ROSTER -> showRoster();
            case SHOW_BOARD -> showBoard();
            case EXIT -> exit();
        };
    }

    private OutputDTO startLevel(int levelId) {
        IZombieLevelDefinition level = levelLoader.loadLevel(levelId);
        currentEngine = new IZombieGameEngine();
        currentGame = new IZombieGame(level);
        currentEngine.setGame(currentGame);

        Map map = new Map(480, 1235, 1655, 1170, currentGame.getRows(), currentGame.getCols());
        currentEngine.setMap(map);
        currentEngine.initializeBoard();

        AppStatus.setGameEngine(currentEngine);
        return new OutputDTO(true, "I, Zombie level " + levelId + " started. Grid: "
                + currentGame.getRows() + "x" + currentGame.getCols()
                + ". Red line at col " + currentGame.getRedLineCol()
                + " (deploy zombies to the right of it). Starting sun: " + currentGame.getSun()
                + ". Use 'izombie roster' to see zombies, 'izombie deploy <alias> <row> <col>' to place one.");
    }

    private OutputDTO deploy(String alias, int row, int col) {
        if (currentGame == null || currentEngine == null) {
            return new OutputDTO(false, "No active I, Zombie game. Use 'izombie start <id>' first.");
        }
        String result = currentEngine.deployZombie(alias, row, col);
        boolean success = result != null && result.startsWith("Deployed");
        return new OutputDTO(success, result);
    }

    private OutputDTO showRoster() {
        if (currentGame == null) {
            return new OutputDTO(false, "No active I, Zombie game.");
        }
        StringBuilder sb = new StringBuilder("Available zombies (sun: ").append(currentGame.getSun()).append(")\n");
        for (ZombieOption option : currentGame.getRoster()) {
            sb.append(" - ").append(option.getAlias())
              .append(" (").append(option.getDisplayName()).append(")")
              .append(": ").append(option.getCost()).append(" sun\n");
        }
        return new OutputDTO(true, sb.toString());
    }

    private OutputDTO showBoard() {
        if (currentGame == null || currentEngine == null) {
            return new OutputDTO(false, "No active I, Zombie game.");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Sun: ").append(currentGame.getSun())
          .append("  Brains left: ").append(currentGame.getBrainsRemaining())
          .append("/").append(currentGame.getRows())
          .append("  Sun rate: ").append(String.format("%.1f", currentGame.getCurrentSunRate()))
          .append("\n");
        Map map = currentEngine.getMap();
        for (int r = 0; r < currentGame.getRows(); r++) {
            for (int c = 0; c < currentGame.getCols(); c++) {
                boolean redLine = c == currentGame.getRedLineCol();
                boolean hasPlant = map != null && map.getPlantAt(r, c) != null;
                if (hasPlant) sb.append("[P]");
                else sb.append(redLine ? "[|]" : "[ ]");
            }
            sb.append(currentGame.isBrainEaten(r) ? "  <- brain eaten" : "").append("\n");
        }
        if (currentGame.isWon()) sb.append("LEVEL COMPLETE!\n");
        if (currentGame.isLost()) sb.append("GAME OVER!\n");
        return new OutputDTO(true, sb.toString());
    }

    private OutputDTO exit() {
        currentGame = null;
        currentEngine = null;
        AppStatus.returnToChapterAndLevelSelection(null);
        return new OutputDTO(true, "Exited I, Zombie. Returning to Chapter Selection.");
    }

    public static IZombieGame getCurrentGame() {
        return currentGame;
    }

    public static IZombieGameEngine getCurrentEngine() {
        return currentEngine;
    }
}
