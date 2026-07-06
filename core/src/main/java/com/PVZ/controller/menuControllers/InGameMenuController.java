package com.PVZ.controller.menuControllers;

import com.PVZ.model.entity.zombies.base.Zombie;
import java.util.List;
import com.PVZ.model.enums.InGameCommand;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.GameEngine;
import com.PVZ.model.game.RegularGameEngine;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.InGameInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class InGameMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof InGameInputDTO dto)) {
            return new OutputDTO(false, "Invalid input.");
        }
        if (dto.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }
        return switch (dto.getCommand()) {
            case ADVANCE_TIME -> new OutputDTO(true, "Advanced time by " + dto.getTickCount() + " ticks.");
            case COLLECT_SUN -> new OutputDTO(true, "Sun collected at the given location.");
            case SHOW_SUN_AMOUNT -> showSunAmount();
            case CHEAT_ADD_SUNS -> cheatAddSuns(dto.getAmount());
            case CHEAT_REMOVE_COOLDOWN -> new OutputDTO(true, "Cooldowns removed.");
            case CHEAT_ADD_PLANT_FOOD -> new OutputDTO(true, "Added one plant food.");
            case CHEAT_SPAWN_ZOMBIE -> {
                cheatSpawnZombie(dto);
                yield new OutputDTO(true, "Zombie spawned: " + dto.getZombieType());
            }
            case CHEAT_RELEASE_NUKE -> new OutputDTO(true, "Nuke released.");
            case PLANT_PLANT -> new OutputDTO(true, "Plant placed: " + dto.getPlantType());
            case PLUCK_PLANT -> new OutputDTO(true, "Plant plucked.");
            case FEED_PLANT -> new OutputDTO(true, "Plant fed.");
            case START_ZOMBIE_WAVES -> {
                if (AppStatus.getGameEngine() instanceof RegularGameEngine rge) {
                    rge.startWaves();
                }
                yield new OutputDTO(true, "Zombie waves started.");
            }
            case SHOW_MAP -> new OutputDTO(true, "Map shown.");
            case SHOW_PLANTS_STATUS -> new OutputDTO(true, "Plants status shown.");
            case SHOW_TILE_STATUS -> new OutputDTO(true, "Tile status shown.");
            case ZOMBIES_INFO -> zombiesInfo();
            case FREEZE_ZOMBIE -> zombieAction(dto, "freeze");
            case POISON_ZOMBIE -> zombieAction(dto, "poison");
            case KILL_ZOMBIE -> zombieAction(dto, "kill");
            case KILL_ALL_ZOMBIES -> killAllZombies();
            case EXIT -> exitToGameMenu();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
        };
    }

    private RegularGameEngine getRGE() {
        if (AppStatus.getGameEngine() instanceof RegularGameEngine rge) return rge;
        return null;
    }

    private OutputDTO zombiesInfo() {
        RegularGameEngine rge = getRGE();
        if (rge == null) return new OutputDTO(false, "Not in a regular game.");
        StringBuilder sb = new StringBuilder();
        for (Zombie z : rge.getZombieEngine().getZombies()) {
            sb.append(z.getStatusString()).append("\n");
        }
        return new OutputDTO(true, sb.isEmpty() ? "No zombies." : sb.toString());
    }

    private OutputDTO zombieAction(InGameInputDTO dto, String action) {
        RegularGameEngine rge = getRGE();
        if (rge == null) return new OutputDTO(false, "Not in a regular game.");
        if (dto.getX() == null || dto.getY() == null)
            return new OutputDTO(false, "Location required: -l (col, row)");
        BattleController bc = rge.getBattleController();
        Zombie z = bc.findZombieAt(dto.getX(), dto.getY());
        if (z == null) return new OutputDTO(false, "No zombie at (" + dto.getX() + ", " + dto.getY() + ").");
        return switch (action) {
            case "freeze" -> { z.freeze(3.0f); yield new OutputDTO(true, "Frozen zombie at (" + dto.getX() + ", " + dto.getY() + ")."); }
            case "poison" -> { z.poison(5.0f, 10.0f); yield new OutputDTO(true, "Poisoned zombie at (" + dto.getX() + ", " + dto.getY() + ")."); }
            case "kill" -> { z.die(bc); yield new OutputDTO(true, "Killed zombie at (" + dto.getX() + ", " + dto.getY() + ")."); }
            default -> new OutputDTO(false, "Unknown action.");
        };
    }

    private OutputDTO killAllZombies() {
        RegularGameEngine rge = getRGE();
        if (rge == null) return new OutputDTO(false, "Not in a regular game.");
        BattleController bc = rge.getBattleController();
        List<Zombie> list = rge.getZombieEngine().getZombies();
        for (int i = list.size() - 1; i >= 0; i--) {
            list.get(i).die(bc);
        }
        return new OutputDTO(true, "All zombies killed.");
    }

    private OutputDTO showSunAmount() {
        User user = AppStatus.currentUser;
        if (user == null || user.userStats == null) {
            return new OutputDTO(false, "You must be logged in.");
        }
        return new OutputDTO(true, "Sun amount: 0");
    }

    private OutputDTO cheatAddSuns(Integer amount) {
        if (amount == null || amount <= 0) {
            return new OutputDTO(false, "Invalid amount.");
        }
        return new OutputDTO(true, "Added " + amount + " suns.");
    }

    private OutputDTO cheatSpawnZombie(InGameInputDTO dto) {
        GameEngine engine = AppStatus.getGameEngine();
        if (!(engine instanceof RegularGameEngine rge)) {
            return new OutputDTO(false, "Not in a regular game.");
        }
        String alias = dto.getZombieType();
        int row = dto.getY() != null ? dto.getY() : 2;
        int x = dto.getX() != null ? dto.getX() : 8;
        rge.getZombieEngine().spawnZombie(alias, row, x);
        return new OutputDTO(true, "Zombie spawned: " + alias + " at row " + row);
    }

    private OutputDTO exitToGameMenu() {
        AppStatus.currentMenuType = MenuType.CHAPTER_AND_LEVEL_SELECTION;
        return new OutputDTO(true, "Entered Game Menu.");
    }
}
