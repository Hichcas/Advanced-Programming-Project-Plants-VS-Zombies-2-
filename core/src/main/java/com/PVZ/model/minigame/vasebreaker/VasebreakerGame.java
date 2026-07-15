package com.PVZ.model.minigame.vasebreaker;

import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantLibrary;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.ZombieType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VasebreakerGame {

    private final Vase[][] grid;
    private final int rows;
    private final int cols;
    private final List<String> zombiePool;
    private final List<PlantType> plantPool;
    private final float seedPacketLifetimeSeconds;
    private final List<DroppedSeedPacket> groundSeedPackets = new ArrayList<>();
    private final Random random = new Random();
    private final VasebreakerEngineCallback callback;
    private final VasebreakerLevelDefinition level;
    private int gargantuarVasesRemaining;
    private boolean finished;

    public VasebreakerGame(VasebreakerLevelDefinition level, VasebreakerEngineCallback callback) {
        this.rows = level.getRows();
        this.cols = level.getCols();
        this.grid = new Vase[rows][cols];
        this.zombiePool = resolveZombiePool(level.getZombiePool());
        this.plantPool = resolvePlantPool(level.getPlantPool());
        this.seedPacketLifetimeSeconds = level.getSeedPacketLifetimeSeconds();
        this.callback = callback;
        this.level = level;
        buildGrid(level);
    }

    private List<PlantType> resolvePlantPool(List<String> names) {
        List<PlantType> pool = new ArrayList<>();
        if (names != null && !names.isEmpty()) {
            for (String name : names) {
                try {
                    pool.add(PlantType.valueOf(name.trim()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (pool.isEmpty()) {
            for (PlantDefinition def : PlantLibrary.all()) {
                if (def != null && def.getType() != null) {
                    pool.add(def.getType());
                }
            }
        }
        if (pool.isEmpty()) {
            pool.add(PlantType.PEASHOOTER);
        }
        return pool;
    }

    private List<String> resolveZombiePool(List<String> names) {
        List<String> pool = new ArrayList<>();
        if (names != null && !names.isEmpty()) {
            pool.addAll(names);
        }
        if (pool.isEmpty()) {
            for (ZombieType type : ZombieType.values()) {
                pool.add(type.alias);
            }
        }
        if (pool.isEmpty()) {
            pool.add("ZombieTutorialDefault");
        }
        return pool;
    }

    private void buildGrid(VasebreakerLevelDefinition level) {
        // Decide whether to build a fixed board (explicit `vases` list with
        // random=false) or a fresh random board every run.
        boolean useFixedLayout = level.getRandom() != null && !level.getRandom()
                && level.getVases() != null && !level.getVases().isEmpty();

        int gargantuarCount = 0;

        if (useFixedLayout) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    grid[r][c] = null;
                }
            }
            for (String entry : level.getVases()) {
                String[] parts = entry.split(",");
                if (parts.length != 3)
                    continue;
                int r = Integer.parseInt(parts[0].trim());
                int c = Integer.parseInt(parts[1].trim());
                VaseType type = VaseType.valueOf(parts[2].trim());
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    grid[r][c] = new Vase(r, c, type);
                    if (type == VaseType.GARGANTUAR)
                        gargantuarCount++;
                }
            }
        } else {
            buildRandomGrid(level);
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (grid[r][c] != null && grid[r][c].getType() == VaseType.GARGANTUAR)
                        gargantuarCount++;
                }
            }
        }
        this.gargantuarVasesRemaining = gargantuarCount;
    }

    private void buildRandomGrid(VasebreakerLevelDefinition level) {
        // Start with an empty board.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = null;
            }
        }

        // Collect every cell and shuffle them so the vase placement is random.
        List<int[]> allCells = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                allCells.add(new int[]{r, c});
            }
        }
        java.util.Collections.shuffle(allCells, random);

        int total = rows * cols;
        int vaseCount = level.getVaseCount() != null ? level.getVaseCount() : total;
        vaseCount = Math.max(1, Math.min(vaseCount, total));

        double plantChance = level.getPlantVaseChance() != null ? level.getPlantVaseChance() : 0.20;
        double gargChance = level.getGargantuarVaseChance() != null ? level.getGargantuarVaseChance() : 0.08;
        // Keep gargantuar chance sensible relative to plant chance.
        gargChance = Math.min(gargChance, Math.max(0.0, 1.0 - plantChance));

        for (int i = 0; i < vaseCount; i++) {
            int[] cell = allCells.get(i);
            int r = cell[0];
            int c = cell[1];
            double roll = random.nextDouble();
            VaseType type;
            if (roll < gargChance) {
                type = VaseType.GARGANTUAR;
            } else if (roll < gargChance + plantChance) {
                type = VaseType.PLANT;
            } else {
                type = VaseType.NORMAL;
            }
            grid[r][c] = new Vase(r, c, type);
        }
    }

    public String breakVaseAt(int row, int col) {
        if (finished)
            return "game is over dige vaghean berid khonatoon";
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return "out of range";
        }
        Vase vase = grid[row][col];
        if (vase == null) {
            return "there is no vase in this cell";
        }
        if (vase.isBroken()) {
            return "this vase is already broken havaset kojast!";
        }

        return switch (vase.getType()) {
            case NORMAL -> breakNormalVase(vase);
            case PLANT -> breakPlantVase(vase);
            case GARGANTUAR -> breakGargantuarVase(vase);
        };
    }

    private String breakNormalVase(Vase vase) {
        double empty = Math.max(0.0, level.getNormalEmptyChance() != null ? level.getNormalEmptyChance() : 0.12);
        double zombie = Math.max(0.0, level.getNormalZombieChance() != null ? level.getNormalZombieChance() : 0.60);
        double seed = Math.max(0.0, level.getNormalSeedChance() != null ? level.getNormalSeedChance() : 0.28);
        double total = empty + zombie + seed;
        if (total <= 0.0) total = 1.0;
        double roll = random.nextDouble() * total;
        VaseOutcome outcome;
        String message;
        if (roll < empty) {
            outcome = VaseOutcome.EMPTY;
            message = "vase broke.rakab khordi :) It is empty";
        } else if (roll < empty + zombie) {
            outcome = VaseOutcome.ZOMBIE;
            String alias = releaseZombie(vase.getRow(), vase.getCol());
            message = "vase broke and (" + alias + ") spawned!";
        } else {
            outcome = VaseOutcome.SEED_PACKET;
            PlantType plantType = dropRandomSeedPacket(vase.getRow(), vase.getCol());
            message = "vase broke and plant" + plantType.getDisplayName() + " spawned";
        }
        vase.breakVase(outcome);
        checkWinCondition();
        return message;
    }

    private String breakPlantVase(Vase vase) {
        vase.breakVase(VaseOutcome.SEED_PACKET);
        PlantType plantType = dropRandomSeedPacket(vase.getRow(), vase.getCol());
        checkWinCondition();
        return "vase broke and plant" + plantType.getDisplayName() + " spawned";
    }

    private String breakGargantuarVase(Vase vase) {
        vase.breakVase(VaseOutcome.ZOMBIE);
        String alias = "ZombieGargantuarBasic";
        if (callback != null) {
            callback.releaseZombieFromVase(alias, vase.getRow(), vase.getCol());
        }
        gargantuarVasesRemaining--;
        checkWinCondition();
        return "Gargantuar vase broke";
    }

    private String releaseZombie(int row, int col) {
        String alias = zombiePool.get(random.nextInt(zombiePool.size()));
        if (callback != null) {
            callback.releaseZombieFromVase(alias, row, col);
        }
        return alias;
    }

    private PlantType dropRandomSeedPacket(int row, int col) {
        PlantType plantType = plantPool.get(random.nextInt(plantPool.size()));
        groundSeedPackets.add(new DroppedSeedPacket(plantType, row, col, seedPacketLifetimeSeconds));
        return plantType;
    }

    public void update(float delta) {
        groundSeedPackets.removeIf(packet -> {
            packet.tick(delta);
            return packet.isExpired();
        });
    }

    public boolean plantSeedAt(int row, int col) {
        DroppedSeedPacket found = null;
        for (DroppedSeedPacket packet : groundSeedPackets) {
            if (packet.getRow() == row && packet.getCol() == col) {
                found = packet;
                break;
            }
        }
        if (found == null)
            return false;
        groundSeedPackets.remove(found);
        if (callback != null) {
            callback.plantAt(row, col, found.getPlantType());
        }
        return true;
    }

    private void checkWinCondition() {
        for (Vase[] row : grid) {
            for (Vase v : row) {
                if (v != null && !v.isBroken())
                    return;
            }
        }
        boolean won = gargantuarVasesRemaining <= 0;
        if (won && !finished && callback != null) {
            callback.onLevelWon();
        }
        finished = won;
    }

    public Vase getVase(int row, int col) {
        return grid[row][col];
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public boolean isFinished() {
        return finished;
    }

    public List<DroppedSeedPacket> getGroundSeedPackets() {
        return groundSeedPackets;
    }
}

