package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.*;
import com.PVZ.model.quest.*;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.QuestInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.time.LocalDate;
import java.util.*;

public class QuestMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof QuestInputDTO dto))
            return new OutputDTO(false, "Invalid input.");
        if (dto.getCommand() == null)
            return new OutputDTO(false, "Invalid Command.");

        User user = AppStatus.currentUser;
        if (user == null || user.questState == null)
            return new OutputDTO(false, "You must be logged in and have quest data.");

        QuestManager qm = user.questState.getQuestManager();
        qm.refreshDailyIfNeeded();
        qm.updateChapterQuests();

        return switch (dto.getCommand()) {
            case LIST                -> listQuests(qm);
            case CLAIM               -> claimQuest(qm, dto.getQuestId());
            case DEBUG_SUN           -> debugSun(qm, dto.getParameter());
            case DEBUG_KILL          -> debugKill(qm, dto.getParameter());
            case DEBUG_PLANT         -> debugPlant(qm, dto.getParameter());
            case DEBUG_KILLBY        -> debugKillBy(qm, dto.getParameter());
            case DEBUG_SPEEDKILL     -> debugSpeedKill(qm, dto.getParameter());
            case DEBUG_LAWNMOWER     -> debugLawnmower(qm, dto.getParameter());
            case DEBUG_WIN           -> debugWin(qm, dto.getParameter());
            case DEBUG_RESET_DAILY   -> debugResetDaily(qm);
            case SHOW_CURRENT_MENU   -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT                -> exitToTravelLog();
        };
    }

    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String PURPLE = "\u001B[35m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RESET  = "\u001B[0m";

    private int statusGroup(Quest q) {
        if (q.isCompleted() && !q.isClaimed()) return 0;
        if (q.isClaimed()) return 2;
        return 1;
    }

    private String colorFor(Quest q) {
        if (q.isClaimed()) return GREEN;
        if (q.isCompleted()) return PURPLE;
        if (q.getCurrentCount() > 0) return YELLOW;
        return RED;
    }

    private OutputDTO listQuests(QuestManager qm) {
        List<Quest> quests = qm.getActiveQuests();
        if (quests.isEmpty()) {
            return new OutputDTO(true, "No active quests right now. Time until daily reset: " + qm.getTimeUntilReset());
        }

        quests.sort(Comparator.comparingInt(q -> switch (q.getPriority()) {
            case CRITICAL -> 0;
            case HIGH -> 1;
            case MEDIUM -> 2;
            case LOW -> 3;
        }));
        quests.sort(Comparator.comparingInt(this::statusGroup));

        StringBuilder sb = new StringBuilder();
        sb.append("Active Quests (Next daily reset in ").append(qm.getTimeUntilReset()).append("):\n");
        sb.append("--------------------------------------------------\n");

        for (Quest q : quests) {
            String color = colorFor(q);
            sb.append(color);
            sb.append(String.format("[%s] %s (%s)\n", q.getId(), q.getFormattedTitle(), q.getType()));
            sb.append("  Description: ").append(q.getFormattedDescription()).append("\n");
            if (q.getTargetCount() == 0) {
                sb.append("  Progress: Conditional");
                if (q.isCompleted()) {
                    sb.append(q.isClaimed() ? " (Claimed)\n" : " (Ready to claim!)\n");
                } else {
                    sb.append("\n");
                }
            } else {
                sb.append("  Progress: ").append(q.getCurrentCount()).append("/").append(q.getTargetCount());
                if (q.isCompleted()) {
                    sb.append(q.isClaimed() ? " (Claimed)\n" : " (Ready to claim!)\n");
                } else {
                    sb.append("\n");
                }
            }
            sb.append("  Reward: ");
            Quest.Reward r = q.getReward();
            sb.append(r != null ? describeReward(r) : "None");
            sb.append("\n").append(RESET);
        }
        return new OutputDTO(true, sb.toString().trim());
    }

    private OutputDTO claimQuest(QuestManager qm, String questId) {
        if (questId == null || questId.isBlank())
            return new OutputDTO(false, "Quest ID is required. Usage: quest claim -i <id>");

        Optional<Quest> opt = qm.getActiveQuests().stream()
            .filter(q -> q.getId().equals(questId))
            .findFirst();

        if (opt.isEmpty()) return new OutputDTO(false, "Quest not found.");
        Quest quest = opt.get();
        if (!quest.isCompleted()) return new OutputDTO(false, "Quest not yet completed.");
        if (quest.isClaimed()) return new OutputDTO(false, "Reward already claimed.");

        applyReward(quest.getReward());
        quest.claim();
        return new OutputDTO(true, "Reward claimed: " + describeReward(quest.getReward()));
    }

    private void applyReward(Quest.Reward reward) {
        if (reward == null) return;
        User user = AppStatus.currentUser;
        if (user == null) return;

        switch (reward.getType()) {
            case COINS -> user.userStats.addCoins(reward.getAmount());
            case DIAMONDS -> user.userStats.addDiamonds(reward.getAmount());
            case UNLOCK_PLANT -> {
                if (reward.getTargetPlant() != null)
                    user.collectionState.unlockPlant(reward.getTargetPlant());
            }
            case SEED_PACKETS -> {
                if (user.collectionState != null && !user.collectionState.getUnlockedPlants().isEmpty()) {
                    PlantType[] unlocked = user.collectionState.getUnlockedPlants().toArray(new PlantType[0]);
                    PlantType randomPlant = unlocked[new Random().nextInt(unlocked.length)];
                    user.collectionState.addSeedPackets(randomPlant, reward.getAmount());
                }
            }
        }
    }

    private String describeReward(Quest.Reward r) {
        return switch (r.getType()) {
            case COINS -> r.getAmount() + " coins";
            case DIAMONDS -> r.getAmount() + " diamonds";
            case UNLOCK_PLANT -> "Unlock plant: " + (r.getTargetPlant() != null ? r.getTargetPlant().getDisplayName() : "unknown");
            case SEED_PACKETS -> r.getAmount() + " seed packets (random)";
        };
    }

    // -------------------- Debug متدهای جدید --------------------
    private OutputDTO debugSun(QuestManager qm, String amountStr) {
        try {
            int amount = Integer.parseInt(amountStr);
            qm.onSunCollected(amount);
            return new OutputDTO(true, "Simulated sun collection: " + amount);
        } catch (NumberFormatException e) { return new OutputDTO(false, "Invalid amount."); }
    }

    private OutputDTO debugKill(QuestManager qm, String countStr) {
        try {
            int count = Integer.parseInt(countStr);
            ChapterEnum chapter = ChapterEnum.ANCIENT_EGYPT;
            for (Quest q : qm.getActiveQuests()) {
                if ("chapter_zombie_kill".equals(q.getConditionKey()) && !q.isCompleted()) {
                    Object param = q.getParameters().get("chapter");
                    if (param instanceof ChapterEnum) chapter = (ChapterEnum) param;
                    else if (param instanceof String) chapter = ChapterEnum.valueOf((String) param);
                    break;
                }
            }
            qm.onZombieKilled(ZombieType.MUMMY_DEFAULT, chapter, count);
            return new OutputDTO(true, "Simulated " + count + " zombie kills in " + chapter.getDisplayName());
        } catch (NumberFormatException e) { return new OutputDTO(false, "Invalid count."); }
    }

    private OutputDTO debugPlant(QuestManager qm, String plantName) {
        try {
            PlantType plant = PlantType.fromName(plantName);
            qm.onPlantPlaced(plant);
            return new OutputDTO(true, "Simulated planting: " + plant.getDisplayName());
        } catch (IllegalArgumentException e) { return new OutputDTO(false, "Unknown plant: " + plantName); }
    }

    private OutputDTO debugKillBy(QuestManager qm, String param) {
        String[] parts = param.split(" ");
        if (parts.length != 2) return new OutputDTO(false, "Usage: quest debug killby <plant> <count>");
        try {
            PlantType plant = PlantType.fromName(parts[0]);
            int count = Integer.parseInt(parts[1]);
            for (int i = 0; i < count; i++) qm.onZombieKilledByPlant(plant);
            return new OutputDTO(true, "Simulated " + count + " kills by " + plant.getDisplayName());
        } catch (Exception e) { return new OutputDTO(false, e.getMessage()); }
    }

    private OutputDTO debugSpeedKill(QuestManager qm, String countStr) {
        try {
            int count = Integer.parseInt(countStr);
            qm.onFirstWaveStarted();
            long now = System.currentTimeMillis();
            for (int i = 0; i < count; i++) qm.onZombieKilledInTimeWindow(now);
            return new OutputDTO(true, "Simulated " + count + " speed kills.");
        } catch (NumberFormatException e) { return new OutputDTO(false, "Invalid count."); }
    }

    private OutputDTO debugLawnmower(QuestManager qm, String countStr) {
        try {
            int count = Integer.parseInt(countStr);
            LevelResult res = new LevelResult();
            res.setWon(true);
            res.setZombiesKilledByLawnmower(count);
            res.setDifficultyLevel(1);
            res.setDayLevel(false);
            res.setPlantTypesUsed(List.of());
            res.setPlantFamiliesUsed(Set.of());
            qm.evaluateEndLevelQuests(res);
            return new OutputDTO(true, "Simulated " + count + " lawnmower kills at end of level.");
        } catch (NumberFormatException e) { return new OutputDTO(false, "Invalid count."); }
    }

    private OutputDTO debugWin(QuestManager qm, String args) {
        // Parse optional parameters from args string
        int lawnmower = 0, col1kills = 0, sunProducers = 0, difficulty = 1;
        boolean dayLevel = false;
        String mapType = null;
        int mapCol = -1, mapRow = -1;

        if (args != null && !args.isEmpty()) {
            String[] tokens = args.split("\\s+");
            for (int i = 0; i < tokens.length; i++) {
                switch (tokens[i]) {
                    case "--lawnmower": lawnmower = Integer.parseInt(tokens[++i]); break;
                    case "--col1kills": col1kills = Integer.parseInt(tokens[++i]); break;
                    case "--sunproducers": sunProducers = Integer.parseInt(tokens[++i]); break;
                    case "--difficulty": difficulty = Integer.parseInt(tokens[++i]); break;
                    case "--day": dayLevel = Boolean.parseBoolean(tokens[++i]); break;
                    case "--map": mapType = tokens[++i]; break;
                    case "--col": mapCol = Integer.parseInt(tokens[++i]); break;
                    case "--row": mapRow = Integer.parseInt(tokens[++i]); break;
                }
            }
        }

        LevelResult res = new LevelResult();
        res.setWon(true);
        res.setFinalSunCount(0);
        res.setPlantsLost(1);
        res.setZombiesKilledByLawnmower(lawnmower);
        res.setDifficultyLevel(difficulty);
        res.setDayLevel(dayLevel);
        res.setPlantTypesUsed(List.of(PlantType.PUFF_SHROOM, PlantType.SUN_SHROOM));
        res.setPlantFamiliesUsed(Set.of(PlantFamily.MUSHROOM, PlantFamily.SUN_PRODUCER));
        res.setLawnlessCol1Kills(col1kills);

        if (mapType != null || mapCol != -1 || mapRow != -1) {
            res.setFinalMap(createMockMap(mapType, mapCol, mapRow, sunProducers));
        }

        qm.evaluateEndLevelQuests(res);
        return new OutputDTO(true, "Simulated win with parameters: " + args);
    }

    private com.PVZ.model.game.Map createMockMap(String mapType, int col, int row, int sunProducers) {
        // Simple 5x9 empty map
        com.PVZ.model.game.Map map = new com.PVZ.model.game.Map(0, 0, 900, 500, 5, 9);
        if ("symmetry".equals(mapType)) {
            map.setPlant(0, 0, PlantType.PEASHOOTER.create(1));
            map.setPlant(0, 8, PlantType.PEASHOOTER.create(1));
        } else if ("nosymmetry".equals(mapType)) {
            map.setPlant(0, 0, PlantType.PEASHOOTER.create(1));
            map.setPlant(0, 8, PlantType.SUNFLOWER.create(1));
        }
        if (sunProducers > 0) {
            for (int i = 0; i < sunProducers; i++) {
                int r = i / 9;
                int c = i % 9;
                if (r < 5 && c < 9) map.setPlant(r, c, PlantType.SUNFLOWER.create(1));
            }
        }
        return map;
    }

    private OutputDTO debugResetDaily(QuestManager qm) {
        qm.setLastDailyRefresh(LocalDate.now().minusDays(1));
        qm.refreshDailyIfNeeded();
        return new OutputDTO(true, "Daily quests reset manually.");
    }

    private OutputDTO exitToTravelLog() {
        AppStatus.currentMenuType = MenuType.TRAVEL_LOG;
        return new OutputDTO(true, "Returned to Travel Log.");
    }
}
