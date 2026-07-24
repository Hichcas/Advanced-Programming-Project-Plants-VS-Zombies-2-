package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.BeghouledGameEngine;
import com.PVZ.model.game.Map;
import com.PVZ.model.minigame.beghouled.BeghouledGame;
import com.PVZ.model.minigame.beghouled.BeghouledLevelDefinition;
import com.PVZ.model.minigame.beghouled.BeghouledLevelLoader;
import com.PVZ.model.minigame.beghouled.BeghouledUpgrade;
import com.PVZ.model.minigame.common.MinigamesDataLoader;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.input.DTO.BeghouledInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;
import com.fasterxml.jackson.databind.JsonNode;

public class BeghouledMenuController {

    private static BeghouledGame currentGame;
    private static BeghouledGameEngine currentEngine;
    private final BeghouledLevelLoader levelLoader = new BeghouledLevelLoader();

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof BeghouledInputDTO dto) || dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        if (currentGame != null && currentGame.isFinished()) {
            String message = currentGame.isWon()
                ? "Target matches reached! Level complete! Returning to Travel Log."
                : "A zombie reached your house. GAME OVER! Returning to Travel Log.";
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
        BeghouledLevelDefinition level = levelLoader.loadLevel(levelId);
        currentEngine = new BeghouledGameEngine();
        currentGame = new BeghouledGame(level);
        currentEngine.setGame(currentGame);

        Map map = new Map(550, 1240, 1600, 1170, currentGame.getRows(), currentGame.getCols());
        currentEngine.setMap(map);
        currentEngine.initializeBoard();

        AppStatus.setGameEngine(currentEngine);
        return new OutputDTO(true, "Beghouled level " + levelId + " started. Grid: "
            + currentGame.getRows() + "x" + currentGame.getCols()
            + ". Target matches: " + currentGame.getTargetMatches()
            + ". Starting sun: " + currentGame.getSun()
            + ". Swap adjacent plants to make matches; spend sun on upgrades. Zombies never stop!");
    }

    private OutputDTO selectLevel(int levelId) {
        BeghouledLevelDefinition level = levelLoader.loadLevel(levelId);
        StringBuilder sb = new StringBuilder("Beghouled level ").append(levelId).append("\n");
        sb.append(" Grid: ").append(level.getRows()).append("x").append(level.getCols()).append("\n");
        sb.append(" Target matches: ").append(level.getTargetMatches()).append("\n");
        sb.append(" Starting sun: ").append(level.getStartingSun()).append("\n");
        sb.append(" Zombie spawn every: ").append(String.format("%.1f", level.getZombieSpawnIntervalSeconds())).append(
            "s\n");
        sb.append(" Board plants: ");
        if (level.getPlantTypes() != null) {
            for (String name : level.getPlantTypes()) {
                PlantType t = safeType(name);
                sb.append(t != null ? t.getDisplayName() : name).append(", ");
            }
        }
        sb.append("\n Upgrades:\n");
        if (level.getUpgrades() != null) {
            for (BeghouledUpgrade up : level.getUpgrades()) {
                sb.append("  - ").append(up.describe()).append("\n");
            }
        }
        sb.append("Use 'beghouled start ").append(levelId).append("' to play.");
        return new OutputDTO(true, sb.toString());
    }

    private static PlantType safeType(String name) {
        try {
            return PlantType.valueOf(name.trim());
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private OutputDTO showLevels() {
        JsonNode section = MinigamesDataLoader.section("beghouled");
        JsonNode levels = section.get("levels");
        if (levels == null || !levels.isArray() || levels.isEmpty()) {
            return new OutputDTO(true, "No Beghouled levels configured yet. "
                + "Use 'beghouled start 1' to play the fallback level.");
        }
        StringBuilder sb = new StringBuilder("Beghouled levels:\n");
        for (JsonNode levelNode : levels) {
            int id = levelNode.path("id").asInt(-1);
            int target = levelNode.path("targetMatches").asInt(0);
            sb.append(" - id ").append(id).append(" (target matches: ").append(target).append(")\n");
        }
        sb.append("Use 'beghouled start <id>' to play one.");
        return new OutputDTO(true, sb.toString());
    }

    private OutputDTO showBoard() {
        if (currentGame == null || currentEngine == null) {
            return new OutputDTO(false, "No active Beghouled game. Use 'beghouled start <id>' first.");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Sun: ").append(currentGame.getSun())
            .append("  Matches: ").append(currentGame.getMatchesMade())
            .append("/").append(currentGame.getTargetMatches())
            .append("  (XX = crater, .. = empty)\n");
        for (int r = 0; r < currentGame.getRows(); r++) {
            for (int c = 0; c < currentGame.getCols(); c++) {
                sb.append("[").append(currentGame.cellTag(r, c)).append("]");
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
        return new OutputDTO(true, "Exited Beghouled. Returning to Travel Log.");
    }

    public static BeghouledGame getCurrentGame() {
        return currentGame;
    }

    public static BeghouledGameEngine getCurrentEngine() {
        return currentEngine;
    }
}
