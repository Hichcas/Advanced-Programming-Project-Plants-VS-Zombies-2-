package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.quest.Quest;
import com.PVZ.model.quest.QuestManager;
import com.PVZ.model.quest.Quest.Reward;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.view.input.DTO.QuestInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


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

        // قبل از هر عملیات، مطمئن شو کوئست‌های روزانه به‌روز باشند (ریست در صورت لزوم)
        qm.refreshDailyIfNeeded();

        return switch (dto.getCommand()) {
            case LIST -> listQuests(qm);
            case CLAIM -> claimQuest(qm, dto.getQuestId());
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToTravelLog();
        };
    }

    // ---------- نمایش لیست ----------
    private OutputDTO listQuests(QuestManager qm) {
        List<Quest> quests = qm.getActiveQuests();
        if (quests.isEmpty()) {
            return new OutputDTO(true, "No active quests right now. Time until daily reset: " + qm.getTimeUntilReset());
        }

        // مرتب‌سازی بر اساس اولویت (Critical > High > Medium > Low)
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
            sb.append("  Progress: ").append(q.getCurrentCount()).append("/").append(q.getTargetCount());
            if (q.isCompleted()) {
                if (q.isClaimed()) {
                    sb.append(" (Claimed)");
                } else {
                    sb.append(" (Ready to claim!)");
                }
            }
            sb.append("\n  Reward: ");
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

    // ---------- دریافت پاداش ----------
    private OutputDTO claimQuest(QuestManager qm, String questId) {
        if (questId == null || questId.isBlank())
            return new OutputDTO(false, "Quest ID is required. Usage: quest claim -i <id>");

        // پیدا کردن کوئست
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

        // اعمال پاداش
        applyReward(quest.getReward());
        quest.claim();  // علامت‌گذاری به‌عنوان دریافت‌شده

        return new OutputDTO(true, "Reward claimed: " + describeReward(quest.getReward()));
    }

    // ---------- اعمال پاداش ----------
    private void applyReward(Reward reward) {
        if (reward == null) return;
        User user = AppStatus.currentUser;
        if (user == null) return;

        switch (reward.getType()) {
            case COINS -> user.userStats.addCoins(reward.getAmount());
            case DIAMONDS -> user.userStats.addDiamonds(reward.getAmount());
            case UNLOCK_PLANT -> {
                // فرض می‌کنیم target اسم گیاه است (مثلاً "CABBAGE_PULT")
                if (reward.getTargetPlant() != null) {
                    try {
                        PlantType plant = reward.getTargetPlant();
                        user.collectionState.unlockPlant(plant);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid plant type in reward: " + reward.getTargetPlant());
                    }
                }
            }
            case SEED_PACKETS -> {
                if (user.collectionState != null && !user.collectionState.getUnlockedPlants().isEmpty()) {
                    PlantType[] unlocked = user.collectionState.getUnlockedPlants().toArray(new PlantType[0]);
                    PlantType randomPlant = unlocked[new java.util.Random().nextInt(unlocked.length)];
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

    private OutputDTO exitToTravelLog() {
        AppStatus.currentMenuType = MenuType.TRAVEL_LOG;
        return new OutputDTO(true, "Returned to Travel Log.");
    }
}
