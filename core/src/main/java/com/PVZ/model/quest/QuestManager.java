package com.PVZ.model.quest;

import com.PVZ.model.enums.*;
import com.PVZ.model.quest.Quest.Reward;
import com.PVZ.model.game.Map;
import com.PVZ.model.entity.Plant;

import java.time.*;
import java.util.*;

public class QuestManager {
    private List<Quest> activeQuests = new ArrayList<>();
    private LocalDate lastDailyRefresh;
    private int consecutiveMaxDifficultyWins = 0;

    private static final List<Quest> DAILY_TEMPLATES = new ArrayList<>();
    private static final List<Quest> STORY_TEMPLATES = new ArrayList<>();
    private static final List<Quest> EPIC_TEMPLATES = new ArrayList<>();

    static {
        DAILY_TEMPLATES.addAll(List.of(
            quest("daily_sun", "Daily Sun Catcher", "Collect {sun_amount} sun in one day",
                QuestType.DAILY, QuestPriority.MEDIUM, "collect_sun", 0,
                new Reward(Reward.RewardType.COINS, 0, null), null),
            quest("daily_kill_family", "Family Slaughter", "Kill zombies using only {family} plants",
                QuestType.DAILY, QuestPriority.MEDIUM, "family_kill_only", 0,
                new Reward(Reward.RewardType.COINS, 1000, null), null),
            quest("daily_no_family", "Blossom in Limitations", "Win without using any {family} plant",
                QuestType.DAILY, QuestPriority.HIGH, "no_family_used", 0,
                new Reward(Reward.RewardType.DIAMONDS, 100, null), null),
            quest("daily_symmetry", "Symmetry", "Final garden layout must be symmetric",
                QuestType.DAILY, QuestPriority.HIGH, "symmetry", 0,
                new Reward(Reward.RewardType.COINS, 500, null), null),
            quest("daily_no_symmetry", "No OCD", "No symmetry in garden except middle row",
                QuestType.DAILY, QuestPriority.MEDIUM, "no_symmetry", 0,
                new Reward(Reward.RewardType.COINS, 800, null), null),
            quest("daily_column_empty", "One Column Less", "Win with column {col} completely empty",
                QuestType.DAILY, QuestPriority.HIGH, "column_empty", 0,
                new Reward(Reward.RewardType.DIAMONDS, 10, null), null),
            quest("daily_row_empty", "Defenseless Row", "Win with row {row} completely empty",
                QuestType.DAILY, QuestPriority.HIGH, "row_empty", 0,
                new Reward(Reward.RewardType.DIAMONDS, 20, null), null),
            quest("daily_cross_empty", "Defenseless Cross", "Win with column {col} and row {row} empty",
                QuestType.DAILY, QuestPriority.HIGH, "cross_empty", 0,
                new Reward(Reward.RewardType.DIAMONDS, 25, null), null),
            quest("daily_plant_pro", "Pro with {plant}", "Kill 10 zombies using only {plant}",
                QuestType.DAILY, QuestPriority.HIGH, "specific_plant_kill", 10,
                new Reward(Reward.RewardType.SEED_PACKETS, 5, null), null),
            quest("daily_only_cactus", "Only Cactus", "Kill 10 zombies using only Cactus",
                QuestType.DAILY, QuestPriority.HIGH, "specific_plant_kill", 10,
                new Reward(Reward.RewardType.DIAMONDS, 20, null), null),
            quest("daily_explosive", "Demolition Expert", "Use 3 explosive plants in one level",
                QuestType.DAILY, QuestPriority.LOW, "use_explosive", 3,
                new Reward(Reward.RewardType.COINS, 100, null), null),
            quest("daily_sun_producers", "Cloudy Day", "Win with at most 3 sun producers",
                QuestType.DAILY, QuestPriority.HIGH, "max_sun_producers", 3,
                new Reward(Reward.RewardType.DIAMONDS, 10, null), null),
            quest("daily_streak", "Win Streak", "Win 5 levels in a row on maximum difficulty",
                QuestType.DAILY, QuestPriority.MEDIUM, "streak", 5,
                new Reward(Reward.RewardType.COINS, 5000, null), null),
            quest("daily_almost_win", "Almost Victory", "Kill 10 zombies in column 1 of a row without a lawnmower",
                QuestType.DAILY, QuestPriority.MEDIUM, "lawnless_col1_kill", 10,
                new Reward(Reward.RewardType.COINS, 300, null), null)
        ));

        STORY_TEMPLATES.addAll(List.of(
            quest("story_chapter_hunt", "Chapter Hunter", "Defeat 50 zombies from {chapter}",
                QuestType.STORY, QuestPriority.HIGH, "chapter_zombie_kill", 50,
                new Reward(Reward.RewardType.SEED_PACKETS, 10, null), null),
            quest("story_economy", "Economic Herbivore", "Win without losing more than {n} plants",
                QuestType.STORY, QuestPriority.HIGH, "max_plant_loss", 0,
                new Reward(Reward.RewardType.SEED_PACKETS, 20, null), null),
            quest("story_speed", "Speed Run", "Kill 10 zombies within 30 seconds of the first wave",
                QuestType.STORY, QuestPriority.MEDIUM, "speed_kill", 10,
                new Reward(Reward.RewardType.COINS, 500, null), null)
        ));

        EPIC_TEMPLATES.addAll(List.of(
            quest("epic_defense", "Master of Defense", "End a level with exactly 0 sun",
                QuestType.EPIC, QuestPriority.CRITICAL, "zero_sun_end", 0,
                new Reward(Reward.RewardType.DIAMONDS, 200, null), null),
            quest("epic_night_day", "Day or Night", "Win a day level using only mushrooms",
                QuestType.EPIC, QuestPriority.HIGH, "day_with_mushrooms", 0,
                new Reward(Reward.RewardType.DIAMONDS, 20, null), null),
            quest("epic_lawnmower", "Lawnmower Time", "Kill at least {n} zombies with lawnmowers",
                QuestType.EPIC, QuestPriority.MEDIUM, "lawnmower_kill", 0,
                new Reward(Reward.RewardType.DIAMONDS, 0, null), null)
        ));
    }

