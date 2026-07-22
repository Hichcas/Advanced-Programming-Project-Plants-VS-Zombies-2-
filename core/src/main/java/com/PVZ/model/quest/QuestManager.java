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

    static { initTemplates(); }

    private static void initTemplates() {
        // Daily templates
        DAILY_TEMPLATES.addAll(List.of(
            quest("daily_sun", "Daily Sun Catcher", "Collect {sun_amount} sun in one day", QuestType.DAILY, QuestPriority.MEDIUM, "collect_sun", 0, new Reward(Reward.RewardType.COINS, 0, null), null),
            quest("daily_kill_family", "Family Slaughter", "Kill zombies using only {family} plants", QuestType.DAILY, QuestPriority.MEDIUM, "family_kill_only", 0, new Reward(Reward.RewardType.COINS, 1000, null), null),
            quest("daily_no_family", "Blossom in Limitations", "Win without using any {family} plant", QuestType.DAILY, QuestPriority.HIGH, "no_family_used", 0, new Reward(Reward.RewardType.DIAMONDS, 100, null), null),
            quest("daily_symmetry", "Symmetry", "Final garden layout must be symmetric", QuestType.DAILY, QuestPriority.HIGH, "symmetry", 0, new Reward(Reward.RewardType.COINS, 500, null), null),
            quest("daily_no_symmetry", "No OCD", "No symmetry in garden except middle row", QuestType.DAILY, QuestPriority.MEDIUM, "no_symmetry", 0, new Reward(Reward.RewardType.COINS, 800, null), null),
            quest("daily_column_empty", "One Column Less", "Win with column {col} completely empty", QuestType.DAILY, QuestPriority.HIGH, "column_empty", 0, new Reward(Reward.RewardType.DIAMONDS, 10, null), null),
            quest("daily_row_empty", "Defenseless Row", "Win with row {row} completely empty", QuestType.DAILY, QuestPriority.HIGH, "row_empty", 0, new Reward(Reward.RewardType.DIAMONDS, 20, null), null),
            quest("daily_cross_empty", "Defenseless Cross", "Win with column {col} and row {row} empty", QuestType.DAILY, QuestPriority.HIGH, "cross_empty", 0, new Reward(Reward.RewardType.DIAMONDS, 25, null), null),
            quest("daily_plant_pro", "Pro with {plant}", "Kill 10 zombies using only {plant}", QuestType.DAILY, QuestPriority.HIGH, "specific_plant_kill", 10, new Reward(Reward.RewardType.SEED_PACKETS, 5, null), null),
            quest("daily_only_cactus", "Only Cactus", "Kill 10 zombies using only Cactus", QuestType.DAILY, QuestPriority.HIGH, "specific_plant_kill", 10, new Reward(Reward.RewardType.DIAMONDS, 20, null), null),
            quest("daily_explosive", "Demolition Expert", "Use 3 explosive plants in one level", QuestType.DAILY, QuestPriority.LOW, "use_explosive", 3, new Reward(Reward.RewardType.COINS, 100, null), null),
            quest("daily_sun_producers", "Cloudy Day", "Win with at most 3 sun producers", QuestType.DAILY, QuestPriority.HIGH, "max_sun_producers", 3, new Reward(Reward.RewardType.DIAMONDS, 10, null), null),
            quest("daily_streak", "Win Streak", "Win 5 levels in a row on maximum difficulty", QuestType.DAILY, QuestPriority.MEDIUM, "streak", 5, new Reward(Reward.RewardType.COINS, 5000, null), null),
            quest("daily_almost_win", "Almost Victory", "Kill 10 zombies in column 1 of a row without a lawnmower", QuestType.DAILY, QuestPriority.MEDIUM, "lawnless_col1_kill", 10, new Reward(Reward.RewardType.COINS, 300, null), null)
        ));
        STORY_TEMPLATES.addAll(List.of(
            quest("story_economy", "Economic Herbivore", "Win without losing more than {n} plants", QuestType.STORY, QuestPriority.HIGH, "max_plant_loss", 0, new Reward(Reward.RewardType.SEED_PACKETS, 20, null), null),
            quest("story_speed", "Speed Run", "Kill 10 zombies within 30 seconds of the first wave", QuestType.STORY, QuestPriority.MEDIUM, "speed_kill", 10, new Reward(Reward.RewardType.COINS, 500, null), null)
        ));
        EPIC_TEMPLATES.addAll(List.of(
            quest("epic_defense", "Master of Defense", "End a level with exactly 0 sun", QuestType.EPIC, QuestPriority.CRITICAL, "zero_sun_end", 0, new Reward(Reward.RewardType.DIAMONDS, 200, null), null),
            quest("epic_night_day", "Day or Night", "Win a day level using only mushrooms", QuestType.EPIC, QuestPriority.HIGH, "day_with_mushrooms", 0, new Reward(Reward.RewardType.DIAMONDS, 20, null), null),
            quest("epic_lawnmower", "Lawnmower Time", "Kill at least {n} zombies with lawnmowers", QuestType.EPIC, QuestPriority.MEDIUM, "lawnmower_kill", 0, new Reward(Reward.RewardType.DIAMONDS, 0, null), null)
        ));
    }

    private static Quest quest(String id, String title, String desc, QuestType type, QuestPriority prio,
                               String condKey, int target, Reward reward, java.util.Map<String, Object> params) {
        return new Quest(id, title, desc, type, prio, condKey, target, reward, params);
    }

    public QuestManager() { addInitialQuests(); }

    private void addInitialQuests() {
        for (Quest template : STORY_TEMPLATES) {
            if (activeQuests.stream().noneMatch(q -> q.getId().equals(template.getId()) && q.isClaimed())) {
                Quest q = copyQuest(template, template.getId());
                if ("max_plant_loss".equals(q.getConditionKey()))
                    q.getParameters().put("n", new Random().nextInt(6));
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
                    q.getReward().setAmount(n);
                }
                activeQuests.add(q);
            }
        }
        updateChapterQuests();
    }

    public void updateChapterQuests() {
        ChapterEnum[] chapters = ChapterEnum.values();
        boolean anyClaimed = activeQuests.stream().anyMatch(q -> q.getId().startsWith("story_chapter_hunt_") && q.isClaimed());
        String firstId = "story_chapter_hunt_" + chapters[0].name();
        boolean firstExists = activeQuests.stream().anyMatch(q -> q.getId().equals(firstId));
        if (!anyClaimed && !firstExists) addChapterQuest(chapters[0]);
        for (int i = 1; i < chapters.length; i++) {
            String prevId = "story_chapter_hunt_" + chapters[i-1].name();
            String currId = "story_chapter_hunt_" + chapters[i].name();
            boolean prevClaimed = activeQuests.stream().anyMatch(q -> q.getId().equals(prevId) && q.isClaimed());
            boolean currExists = activeQuests.stream().anyMatch(q -> q.getId().equals(currId));
            if (prevClaimed && !currExists) addChapterQuest(chapters[i]);
        }
    }

    private void addChapterQuest(ChapterEnum chapter) {
        activeQuests.add(new Quest("story_chapter_hunt_" + chapter.name(), "Chapter Hunter",
            "Defeat 50 zombies from {chapter}", QuestType.STORY, QuestPriority.HIGH,
            "chapter_zombie_kill", 50, new Reward(Reward.RewardType.SEED_PACKETS, 10, null),
            java.util.Map.of("chapter", chapter)));
    }

    private Quest copyQuest(Quest template, String newId) {
        Reward r = template.getReward();
        Reward rewardCopy = (r != null) ? new Reward(r.getType(), r.getAmount(), r.getTargetPlant()) : null;
        return new Quest(newId, template.getTitle(), template.getDescriptionTemplate(),
            template.getType(), template.getPriority(), template.getConditionKey(),
            template.getTargetCount(), rewardCopy, template.getParameters());
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> T getParamAsEnum(java.util.Map<String, Object> params, String key, Class<T> enumClass, T fallback) {
        Object obj = params.get(key);
        if (obj == null) return fallback;
        if (enumClass.isInstance(obj)) return (T) obj;
        if (obj instanceof String) {
            try { return Enum.valueOf(enumClass, (String) obj); } catch (IllegalArgumentException e) { return fallback; }
        }
        return fallback;
    }

    private ChapterEnum getChapterParam(Quest q) { return getParamAsEnum(q.getParameters(), "chapter", ChapterEnum.class, ChapterEnum.ANCIENT_EGYPT); }
    private PlantType getPlantParam(Quest q) { return getParamAsEnum(q.getParameters(), "plant", PlantType.class, PlantType.PEASHOOTER); }
    private PlantFamily getFamilyParam(Quest q) { return getParamAsEnum(q.getParameters(), "family", PlantFamily.class, PlantFamily.GENERAL); }

    private PlantFamily randomMintFamily() { return PlantFamily.MINT_FAMILIES[new Random().nextInt(PlantFamily.MINT_FAMILIES.length)]; }
    private PlantType randomAttackingPlant() {
        List<PlantType> attackers = new ArrayList<>();
        for (PlantType p : PlantType.values()) {
            if (p == PlantType.CACTUS) continue;
            PlantFamily family = PlantFamilyMapper.getFamily(p);
            if (family == PlantFamily.SHOOTER || family == PlantFamily.MELEE ||
                family == PlantFamily.LOBBER || family == PlantFamily.EXPLOSIVE)
                attackers.add(p);
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
                case "collect_sun": {
                    int[] options = {3000, 4000, 5000};
                    int sun = options[new Random().nextInt(options.length)];
                    q.getParameters().put("sun_amount", sun);
                    q.setTargetCount(sun);
                    q.getReward().setAmount(sun / 100);
                    break;
                }
                case "family_kill_only": q.getParameters().put("family", randomMintFamily()); break;
                case "no_family_used": q.getParameters().put("family", randomMintFamily()); break;
                case "column_empty": q.getParameters().put("col", new Random().nextInt(9) + 1); break;
                case "row_empty": q.getParameters().put("row", new Random().nextInt(5) + 1); break;
                case "cross_empty": {
                    q.getParameters().put("col", new Random().nextInt(9) + 1);
                    q.getParameters().put("row", new Random().nextInt(5) + 1);
                    break;
                }
                case "specific_plant_kill": {
                    if (q.getId().startsWith("daily_plant_pro")) q.getParameters().put("plant", randomAttackingPlant());
                    else if (q.getId().startsWith("daily_only_cactus")) q.getParameters().put("plant", PlantType.CACTUS);
                    break;
                }
                default: break;
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

    public void onZombieKilled(ZombieType zombieType, ChapterEnum chapter, int count) {
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && "chapter_zombie_kill".equals(q.getConditionKey()) && getChapterParam(q) == chapter)
                q.incrementProgress(count);
        }
    }
    public void onSunCollected(int amount) {
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && "collect_sun".equals(q.getConditionKey())) q.incrementProgress(amount);
        }
    }
    public void onPlantPlaced(PlantType plantType) {
        PlantFamily family = PlantFamilyMapper.getFamily(plantType);
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && "use_explosive".equals(q.getConditionKey()) && family == PlantFamily.EXPLOSIVE)
                q.incrementProgress(1);
        }
    }
    public void onFirstWaveStarted() {
        long now = System.currentTimeMillis();
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && "speed_kill".equals(q.getConditionKey()))
                q.getRuntimeState().put("waveStartTime", now);
        }
    }
    public void onZombieKilledInTimeWindow(long killTimeMillis) {
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && "speed_kill".equals(q.getConditionKey())) {
                Long start = (Long) q.getRuntimeState().get("waveStartTime");
                int sec = (int) q.getParameters().getOrDefault("seconds", 30);
                if (start != null && (killTimeMillis - start) <= sec * 1000L) q.incrementProgress(1);
            }
        }
    }
    public void onZombieKilledByPlant(PlantType plantType) {
        PlantFamily family = PlantFamilyMapper.getFamily(plantType);
        for (Quest q : activeQuests) {
            if (q.isCompleted()) continue;
            if ("specific_plant_kill".equals(q.getConditionKey()) && getPlantParam(q) == plantType)
                q.incrementProgress(1);
            if ("family_kill_only".equals(q.getConditionKey()) && family != getFamilyParam(q))
                q.getRuntimeState().put("familyViolated", true);
        }
    }

    public void evaluateEndLevelQuests(LevelResult result) {
        if (!result.isWon()) { consecutiveMaxDifficultyWins = 0; return; }
        if (result.getDifficultyLevel() == 5) consecutiveMaxDifficultyWins++;
        else consecutiveMaxDifficultyWins = 0;

        for (Quest q : activeQuests) {
            if (q.isCompleted() || q.isClaimed()) continue;
            String key = q.getConditionKey();
            switch (key) {
                case "symmetry": if (result.getFinalMap() != null && checkSymmetry(result.getFinalMap())) q.setCompleted(true); break;
                case "no_symmetry": if (result.getFinalMap() != null && checkNoSymmetry(result.getFinalMap())) q.setCompleted(true); break;
                case "column_empty": if (result.getFinalMap() != null && isColumnEmpty(result.getFinalMap(), (int) q.getParameters().get("col"))) q.setCompleted(true); break;
                case "row_empty": if (result.getFinalMap() != null && isRowEmpty(result.getFinalMap(), (int) q.getParameters().get("row"))) q.setCompleted(true); break;
                case "cross_empty": {
                    if (result.getFinalMap() != null) {
                        int c = (int) q.getParameters().get("col");
                        int r = (int) q.getParameters().get("row");
                        if (isColumnEmpty(result.getFinalMap(), c) && isRowEmpty(result.getFinalMap(), r))
                            q.setCompleted(true);
                    }
                    break;
                }
                case "zero_sun_end": if (result.getFinalSunCount() == 0) q.setCompleted(true); break;
                case "max_plant_loss": if (result.getPlantsLost() <= (int) q.getParameters().get("n")) q.setCompleted(true); break;
                case "day_with_mushrooms": if (result.isDayLevel() && result.getPlantTypesUsed().stream().allMatch(pt -> PlantFamilyMapper.getFamily(pt) == PlantFamily.MUSHROOM)) q.setCompleted(true); break;
                case "no_family_used": if (!result.getPlantFamiliesUsed().contains(getFamilyParam(q))) q.setCompleted(true); break;
                case "lawnmower_kill": { int kills = result.getZombiesKilledByLawnmower(); if (kills > 0) q.incrementProgress(kills); break; }
                case "lawnless_col1_kill": { int kills = result.getLawnlessCol1Kills(); if (kills > 0) q.incrementProgress(kills); break; }
                case "streak": if (consecutiveMaxDifficultyWins >= q.getTargetCount()) { q.setCurrentCount(q.getTargetCount()); q.setCompleted(true); } break;
                case "family_kill_only": { Boolean violated = (Boolean) q.getRuntimeState().get("familyViolated"); if (violated == null || !violated) q.setCompleted(true); break; }
                case "max_sun_producers": if (result.getFinalMap() != null && countSunProducers(result.getFinalMap()) <= q.getTargetCount()) q.setCompleted(true); break;
                case "speed_kill": case "use_explosive": if (!q.isCompleted()) q.resetProgress(); break;
                default: break;
            }
        }
        activeQuests.forEach(q -> q.getRuntimeState().clear());
    }

    private boolean checkSymmetry(Map map) {
        int cols = map.getCols(), rows = map.getRows();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols / 2; c++) {
                Plant left = map.getPlantAt(r, c);
                Plant right = map.getPlantAt(r, cols - 1 - c);
                if ((left == null) != (right == null)) return false;
                if (left != null && !left.getType().equals(right.getType())) return false;
            }
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
        for (int r = 0; r < map.getRows(); r++) if (map.getPlantAt(r, col) != null) return false;
        return true;
    }

    private boolean isRowEmpty(Map map, int rowIndex) {
        int row = rowIndex - 1;
        for (int c = 0; c < map.getCols(); c++) if (map.getPlantAt(row, c) != null) return false;
        return true;
    }

    private int countSunProducers(Map map) {
        int count = 0;
        for (int r = 0; r < map.getRows(); r++) {
            for (int c = 0; c < map.getCols(); c++) {
                Plant p = map.getPlantAt(r, c);
                if (p != null && PlantFamilyMapper.getFamily(p.getType()) == PlantFamily.SUN_PRODUCER) count++;
            }
        }
        return count;
    }

    public List<Quest> getActiveQuests() {
        for (Quest q : activeQuests) {
            if ("lawnmower_kill".equals(q.getConditionKey()) && q.getReward() != null
                && q.getReward().getAmount() == 0 && q.getTargetCount() > 0)
                q.getReward().setAmount(q.getTargetCount());
        }
        return activeQuests;
    }

    public boolean claimQuest(String questId) {
        for (Quest q : activeQuests) {
            if (q.getId().equals(questId) && q.isCompleted() && !q.isClaimed()) {
                q.claim();
                updateChapterQuests();
                return true;
            }
        }
        return false;
    }

    public LocalDate getLastDailyRefresh() { return lastDailyRefresh; }
    public void setLastDailyRefresh(LocalDate lastDailyRefresh) { this.lastDailyRefresh = lastDailyRefresh; }
}
