package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.TileType;

import java.util.StringJoiner;

/**
 * Handler for map-related display and debug operations.
 * Used by RegularGameEngine for showMapText, showPlantsStatusText, etc.
 */
public class MapHandler {

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
        String tideInfo = formatTideInfo(engine);
        if (!tideInfo.isEmpty()) builder.append(tideInfo).append('\n');
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
        if (engine.map == null) return '.';
        Tile tile = engine.map.getTile(row, col);
        if (tile == null) return '.';
        Plant topPlant = tile.getPlant();
        Plant basePlant = tile.getBasePlant();
        boolean hasOctopus = tile.getOctopusHp() > 0;
        if (hasOctopus && zombieAt[row][col]) return '@';
        if (hasOctopus) return 'O';
        if (zombieAt[row][col] && topPlant != null) return '#';
        if (zombieAt[row][col]) return 'Z';
        if (topPlant != null && basePlant != null) return 'S';
        if (topPlant != null) {
            char c = topPlant.getType().name().charAt(0);
            return c;
        }
        if (basePlant != null) return basePlant.getType().name().charAt(0);
        return tileDebugLabel(tile.getType()).charAt(0);
    }

    private static String formatBeltQueue(RegularGameEngine engine) {
        StringJoiner joiner = new StringJoiner(", ");
        for (PlantType type : engine.conveyorBeltQueue) joiner.add(type.getDisplayName());
        return joiner.toString();
    }

    // ---------- Plant and tile status ----------
    public static String showPlantsStatusText(RegularGameEngine engine) {
        if (engine.map == null) return "Map is not ready.";
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Plant basePlant = engine.map.getBasePlantAt(row, col);
                if (basePlant != null) {
                    builder.append("[BASE] ").append(basePlant.getType().getDisplayName())
                        .append(" at (").append(col).append(", ").append(row).append(") hp=")
                        .append(basePlant.getCurrentHp())
                        .append("\n");
                }
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
        if (tile == null) return "Invalid tile.";

        StringBuilder sb = new StringBuilder();
        sb.append("Tile (").append(col).append(", ").append(row).append("): ");
        sb.append("type=").append(tile.getType().name());

        if (tile.getType() == TileType.WATER) sb.append(" [Water]");
        else if (tile.getType() == TileType.TIDE) sb.append(" [Tide]");
        else if (tile.getType() == TileType.LOW_COAST) sb.append(" [Low Coast]");

        Plant base = tile.getBasePlant();
        if (base != null) {
            sb.append(" | Base: ").append(base.getType().getDisplayName())
              .append(" hp=").append(base.getCurrentHp());
        }

        Plant top = tile.getPlant();
        if (top != null) {
            sb.append(" | Plant: ").append(top.getType().getDisplayName())
              .append(" hp=").append(top.getCurrentHp());
            if (top.isPlantFoodActive()) sb.append(" [plant food]");
            Object fl = top.getRuntimeState("freezeLevel");
            if (fl instanceof Number && ((Number) fl).intValue() >= 3) {
                sb.append(" [FROZEN]");
            }
        } else if (base == null) {
            sb.append(" | Empty");
        }

        int octoHp = tile.getOctopusHp();
        if (octoHp > 0) {
            sb.append(" | Octopus hp=").append(octoHp).append(" (shoot to free plant!)");
        }

        return sb.toString();
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

    public static String formatTideInfo(RegularGameEngine engine) {
        if (engine.map == null) return "";
        StringBuilder sb = new StringBuilder("Tide: ");
        for (int c = 0; c < 9; c++) {
            boolean isWater = false;
            for (int r = 0; r < 5; r++) {
                Tile t = engine.map.getTile(r, c);
                if (t != null && (t.getType() == TileType.WATER || t.getType() == TileType.TIDE)) {
                    isWater = true;
                    break;
                }
            }
            sb.append(isWater ? '~' : '.');
        }
        return sb.toString();
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