    private static Quest quest(String id, String title, String desc, QuestType type, QuestPriority prio,
                               String condKey, int target, Reward reward, java.util.Map<String, Object> params) {
        return new Quest(id, title, desc, type, prio, condKey, target, reward, params);
    }

    public QuestManager() {
        addInitialQuests();
    }

    private void addInitialQuests() {
        for (Quest template : STORY_TEMPLATES) {
            if (activeQuests.stream().noneMatch(q -> q.getId().equals(template.getId()) && q.isClaimed())) {
                Quest q = copyQuest(template, template.getId());
                if ("chapter_zombie_kill".equals(q.getConditionKey())) {
                    q.getParameters().put("chapter", randomChapter());
                }
                if ("max_plant_loss".equals(q.getConditionKey())) {
                    q.getParameters().put("n", new Random().nextInt(6));
                }
                activeQuests.add(q);
            }
        }
        for (Quest template : EPIC_TEMPLATES) {
            if (activeQuests.stream().noneMatch(q -> q.getId().equals(template.getId()) && q.isClaimed())) {
                Quest q = copyQuest(template, template.getId());
                if ("lawnmower_kill".equals(q.getConditionKey())) {
                    int n = 10 * (new Random().nextInt(5) + 1);
                    q.getParameters().put("n", n);
                    q.setTargetCount(n);
                }
                activeQuests.add(q);
            }
        }
    }

    private Quest copyQuest(Quest template, String newId) {
        return new Quest(newId, template.getTitle(), template.getDescriptionTemplate(),
            template.getType(), template.getPriority(), template.getConditionKey(),
            template.getTargetCount(), template.getReward(), template.getParameters());
    }

    private ChapterEnum randomChapter() {
        ChapterEnum[] chapters = ChapterEnum.values();
        return chapters[new Random().nextInt(chapters.length)];
    }

    private PlantFamily randomMintFamily() {
        return PlantFamily.MINT_FAMILIES[new Random().nextInt(PlantFamily.MINT_FAMILIES.length)];
    }

    private PlantType randomAttackingPlant() {
        List<PlantType> attackers = new ArrayList<>();
        for (PlantType p : PlantType.values()) {
            if (p == PlantType.CACTUS) continue;
            PlantFamily family = PlantFamilyMapper.getFamily(p);
            if (family == PlantFamily.SHOOTER || family == PlantFamily.MELEE ||
                family == PlantFamily.LOBBER || family == PlantFamily.EXPLOSIVE) {
                attackers.add(p);
            }
        }
        return attackers.isEmpty() ? PlantType.PEASHOOTER : attackers.get(new Random().nextInt(attackers.size()));
    }

