package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.Map;
import com.PVZ.model.game.ZombotanyGameEngine;
import com.PVZ.model.minigame.zombotany.ZombotanyGame;
import com.PVZ.model.minigame.zombotany.ZombotanyLevelDefinition;
import com.PVZ.model.minigame.zombotany.ZombotanyLevelLoader;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.DTO.ZombotanyInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.List;

public class ZombotanyMenuController {

    private static ZombotanyGame currentGame;
    private static ZombotanyGameEngine currentEngine;
    private static int selectedLevelId = -1;
    private final ZombotanyLevelLoader levelLoader = new ZombotanyLevelLoader();

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof ZombotanyInputDTO dto) || dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        if (currentGame != null && currentGame.isFinished()) {
            String message = currentGame.isWon()
                    ? "All waves cleared! Level complete! Returning to Travel Log."
                    : "The zombies reached your house. GAME OVER! Returning to Travel Log.";
            currentGame = null;
            currentEngine = null;
            AppStatus.returnToTravelLog();
            return new OutputDTO(true, message);
        }

        return switch (dto.getCommand()) {
            case START_LEVEL -> startLevel(dto.getLevelId());
            case SELECT_LEVEL -> selectLevel(dto.getLevelId());
            case SHOW_LEVELS -> showLevels();
            case SHOW_BOARD -> showBoard();
            case EXIT -> exit();
        };
    }

    private OutputDTO startLevel(int levelId) {
        ZombotanyLevelDefinition level = levelLoader.loadLevel(levelId);
        currentEngine = new ZombotanyGameEngine();
        currentGame = new ZombotanyGame(level);
        currentEngine.setGame(currentGame);

        Map map = new Map(550, 1240, 1600, 1170, currentGame.getRows(), currentGame.getCols());
        currentEngine.setMap(map);
        currentEngine.initializeBoard();

        selectedLevelId = levelId;
        AppStatus.setGameEngine(currentEngine);

        StringBuilder pool = new StringBuilder();
        for (PlantType type : currentGame.getPlantPool()) {
            if (pool.length() > 0) pool.append(", ");
            pool.append(type.getDisplayName());
        }
        return new OutputDTO(true, "Zombotany level " + levelId + " started. Grid: "
                + currentGame.getRows() + "x" + currentGame.getCols()
                + ". Starting sun: " + currentGame.getSun()
                + ". Waves: " + currentGame.getWaves().size()
                + ". Available plants: [" + pool + "]."
                + " Click a seed packet to select it, then click a tile to plant it."
                + " Defend the house from the plant zombies!");
    }

    private OutputDTO selectLevel(int levelId) {
        selectedLevelId = levelId;
        ZombotanyLevelDefinition level = levelLoader.loadLevel(levelId);
        return new OutputDTO(true, "Zombotany level " + levelId + " selected. Grid: "
                + level.getRows() + "x" + level.getCols()
                + ", starting sun: " + level.getStartingSun()
                + ", waves: " + (level.getWaves() == null ? 0 : level.getWaves().size())
                + ". Use 'zombotany start " + levelId + "' to play it.");
    }

    private OutputDTO showLevels() {
        List<ZombotanyLevelDefinition> levels = levelLoader.loadAll();
        if (levels.isEmpty()) {
            return new OutputDTO(true, "No Zombotany levels defined yet.");
        }
        StringBuilder sb = new StringBuilder("Zombotany levels:\n");
        for (ZombotanyLevelDefinition level : levels) {
            sb.append("  - id ").append(level.getId())
              .append(" | ").append(level.getRows()).append("x").append(level.getCols())
              .append(" | sun ").append(level.getStartingSun())
              .append(" | waves ").append(level.getWaves() == null ? 0 : level.getWaves().size())
              .append("\n");
        }
        sb.append("Use 'zombotany start <id>' to play a level.");
        return new OutputDTO(true, sb.toString());
    }

    private OutputDTO showBoard() {
        if (currentGame == null || currentEngine == null) {
            return new OutputDTO(false, "No active Zombotany game. Use 'zombotany start <id>' first.");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Zombotany — Sun: ").append(currentEngine.getSunCount())
          .append(" | Waves: ").append(currentGame.getWaves().size())
          .append("\n");
        Map map = currentEngine.getMap();
        for (int r = 0; r < currentGame.getRows(); r++) {
            for (int c = 0; c < currentGame.getCols(); c++) {
                boolean hasPlant = map != null && map.getPlantAt(r, c) != null;
                sb.append(hasPlant ? "[P]" : "[ ]");
            }
            sb.append("\n");
        }
        if (currentGame.isWon()) sb.append("LEVEL COMPLETE!\n");
        if (currentGame.isLost()) sb.append("GAME OVER!\n");
        return new OutputDTO(true, sb.toString());
    }

    private OutputDTO exit() {
        currentGame = null;
        currentEngine = null;
        AppStatus.returnToTravelLog();
        return new OutputDTO(true, "Exited Zombotany. Returning to Travel Log.");
    }

    public static ZombotanyGame getCurrentGame() {
        return currentGame;
    }

    public static ZombotanyGameEngine getCurrentEngine() {
        return currentEngine;
    }
}
