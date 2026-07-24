package com.PVZ.model.game;

import com.PVZ.model.entity.LawnMower;
import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.status.AppStatus;

import java.util.StringJoiner;

public class BoardHandler {

    // ---------- Lawn mowers ----------
    public static void initLawnMowers(RegularGameEngine engine, Map map) {
        if (map == null) return;
        float tileWidth = map.getTileWidth();
        float tileHeight = map.getTileHeight();
        float startX = map.getStartX();
        float startY = map.getStartY();
        double triggerX = startX - tileWidth * 0.75;
        double travelLimitX = 2560 + tileWidth;
        for (int row = 0; row < 5; row++) {
            LawnMower mower = new LawnMower();
            double parkY = startY - (row + 1) * tileHeight + tileHeight * 0.15;
            mower.init(row, parkY, triggerX, travelLimitX);
            engine.lawnMowers[row] = mower;
        }
    }

    public static void updateLawnMowers(RegularGameEngine engine, float delta) {
        for (LawnMower mower : engine.lawnMowers) {
            if (mower == null) continue;
            if (!mower.isTriggered() && !mower.isUsed()) {
                for (Zombie z : engine.getZombiesInLane(mower.getRow())) {
                    if (z != null && !z.isDead() && z.getX() <= mower.getFrontX()) {
                        mower.trigger();
                        engine.questLawnmowerKills++;
                        engine.getBattleController().notifyZombieKilled(z, null);
                        engine.zombieEngine.kill(z);
                        break;
                    }
                }
            }
            if (mower.isTriggered() && !mower.isUsed()) {
                mower.advance(delta);
                for (Zombie z : engine.getZombiesInLane(mower.getRow())) {
                    if (z != null && !z.isDead() && mower.getHitbox().overlaps(z.getHitbox())) {
                        engine.questLawnmowerKills++;
                        engine.getBattleController().notifyZombieKilled(z, null);
                        engine.zombieEngine.kill(z);
                    }
                }
            }
            if (mower.isUsed() && engine.gameStatus != null && !engine.gameStatus.isGameOver()) {
                for (Zombie z : engine.getZombiesInLane(mower.getRow())) {
                    if (z != null && !z.isDead() && z.getX() <= mower.getFrontX()) {
                        System.out.println("The zombie ate your brain; LOSER!!!");
                        engine.triggerGameOver(false);
                        return;
                    }
                }
            }
        }
    }

    // ---------- Map display ----------
    public static String showMapText(RegularGameEngine engine) {
        StringBuilder builder = new StringBuilder();
        builder.append("Sun: ").append(engine.getSunCount())
            .append(" | Wave: ").append(engine.waveManager == null ? 0 : engine.waveManager.getCurrentWave())
            .append('\n');
        if (engine.conveyorBeltMode) {
            builder.append("Belt: ").append(engine.conveyorBeltQueue.isEmpty() ? "(empty)" : formatBeltQueue(engine))
                .append('\n');
        }
        appendMapGrid(engine, builder);
        builder.append("Tile debug:\n");
        builder.append(tileDebugList(engine.map));
        return builder.toString().trim();
    }

    private static void appendMapGrid(RegularGameEngine engine, StringBuilder builder) {
        boolean[][] zombieAt = buildZombieGrid(engine);
        builder.append("Map:\n");
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                char cell = getMapCellChar(engine, row, col, zombieAt);
                builder.append(cell);
                if (col + 1 < 9) builder.append(' ');
            }
            builder.append('\n');
        }
    }

    private static boolean[][] buildZombieGrid(RegularGameEngine engine) {
        boolean[][] zombieAt = new boolean[5][9];
        for (Zombie z : engine.getZombieList()) {
            if (z == null || z.isDead()) continue;
            int row = (int) Math.round(z.getRow());
            int col = engine.map != null ? engine.map.worldToCol((float) z.getX()) : -1;
            if (row >= 0 && row < 5 && col >= 0 && col < 9) zombieAt[row][col] = true;
        }
        return zombieAt;
    }

    private static char getMapCellChar(RegularGameEngine engine, int row, int col, boolean[][] zombieAt) {
        Plant plant = engine.map == null ? null : engine.map.getPlantAt(row, col);
        if (zombieAt[row][col] && plant != null) return '#';
        else if (zombieAt[row][col]) return 'Z';
        else if (plant != null) return plant.getType().name().charAt(0);
        else if (engine.map != null) {
            Tile tile = engine.map.getTile(row, col);
            return tile == null ? '.' : tileDebugLabel(tile.getType()).charAt(0);
        } else return '.';
    }

    private static String formatBeltQueue(RegularGameEngine engine) {
        StringJoiner joiner = new StringJoiner(", ");
        for (PlantType type : engine.conveyorBeltQueue) joiner.add(type.getDisplayName());
        return joiner.toString();
    }

    public static String showPlantsStatusText(RegularGameEngine engine) {
        if (engine.map == null) return "Map is not ready.";
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Plant plant = engine.map.getPlantAt(row, col);
                if (plant != null) {
                    builder.append(plant.getType().getDisplayName())
                        .append(" at (").append(col).append(", ").append(row).append(") hp=")
                        .append(plant.getCurrentHp())
                        .append(plant.isPlantFoodActive() ? " [plant food]" : "");
                    Object fl = plant.getRuntimeState("freezeLevel");
                    if (fl instanceof Number && ((Number) fl).intValue() >= 3) {
                        builder.append(" [FROZEN freezelv=").append(fl)
                            .append(" iceHp=").append(plant.getRuntimeState("iceHp")).append("]");
                    }
                    builder.append("\n");
                }
            }
        }
        return builder.length() == 0 ? "No plants." : builder.toString().trim();
    }

    public static String showTileStatusText(RegularGameEngine engine, int x, int y) {
        if (engine.map == null) return "Map is not ready.";
        int row = engine.normalizeIndex(y);
        int col = engine.normalizeIndex(x);
        if (!engine.map.isWithinBounds(row, col)) return "Invalid tile.";
        Tile tile = engine.map.getTile(row, col);
        Plant plant = tile == null ? null : tile.getPlant();
        if (plant == null) return "Tile (" + col + ", " + row + ") is empty.";
        return "Tile (" + col + ", " + row + ") contains " + plant.getType().getDisplayName()
            + " hp=" + plant.getCurrentHp()
            + (plant.isPlantFoodActive() ? " [plant food]" : "");
    }

    // ---------- Tile debug utilities ----------
    public static String tileDebugLabel(TileType type) {
        if (type == null) return ".";
        return switch (type) {
            case TOMBSTONE -> "T";
            case WATER -> "~";
            case TIDE -> "^";
            case ICE -> "*";
            case SLIPPERY_UP -> "U";
            case SLIPPERY_DOWN -> "D";
            case NECROMANCY -> "N";
            case LOW_COAST -> "L";
            case CRATER -> "C";
            default -> ".";
        };
    }

    public static String tileDebugList(Map map) {
        if (map == null) return "  (none)\n";
        StringBuilder builder = new StringBuilder();
        boolean any = false;
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getCols(); c++) {
                Tile tile = map.getTile(r, c);
                if (tile == null || tile.getType() == TileType.NORMAL) continue;
                any = true;
                builder.append("  (").append(c).append(", ").append(r).append(") = ")
                    .append(tile.getType().name());
                if (tile.getType() == TileType.TOMBSTONE) builder.append(" hp=").append(tile.getHp());
                builder.append('\n');
            }
        }
        if (!any) builder.append("  (none)\n");
        return builder.toString();
    }
}