    public void refreshDailyIfNeeded() {
        LocalDate today = LocalDate.now();
        if (lastDailyRefresh != null && lastDailyRefresh.equals(today)) return;

        activeQuests.removeIf(q -> q.getType() == QuestType.DAILY);
        List<Quest> shuffled = new ArrayList<>(DAILY_TEMPLATES);
        Collections.shuffle(shuffled);
        int toAdd = Math.min(3, shuffled.size());
        for (int i = 0; i < toAdd; i++) {
            Quest template = shuffled.get(i);
            Quest q = copyQuest(template, template.getId() + "_" + System.currentTimeMillis());

            switch (q.getConditionKey()) {
                case "collect_sun" -> {
                    int[] options = {3000, 4000, 5000};
                    int sun = options[new Random().nextInt(options.length)];
                    q.getParameters().put("sun_amount", sun);
                    q.setTargetCount(sun);
                    q.getReward().setAmount(sun / 100);
                }
                case "family_kill_only" -> q.getParameters().put("family", randomMintFamily());
                case "no_family_used" -> q.getParameters().put("family", randomMintFamily());
                case "column_empty" -> q.getParameters().put("col", new Random().nextInt(9) + 1);
                case "row_empty" -> q.getParameters().put("row", new Random().nextInt(5) + 1);
                case "cross_empty" -> {
                    q.getParameters().put("col", new Random().nextInt(9) + 1);
                    q.getParameters().put("row", new Random().nextInt(5) + 1);
                }
                case "specific_plant_kill" -> {
                    if (q.getId().startsWith("daily_plant_pro")) {
                        q.getParameters().put("plant", randomAttackingPlant());
                    } else if (q.getId().startsWith("daily_only_cactus")) {
                        q.getParameters().put("plant", PlantType.CACTUS);
                    }
                }
            }
            activeQuests.add(q);
        }
        lastDailyRefresh = today;
    }

