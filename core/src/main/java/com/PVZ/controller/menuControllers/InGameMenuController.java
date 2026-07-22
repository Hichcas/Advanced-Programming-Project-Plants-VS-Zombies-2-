package com.PVZ.controller.menuControllers;

import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.GameEngine;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.InGameInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.Arrays;
import java.util.List;

public class InGameMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof InGameInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        RegularGameEngine engine = getEngine();
        OutputDTO gameOverResult = handleGameOverIfNeeded(engine);
        if (gameOverResult != null) {
            return gameOverResult;
        }

        return dispatchCommand(dto, engine);
    }

    private OutputDTO handleGameOverIfNeeded(RegularGameEngine engine) {
        if (engine == null || engine.gameStatus == null || !engine.gameStatus.isGameOver()) {
            return null;
        }
        engine.gameStatus.setGameOver(false);
        engine.resetGameOverState();
        com.PVZ.model.status.AppStatus.returnToChapterAndLevelSelection("GAME OVER");
        return new OutputDTO(true,
            "The zombie ate your brain; LOSER!!! Returning to level select.");
    }

    private OutputDTO dispatchCommand(InGameInputDTO dto, RegularGameEngine engine) {
        return switch (dto.getCommand()) {
            case ADVANCE_TIME -> handleAdvanceTime(dto, engine);
            case COLLECT_SUN -> handleCollectSun(dto, engine);
            case SHOW_SUN_AMOUNT -> showSunAmount(engine);
            case CHEAT_ADD_SUNS -> handleCheatAddSuns(dto, engine);
            case CHEAT_REMOVE_COOLDOWN -> handleCheatRemoveCooldown(engine);
            case CHEAT_ADD_PLANT_FOOD -> handleCheatAddPlantFood(engine);
            case CHEAT_SPAWN_ZOMBIE -> cheatSpawnZombie(dto);
            case CHEAT_SET_WATER -> setTileWater(dto);
            case CHEAT_SET_DRY -> setTileDry(dto);
            case CHEAT_RELEASE_NUKE -> nukeAction();
            case PLANT_PLANT -> handlePlantPlant(dto, engine);
            case PLUCK_PLANT -> handlePluckPlant(dto, engine);
            case FEED_PLANT -> handleFeedPlant(dto, engine);
            case START_ZOMBIE_WAVES -> handleStartZombieWaves(dto, engine);
            case SHOW_MAP -> handleShowMap(engine);
            case SHOW_PLANTS_STATUS -> handleShowPlantsStatus(engine);
            case SHOW_TILE_STATUS -> handleShowTileStatus(dto, engine);
            case ZOMBIES_INFO -> zombiesInfo();
            case SHOW_TILE_DEBUG -> enableTileDebug();
            case HIDE_TILE_DEBUG -> disableTileDebug();
            case FREEZE_ZOMBIE -> zombieAction(dto, "freeze");
            case POISON_ZOMBIE -> zombieAction(dto, "poison");
            case HYPNOTIZE_ZOMBIE -> zombieAction(dto, "hypnotize");
            case KILL_ZOMBIE -> zombieAction(dto, "kill");
            case KILL_ALL_ZOMBIES -> killAllZombies();
            case EXIT -> exitToGameMenu();
            case SHOW_CURRENT_MENU -> showCurrentMenu();
        };
    }

    private OutputDTO handleAdvanceTime(InGameInputDTO dto, RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.advanceTimeText(dto.getTickCount()));
    }

    private OutputDTO handleCollectSun(InGameInputDTO dto, RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.collectSunAt(dto.getX(), dto.getY()));
    }

    private OutputDTO showSunAmount(RegularGameEngine engine) {
        User user = AppStatus.currentUser;
        if (user == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.showSunAmountText());
    }

    private OutputDTO handleCheatAddSuns(InGameInputDTO dto, RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.addSunsCheat(dto.getAmount()));
    }

    private OutputDTO handleCheatRemoveCooldown(RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.removeCooldownCheat());
    }

    private OutputDTO handleCheatAddPlantFood(RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.addPlantFoodCheat());
    }

    private OutputDTO nukeAction() {
        return new OutputDTO(true, "Nuke released.");
    }

    private OutputDTO handlePlantPlant(InGameInputDTO dto, RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        String result = engine.plantPlant(dto.getPlantType(), dto.getX(), dto.getY());
        return new OutputDTO(true, colorizeIfLocked(result));
    }

    private OutputDTO handlePluckPlant(InGameInputDTO dto, RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.pluckPlant(dto.getX(), dto.getY()));
    }

    private OutputDTO handleFeedPlant(InGameInputDTO dto, RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.feedPlant(dto.getX(), dto.getY()));
    }

    private OutputDTO handleStartZombieWaves(InGameInputDTO dto, RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        if (engine instanceof RegularGameEngine rge) {
            rge.startWaves();
        }
        return new OutputDTO(true, engine.startZombieWavesText());
    }

    private OutputDTO handleShowMap(RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.showMapText());
    }

    private OutputDTO handleShowPlantsStatus(RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.showPlantsStatusText());
    }

    private OutputDTO handleShowTileStatus(InGameInputDTO dto, RegularGameEngine engine) {
        if (engine == null) {
            return new OutputDTO(false, "Game engine is not ready.");
        }
        return new OutputDTO(true, engine.showTileStatusText(dto.getX(), dto.getY()));
    }

    private OutputDTO enableTileDebug() {
        com.PVZ.model.status.AppStatus.tileDebugEnabled = true;
        return new OutputDTO(true, "Tile debug overlay ON.");
    }

    private OutputDTO disableTileDebug() {
        com.PVZ.model.status.AppStatus.tileDebugEnabled = false;
        return new OutputDTO(true, "Tile debug overlay OFF.");
    }

    private OutputDTO showCurrentMenu() {
        return new OutputDTO(true, AppStatus.currentMenuType.name());
    }

    private RegularGameEngine getEngine() {
        if (AppStatus.getGameEngine() instanceof RegularGameEngine regularGameEngine) {
            return regularGameEngine;
        }
        return null;
    }

    private OutputDTO cheatSpawnZombie(InGameInputDTO dto) {
        GameEngine engine = AppStatus.getGameEngine();
        if (!(engine instanceof RegularGameEngine rge)) {
            return new OutputDTO(false, "Not in a regular game.");
        }

        String alias = dto.getZombieType();
        int row = dto.getY() != null ? dto.getY() : 2;
        int x = dto.getX() != null ? dto.getX() : 8;

        if (row < 0 || row > 4 || x < 0 || x > 8) {
            return new OutputDTO(false, "Invalid position: (" + x + ", " + row
                + "). Must be col 0-8, row 0-4.");
        }

        String resolvedAlias = alias;
        try {
            ZombieType.fromAlias(alias);
        } catch (IllegalArgumentException e) {
            resolvedAlias = Arrays.stream(ZombieType.values())
                .filter(z -> z.name().equalsIgnoreCase(alias))
                .findFirst()
                .map(z -> z.alias)
                .orElse(null);
            if (resolvedAlias == null) {
                return new OutputDTO(false, "Unknown zombie: " + alias);
            }
        }
        rge.getZombieEngine().spawnZombie(resolvedAlias, row, x);
        return new OutputDTO(true, "Zombie spawned: " + resolvedAlias + " at row " + row);
    }

    private OutputDTO setTileWater(InGameInputDTO dto) {
        RegularGameEngine rge = getEngine();
        if (rge == null) {
            return new OutputDTO(false, "Not in a regular game.");
        }
        int row = dto.getY();
        int col = dto.getX();
        rge.getBattleController().setTileTypeAt(row, col, TileType.WATER);
        return new OutputDTO(true, "Tile (" + col + ", " + row + ") set to WATER.");
    }

    private OutputDTO setTileDry(InGameInputDTO dto) {
        RegularGameEngine rge = getEngine();
        if (rge == null) {
            return new OutputDTO(false, "Not in a regular game.");
        }
        int row = dto.getY();
        int col = dto.getX();
        rge.getBattleController().setTileTypeAt(row, col, TileType.NORMAL);
        return new OutputDTO(true, "Tile (" + col + ", " + row + ") set to NORMAL.");
    }

    private OutputDTO zombiesInfo() {
        RegularGameEngine rge = getEngine();
        if (rge == null) {
            return new OutputDTO(false, "Not in a regular game.");
        }

        String details = zombiesInfoDetails();
        return new OutputDTO(true, details.isBlank() ? "No zombies." : details);
    }

    private String zombiesInfoDetails() {
        RegularGameEngine rge = getEngine();
        if (rge == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Zombie z : rge.getZombieEngine().getZombies()) {
            sb.append(z.getStatusString()).append("\n");
        }
        return sb.toString().trim();
    }

    private OutputDTO zombieAction(InGameInputDTO dto, String action) {
        RegularGameEngine rge = getEngine();
        if (rge == null) {
            return new OutputDTO(false, "Not in a regular game.");
        }
        if (dto.getX() == null || dto.getY() == null) {
            return new OutputDTO(false, "Location required: -l (col, row)");
        }

        BattleController bc = rge.getBattleController();
        Zombie z = bc.findZombieAt(dto.getX(), dto.getY());
        if (z == null) {
            return new OutputDTO(false, "No zombie at (" + dto.getX() + ", " + dto.getY()
                + ").");
        }

        return switch (action) {
            case "freeze" -> {
                z.freeze(3.0f);
                yield new OutputDTO(true, "Frozen zombie at (" + dto.getX() + ", " + dto.getY()
                    + ").");
            }
            case "poison" -> {
                z.poison(5.0f, 10.0f);
                yield new OutputDTO(true, "Poisoned zombie at (" + dto.getX() + ", " + dto.getY()
                    + ").");
            }
            case "hypnotize" -> {
                z.hypnotize(5.0f);
                yield new OutputDTO(true, "Hypnotized zombie at (" + dto.getX() + ", " + dto.getY()
                    + ").");
            }
            case "kill" -> {
                z.die(bc);
                yield new OutputDTO(true, "Killed zombie at (" + dto.getX() + ", " + dto.getY()
                    + ").");
            }
            default -> new OutputDTO(false, "Unknown action.");
        };
    }

    private OutputDTO killAllZombies() {
        RegularGameEngine rge = getEngine();
        if (rge == null) {
            return new OutputDTO(false, "Not in a regular game.");
        }

        BattleController bc = rge.getBattleController();
        List<Zombie> list = rge.getZombieEngine().getZombies();
        for (int i = list.size() - 1; i >= 0; i--) {
            list.get(i).die(bc);
        }
        return new OutputDTO(true, "All zombies killed.");
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.returnToChapterAndLevelSelection(null);
        return new OutputDTO(true, "Entered Game Menu.");
    }

    private String colorizeIfLocked(String message) {
        if (message != null && message.toLowerCase().contains("locked for this level")) {
            return "\u001B[33m" + message + "\u001B[0m";
        }
        return message;
    }
}
