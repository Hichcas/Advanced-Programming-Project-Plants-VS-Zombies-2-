package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Sun;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.game.chapter.ChapterConfig;
import com.PVZ.model.game.chapter.ChapterLibrary;
import com.PVZ.model.quest.LevelResult;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.UserRegistry;

public class UpdateHandler {

    private static final double TICK_SECONDS = 0.1;

    public static void update(RegularGameEngine engine, float delta) {
        if (engine.gameOverTriggered) {
            updateGameOverTimer(engine, delta);
            return;
        }
        if (engine.gameStatus != null && engine.gameStatus.isGameOver()) return;
        if (engine.waveManager != null) engine.waveManager.update(delta, engine.zombieEngine);
        if (engine.battleController != null) engine.battleController.update(delta);

        engine.tickAccumulator += delta;
        while (engine.tickAccumulator >= TICK_SECONDS) {
            engine.tickAccumulator -= TICK_SECONDS;
            advanceOneTick(engine);
        }

        if (engine.gameStatus != null && !engine.gameStatus.isGameOver() && !engine.gameStatus.isWon()
            && engine.waveManager != null && engine.waveManager.isFinished()) {
            boolean anyAlive = false;
            for (Zombie z : engine.getZombieList()) {
                if (z != null && !z.isDead()) { anyAlive = true; break; }
            }
            if (!anyAlive) {
                System.out.println("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                triggerGameOver(engine, true);
            }
        }
    }

    private static void advanceOneTick(RegularGameEngine engine) {
        if (engine.gameStatus != null && engine.gameStatus.isGameOver()) return;
        updatePlants(engine);
        updateProjectiles(engine, (float) TICK_SECONDS);
        updateSunManager(engine, (float) TICK_SECONDS);
        updateSkySun(engine, (float) TICK_SECONDS);
        BoardHandler.updateLawnMowers(engine, (float) TICK_SECONDS);
        WaveHandler.updateConveyorBelt(engine, (float) TICK_SECONDS);

        for (Zombie z : engine.getZombieList()) {
            if (z != null && !z.isDead()) z.updateEffects((float) TICK_SECONDS);
        }

        engine.rechargeRemaining.replaceAll((type, remaining) -> Math.max(0.0, remaining - TICK_SECONDS));

        if (engine.gameStatus != null) {
            engine.gameStatus.setRemainingZombieWaveInPercent(
                engine.waveManager != null ? engine.waveManager.getProgressPercent() : 0);
        }

        if (AppStatus.currentChapter != null) {
            AppStatus.currentChapter.update(engine.map, engine);
        }

        if (engine.specialLevel != null) {
            engine.specialLevel.onTick(engine, engine.map);
            if (engine.specialLevel.isLossConditionMet()) {
                System.out.println("[SpecialLevel] Loss condition met: " + engine.specialLevel.getName());
                triggerGameOver(engine, false);
            }
        }
    }

    private static void updatePlants(RegularGameEngine engine) {
        if (engine.map == null) return;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                Plant plant = engine.map.getPlantAt(row, col);
                if (plant == null) continue;
                plant.putRuntimeState("row", row);
                plant.putRuntimeState("col", col);
                plant.putRuntimeState("lane", row);

                Object freezeLv = plant.getRuntimeState("freezeLevel");
                boolean isFrozen = freezeLv instanceof Number && ((Number) freezeLv).intValue() >= 3;
                if (!isFrozen) plant.update(engine, TICK_SECONDS);

                if (plant.isDead()) {
                    if (plant.getStats().getBooleanExtra("explodeOnDeath", false)) {
                        CombatHandler.damageArea(engine, row, row,
                            Math.max(plant.getStats().getExplodeDamage(), plant.getStats().getDamage()));
                    }
                    engine.map.removePlant(row, col);
                    engine.questPlantsLost++;
                    if (engine.specialLevel != null) engine.specialLevel.onPlantDestroyed(row, col, engine);
                }
            }
        }
    }

    private static void updateProjectiles(RegularGameEngine engine, float delta) {
        if (engine.projectiles.isEmpty()) return;
        for (var p : engine.projectiles) p.update(delta);
        engine.projectiles.removeIf(Projectile::isDestroyed);
    }

    private static void updateSunManager(RegularGameEngine engine, float delta) {
        engine.sunManager.update(delta);
        engine.lootManager.update(delta);
    }

    private static void updateSkySun(RegularGameEngine engine, float delta) {
        if (engine.map == null || !engine.zombieWavesStarted) return;
        if (engine.gameStatus != null && engine.gameStatus.isNoSkySun()) return;
        engine.skySunTimer += delta;
        if (engine.skySunTimer < 10.0) return;
        engine.skySunTimer = 0.0;

        int row = engine.random.nextInt(5);
        int col = engine.random.nextInt(9);
        Tile tile = engine.map.getTile(row, col);
        if (tile == null) return;
        double landingX = tile.getX() + tile.getWidth() / 2.0;
        double groundY = tile.getY() + tile.getHeight() / 2.0;
        double startY = engine.map.getStartY() + engine.map.getTileHeight() * 2.0;

        // Sky sun type roll per spec: 80% normal (25 sun), 15% special (100 sun),
        // 5% radioactive (explodes if harvested mid-air; turns into a normal sun on landing).
        Sun.SunType type;
        int amount;
        int roll = engine.random.nextInt(100);
        if (roll < 80) {
            type = Sun.SunType.NORMAL;
            amount = 25;
        } else if (roll < 95) {
            type = Sun.SunType.SPECIAL;
            amount = 100;
        } else {
            type = Sun.SunType.RADIOACTIVE;
            amount = 25;
        }
        engine.sunManager.spawnFalling(landingX, startY, amount, groundY, type);
    }

    // ---------- Game Over ----------
    public static void triggerGameOver(RegularGameEngine engine, boolean win) {
        if (engine.gameOverTriggered) return;

        if (win && AppStatus.currentUser != null && AppStatus.currentUser.questState != null) {
            LevelResult res = new LevelResult();
            res.setWon(true);
            res.setFinalSunCount(engine.getSunCount());
            res.setPlantsLost(engine.questPlantsLost);
            res.setZombiesKilledByLawnmower(engine.questLawnmowerKills);
            res.setLawnlessCol1Kills(engine.questLawnlessCol1Kills);
            res.setDifficultyLevel(AppStatus.currentUser.appStats.getDifficultyLevel());
            res.setDayLevel(!engine.gameStatus.isNoSkySun());
            res.setFinalMap(engine.map);
            res.setPlantTypesUsed(new java.util.ArrayList<>(engine.questPlantTypesUsed));
            res.setPlantFamiliesUsed(new java.util.HashSet<>(engine.questPlantFamiliesUsed));
            AppStatus.currentUser.questState.getQuestManager().onLevelEnd(res);
        }

        engine.gameOverTriggered = true;
        engine.gameOverTimer = 0f;
        engine.gameOverWin = win;
        if (engine.gameStatus != null) {
            engine.gameStatus.setGameOver(true);
            engine.gameStatus.setWon(win);
        }
        if (win && AppStatus.currentUser != null && AppStatus.currentUser.userStats != null) {
            int score = engine.getSunCount() * 10;
            AppStatus.currentUser.userStats.updateHighestScore(score);
        }
    }

    public static void resetGameOverState(RegularGameEngine engine) {
        engine.gameOverTriggered = false;
        engine.gameOverNavigated = false;
        engine.gameOverTimer = 0f;
        engine.gameOverWin = false;
    }

    public static void updateGameOverTimer(RegularGameEngine engine, float delta) {
        if (!engine.gameOverTriggered || engine.gameOverNavigated) return;
        engine.gameOverTimer += delta;
        if (engine.gameOverTimer >= 1.5f) {
            engine.gameOverNavigated = true;
            if (engine.gameOverWin) {
                var stats = AppStatus.currentUser != null ? AppStatus.currentUser.userStats : null;
                if (stats != null) {
                    stats.incrementStagesCompleted();
                    stats.addCoins(100);
                    int diff = AppStatus.currentUser.appStats != null
                        ? AppStatus.currentUser.appStats.getDifficultyLevel() : 1;
                    if (diff >= 4) stats.addCoins(50);
                    if (diff == 5) stats.addDiamonds(5);
                }
                if (AppStatus.currentUser != null) {
                    ChapterEnum chapter = AppStatus.getCurrentChapterEnum();
                    if (chapter != null && AppStatus.currentUser.progressState != null) {
                        AppStatus.currentUser.progressState.completeLevel(chapter, AppStatus.currentStageNumber);
                        ChapterConfig config = ChapterLibrary.getChapterConfig(AppStatus.currentChapterName);
                        if (config != null) {
                            int maxStage = config.getStages().size();
                            if (AppStatus.currentStageNumber >= maxStage) {
                                ChapterEnum[] chapters = ChapterEnum.values();
                                for (int i = 0; i < chapters.length; i++) {
                                    if (chapters[i] == chapter && i + 1 < chapters.length) {
                                        AppStatus.currentUser.progressState.completeLevel(chapters[i + 1], 1);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    UserRegistry.touch(AppStatus.currentUser.profile.getUsername());
                }
            AppStatus.lastGameResultWin = engine.gameOverWin;
            AppStatus.currentMenuType = MenuType.END_OF_GAME;
            }
        }
    }

    public static void resetBoardAfterGameOver(RegularGameEngine engine) {
        engine.gameOverTriggered = false;
        engine.gameOverNavigated = false;
        engine.gameOverTimer = 0f;
        engine.gameOverWin = false;
        if (engine.gameStatus != null) {
            engine.gameStatus.setGameOver(false);
            engine.gameStatus.setWon(false);
        }
        if (engine.map != null) {
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 9; col++) {
                    engine.map.removePlant(row, col);
                }
            }
        }
        engine.projectiles.clear();
        engine.zombies.clear();
        if (engine.zombieEngine != null) engine.zombieEngine.getZombies().clear();
        BoardHandler.initLawnMowers(engine, engine.map);
        engine.zombieWavesStarted = false;
        engine.tickAccumulator = 0f;
        engine.selectedPlantType = null;
        engine.rechargeRemaining.clear();
        engine.conveyorBeltQueue.clear();
        engine.sunManager.clear();
        engine.lootManager.clear();
        engine.plantFoodManager.reset();
        if (engine.gameStatus != null) engine.gameStatus.setRemainingZombieWaveInPercent(0);
    }

    public static void advanceTicks(RegularGameEngine engine, int ticks) {
        int safeTicks = Math.max(0, ticks);
        for (int i = 0; i < safeTicks; i++) {
            update(engine, (float) TICK_SECONDS);
        }
    }
}
