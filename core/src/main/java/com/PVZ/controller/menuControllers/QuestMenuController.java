package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.ChapterEnum;
import com.PVZ.model.enums.ZombieType;
import com.PVZ.model.enums.PlantFamily;
import com.PVZ.model.quest.Quest;
import com.PVZ.model.quest.QuestManager;
import com.PVZ.model.quest.Quest.Reward;
import com.PVZ.model.quest.LevelResult;
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

        return switch (dto.getCommand()) {
            case LIST                -> listQuests(qm);
            case CLAIM               -> claimQuest(qm, dto.getQuestId());
            case DEBUG_SUN           -> debugSun(qm, dto.getParameter());
            case DEBUG_KILL          -> debugKill(qm, dto.getParameter());
            case DEBUG_PLANT         -> debugPlant(qm, dto.getParameter());
            case DEBUG_WIN           -> debugWin(qm);
            case DEBUG_RESET_DAILY   -> debugResetDaily(qm);
            case SHOW_CURRENT_MENU   -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT                -> exitToTravelLog();
        };
    }

    // -------------------- نمایش لیست --------------------
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

        StringBuilder sb = new StringBuilder();
        sb.append("Active Quests (Next daily reset in ").append(qm.getTimeUntilReset()).append("):\n");
        sb.append("--------------------------------------------------\n");

        for (Quest q : quests) {
            sb.append(String.format("[%s] %s (%s)\n", q.getId(), q.getTitle(), q.getType()));
            sb.append("  Description: ").append(q.getFormattedDescription()).append("\n");
            if (q.getTargetCount() == 0) {
                sb.append("  Progress: Conditional\n");
            } else {
                sb.append("  Progress: ").append(q.getCurrentCount()).append("/").append(q.getTargetCount());
                if (q.isCompleted()) {
                    sb.append(q.isClaimed() ? " (Claimed)\n" : " (Ready to claim!)\n");
                } else {
                    sb.append("\n");
                }
            }
            sb.append("  Reward: ");
            Reward r = q.getReward();
            if (r != null) {
                sb.append(describeReward(r));
            } else {
                sb.append("None");
            }
            sb.append("\n");
        }
        return new OutputDTO(true, sb.toString().trim());
    }

    // -------------------- دریافت پاداش --------------------
    private OutputDTO claimQuest(QuestManager qm, String questId) {
        if (questId == null || questId.isBlank())
            return new OutputDTO(false, "Quest ID is required. Usage: quest claim -i <id>");

        Optional<Quest> opt = qm.getActiveQuests().stream()
            .filter(q -> q.getId().equals(questId))
            .findFirst();

        if (opt.isEmpty())
            return new OutputDTO(false, "Quest not found.");

        Quest quest = opt.get();
        if (!quest.isCompleted())
            return new OutputDTO(false, "Quest not yet completed.");
        if (quest.isClaimed())
            return new OutputDTO(false, "Reward already claimed.");

        applyReward(quest.getReward());
        quest.claim();
        return new OutputDTO(true, "Reward claimed: " + describeReward(quest.getReward()));
    }

    // -------------------- اعمال پاداش --------------------
    private void applyReward(Reward reward) {
        if (reward == null) return;
        User user = AppStatus.currentUser;
        if (user == null) return;

        switch (reward.getType()) {
            case COINS -> user.userStats.addCoins(reward.getAmount());
            case DIAMONDS -> user.userStats.addDiamonds(reward.getAmount());
            case UNLOCK_PLANT -> {
                if (reward.getTargetPlant() != null) {
                    user.collectionState.unlockPlant(reward.getTargetPlant());
                }
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

    private String describeReward(Reward r) {
        return switch (r.getType()) {
            case COINS -> r.getAmount() + " coins";
            case DIAMONDS -> r.getAmount() + " diamonds";
            case UNLOCK_PLANT -> "Unlock plant: " + (r.getTargetPlant() != null ? r.getTargetPlant().getDisplayName() : "unknown");
            case SEED_PACKETS -> r.getAmount() + " seed packets (random)";
        };
    }

    // -------------------- Debug متدها --------------------
    private OutputDTO debugSun(QuestManager qm, String amountStr) {
        try {
            int amount = Integer.parseInt(amountStr);
            qm.onSunCollected(amount);
            return new OutputDTO(true, "Simulated sun collection: " + amount);
        } catch (NumberFormatException e) {
            return new OutputDTO(false, "Invalid amount.");
        }
    }

    private OutputDTO debugKill(QuestManager qm, String countStr) {
        try {
            int count = Integer.parseInt(countStr);
            // فرض می‌کنیم زامبی‌ها از فصل Ancient Egypt و نوع Mummy هستند
            qm.onZombieKilled(ZombieType.MUMMY_DEFAULT, ChapterEnum.ANCIENT_EGYPT, count);
            return new OutputDTO(true, "Simulated " + count + " zombie kills.");
        } catch (NumberFormatException e) {
            return new OutputDTO(false, "Invalid count.");
        }
    }

    private OutputDTO debugPlant(QuestManager qm, String plantName) {
        try {
            PlantType plant = PlantType.fromName(plantName);
            qm.onPlantPlaced(plant);
            return new OutputDTO(true, "Simulated planting: " + plant.getDisplayName());
        } catch (IllegalArgumentException e) {
            return new OutputDTO(false, "Unknown plant: " + plantName);
        }
    }

    private OutputDTO debugWin(QuestManager qm) {
        // شبیه‌سازی یک برد با پارامترهای تستی
        LevelResult res = new LevelResult();
        res.setWon(true);
        res.setFinalSunCount(0);                 // تست "استاد دفاع"
        res.setPlantsLost(1);                    // تست "گیاهخوار اقتصادی" (n=2)
        res.setZombiesKilledByLawnmower(12);     // تست "وقت چمن‌زنی" (n>=10)
        res.setDifficultyLevel(5);               // تست "برد پشت برد"
        res.setDayLevel(true);                   // تست "شب یا صبح"
        res.setPlantTypesUsed(List.of(PlantType.PUFF_SHROOM, PlantType.SUN_SHROOM)); // فقط قارچ → تست day_with_mushrooms
        res.setPlantFamiliesUsed(Set.of(PlantFamily.MUSHROOM, PlantFamily.SUN_PRODUCER));
        qm.evaluateEndLevelQuests(res);
        return new OutputDTO(true, "Simulated level win with test data.");
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