    public String getTimeUntilReset() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = LocalDate.now().plusDays(1).atStartOfDay();
        Duration d = Duration.between(now, next);
        return d.toHours() + "h " + d.toMinutesPart() + "m";
    }

    // ========== event handlers ==========
    public void onZombieKilled(ZombieType zombieType, ChapterEnum chapter, int count) {
        for (Quest q : activeQuests) {
            if (q.isCompleted()) continue;
            if ("chapter_zombie_kill".equals(q.getConditionKey())) {
                ChapterEnum required = (ChapterEnum) q.getParameters().get("chapter");
                if (required == chapter) q.incrementProgress(count);
            }
        }
    }

    public void onSunCollected(int amount) {
        for (Quest q : activeQuests)
            if (!q.isCompleted() && "collect_sun".equals(q.getConditionKey()))
                q.incrementProgress(amount);
    }

    public void onPlantPlaced(PlantType plantType) {
        PlantFamily family = PlantFamilyMapper.getFamily(plantType);
        for (Quest q : activeQuests) {
            if (q.isCompleted()) continue;
            if ("use_explosive".equals(q.getConditionKey()) && family == PlantFamily.EXPLOSIVE)
                q.incrementProgress(1);
        }
    }

    public void onFirstWaveStarted() {
        long now = System.currentTimeMillis();
        for (Quest q : activeQuests)
            if (!q.isCompleted() && "speed_kill".equals(q.getConditionKey()))
                q.getRuntimeState().put("waveStartTime", now);
    }

    public void onZombieKilledInTimeWindow(long killTimeMillis) {
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && "speed_kill".equals(q.getConditionKey())) {
                Long start = (Long) q.getRuntimeState().get("waveStartTime");
                int sec = (int) q.getParameters().getOrDefault("seconds", 30);
                if (start != null && (killTimeMillis - start) <= sec * 1000L)
                    q.incrementProgress(1);
            }
        }
    }

    public void onZombieKilledByPlant(PlantType plantType) {
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && "specific_plant_kill".equals(q.getConditionKey())) {
                PlantType required = (PlantType) q.getParameters().get("plant");
                if (required == plantType) q.incrementProgress(1);
            }
        }
    }

    // ========== end-of-level evaluation ==========
    public void evaluateEndLevelQuests(LevelResult result) {
        if (!result.isWon()) {
            consecutiveMaxDifficultyWins = 0;
            return;
        }
        if (result.getDifficultyLevel() == 5) consecutiveMaxDifficultyWins++;
        else consecutiveMaxDifficultyWins = 0;

        for (Quest q : activeQuests) {
            if (q.isCompleted() || q.isClaimed()) continue;

            switch (q.getConditionKey()) {
                case "symmetry" -> { if (checkSymmetry(result.getFinalMap())) q.setCompleted(true); }
                case "no_symmetry" -> { if (checkNoSymmetry(result.getFinalMap())) q.setCompleted(true); }
                case "column_empty" -> {
                    int col = (int) q.getParameters().get("col");
                    if (isColumnEmpty(result.getFinalMap(), col)) q.setCompleted(true);
                }
                case "row_empty" -> {
                    int row = (int) q.getParameters().get("row");
                    if (isRowEmpty(result.getFinalMap(), row)) q.setCompleted(true);
                }
                case "cross_empty" -> {
                    int c = (int) q.getParameters().get("col");
                    int r = (int) q.getParameters().get("row");
                    if (isColumnEmpty(result.getFinalMap(), c) && isRowEmpty(result.getFinalMap(), r))
                        q.setCompleted(true);
                }
                case "zero_sun_end" -> { if (result.getFinalSunCount() == 0) q.setCompleted(true); }
                case "max_plant_loss" -> {
                    int maxLoss = (int) q.getParameters().get("n");
                    if (result.getPlantsLost() <= maxLoss) q.setCompleted(true);
                }
                case "day_with_mushrooms" -> {
                    if (result.isDayLevel() && result.getPlantTypesUsed().stream().allMatch(
                        pt -> PlantFamilyMapper.getFamily(pt) == PlantFamily.MUSHROOM))
                        q.setCompleted(true);
                }
                case "no_family_used" -> {
                    PlantFamily forbidden = (PlantFamily) q.getParameters().get("family");
                    if (!result.getPlantFamiliesUsed().contains(forbidden)) q.setCompleted(true);
                }
                case "lawnmower_kill" -> {
                    int needed = (int) q.getParameters().get("n");
                    if (result.getZombiesKilledByLawnmower() >= needed) q.setCompleted(true);
                }
                case "streak" -> { if (consecutiveMaxDifficultyWins >= q.getTargetCount()) q.setCompleted(true); }
            }
        }
        activeQuests.forEach(q -> q.getRuntimeState().clear());
    }

    // ---------- map helpers ----------
    private boolean checkSymmetry(Map map) {
        int cols = map.getCols(), rows = map.getRows();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols / 2; c++) {
                Plant left = map.getPlantAt(r, c);
                Plant right = map.getPlantAt(r, cols - 1 - c);
                if ((left == null) != (right == null)) return false;
                if (left != null && !left.getType().equals(right.getType())) return false;
            }
        return true;
    }

    private boolean checkNoSymmetry(Map map) {
        int cols = map.getCols(), rows = map.getRows(), midRow = rows / 2;
        for (int r = 0; r < rows; r++) {
            if (r == midRow) continue;
            for (int c = 0; c < cols / 2; c++) {
                Plant left = map.getPlantAt(r, c);
                Plant right = map.getPlantAt(r, cols - 1 - c);
                if ((left == null) != (right == null)) continue;
                if (left != null && left.getType().equals(right.getType())) return false;
            }
        }
        return true;
    }

    private boolean isColumnEmpty(Map map, int colIndex) {
        int col = colIndex - 1;
        for (int r = 0; r < map.getRows(); r++)
            if (map.getPlantAt(r, col) != null) return false;
        return true;
    }

    private boolean isRowEmpty(Map map, int rowIndex) {
        int row = rowIndex - 1;
        for (int c = 0; c < map.getCols(); c++)
            if (map.getPlantAt(row, c) != null) return false;
        return true;
    }

    public List<Quest> getActiveQuests() { return activeQuests; }

    public boolean claimQuest(String questId) {
        for (Quest q : activeQuests) {
            if (q.getId().equals(questId) && q.isCompleted() && !q.isClaimed()) {
                q.claim();
                return true;
            }
        }
        return false;
    }

    public LocalDate getLastDailyRefresh() { return lastDailyRefresh; }
    public void setLastDailyRefresh(LocalDate lastDailyRefresh) { this.lastDailyRefresh = lastDailyRefresh; }
}
