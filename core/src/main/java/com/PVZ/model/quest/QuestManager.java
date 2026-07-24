package com.PVZ.model.quest;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.enums.*;
import com.PVZ.model.game.Map;
import com.PVZ.model.quest.Quest.Reward;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuestManager {
    private List<Quest> activeQuests = new ArrayList<>();
    private LocalDate lastDailyRefresh;
    private int consecutiveMaxDifficultyWins = 0;
    private ChapterEnum currentChapter = ChapterEnum.ANCIENT_EGYPT;
    private int currentDifficulty = 1;
    private boolean isDayLevel = true;
    private static final List<Quest> DAILY_TEMPLATES = new ArrayList<>();
    private static final List<Quest> STORY_TEMPLATES = new ArrayList<>();
    private static final List<Quest> EPIC_TEMPLATES = new ArrayList<>();

    static {
        DAILY_TEMPLATES.addAll(List.of(
            quest("daily_sun", "Daily Sun Catcher", "Collect {sun_amount} sun in one day",
                QuestType.DAILY, QuestPriority.MEDIUM, "collect_sun", 0,
                new Reward(Reward.RewardType.COINS, 0, null), null),
            quest("daily_kill_family", "Family Slaughter",
                "Kill zombies using only {family} plants",
                QuestType.DAILY, QuestPriority.MEDIUM, "family_kill_only", 0,
                new Reward(Reward.RewardType.COINS, 1000, null), null),
            quest("daily_no_family", "Blossom in Limitations",
                "Win without using any {family} plant",
                QuestType.DAILY, QuestPriority.HIGH, "no_family_used", 0,
                new Reward(Reward.RewardType.DIAMONDS, 100, null), null),
            quest("daily_symmetry", "Symmetry",
                "Final garden layout must be symmetric",
                QuestType.DAILY, QuestPriority.HIGH, "symmetry", 0,
                new Reward(Reward.RewardType.COINS, 500, null), null),
            quest("daily_no_symmetry", "No OCD",
                "No symmetry in garden except middle row",
                QuestType.DAILY, QuestPriority.MEDIUM, "no_symmetry", 0,
                new Reward(Reward.RewardType.COINS, 800, null), null),
            quest("daily_column_empty", "One Column Less",
                "Win with column {col} completely empty",
                QuestType.DAILY, QuestPriority.HIGH, "column_empty", 0,
                new Reward(Reward.RewardType.DIAMONDS, 10, null), null),
            quest("daily_row_empty", "Defenseless Row",
                "Win with row {row} completely empty",
                QuestType.DAILY, QuestPriority.HIGH, "row_empty", 0,
                new Reward(Reward.RewardType.DIAMONDS, 20, null), null),
            quest("daily_cross_empty", "Defenseless Cross",
                "Win with column {col} and row {row} empty",
                QuestType.DAILY, QuestPriority.HIGH, "cross_empty", 0,
                new Reward(Reward.RewardType.DIAMONDS, 25, null), null),
            quest("daily_plant_pro", "Pro with {plant}",
                "Kill 10 zombies using only {plant}",
                QuestType.DAILY, QuestPriority.HIGH, "specific_plant_kill", 10,
                new Reward(Reward.RewardType.UNLOCK_PLANT, 0, null), null),
            quest("daily_only_cactus", "Only Cactus",
                "Kill 10 zombies using only Cactus",
                QuestType.DAILY, QuestPriority.HIGH, "specific_plant_kill", 10,
                new Reward(Reward.RewardType.DIAMONDS, 20, null), null),
            quest("daily_explosive", "Demolition Expert",
                "Use 3 explosive plants in one level",
                QuestType.DAILY, QuestPriority.LOW, "use_explosive", 3,
                new Reward(Reward.RewardType.COINS, 100, null), null),
            quest("daily_sun_producers", "Cloudy Day",
                "Win with at most 3 sun producers",
                QuestType.DAILY, QuestPriority.HIGH, "max_sun_producers", 3,
                new Reward(Reward.RewardType.DIAMONDS, 10, null), null),
            quest("daily_streak", "Win Streak",
                "Win 5 levels in a row on maximum difficulty",
                QuestType.DAILY, QuestPriority.MEDIUM, "streak", 5,
                new Reward(Reward.RewardType.COINS, 5000, null), null),
            quest("daily_almost_win", "Almost Victory",
                "Kill 10 zombies in column 1 of a row without a lawnmower",
                QuestType.DAILY, QuestPriority.MEDIUM, "lawnless_col1_kill", 10,
                new Reward(Reward.RewardType.COINS, 300, null), null)
        ));

        STORY_TEMPLATES.addAll(List.of(
            quest("story_economy", "Economic Herbivore",
                "Win without losing more than {n} plants",
                QuestType.STORY, QuestPriority.HIGH, "max_plant_loss", 0,
                new Reward(Reward.RewardType.SEED_PACKETS, 20, null), null),
            quest("story_speed", "Speed Run",
                "Kill 10 zombies within 30 seconds of the first wave",
                QuestType.STORY, QuestPriority.MEDIUM, "speed_kill", 10,
                new Reward(Reward.RewardType.COINS, 500, null), null)
        ));

        EPIC_TEMPLATES.addAll(List.of(
            quest("epic_defense", "Master of Defense",
                "End a level with exactly 0 sun",
                QuestType.EPIC, QuestPriority.CRITICAL, "zero_sun_end", 0,
                new Reward(Reward.RewardType.DIAMONDS, 200, null), null),
            quest("epic_night_day", "Day or Night",
                "Win a day level using only mushrooms",
                QuestType.EPIC, QuestPriority.HIGH, "day_with_mushrooms", 0,
                new Reward(Reward.RewardType.DIAMONDS, 20, null), null),
            quest("epic_lawnmower", "Lawnmower Time",
                "Kill at least {n} zombies with lawnmowers",
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

    public void onLevelStart(ChapterEnum chapter, int difficulty, boolean isDay) {
        this.currentChapter = chapter;
        this.currentDifficulty = difficulty;
        this.isDayLevel = isDay;
    }

    public void onFirstWaveStarted() {
        long now = System.currentTimeMillis();
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && "speed_kill".equals(q.getConditionKey())) {
                q.getRuntimeState().put("waveStartTime", now);
            }
        }
    }

    public void onSunCollected(int amount) {
        for (Quest q : activeQuests) {
            if (!q.isCompleted() && "collect_sun".equals(q.getConditionKey())) {
                q.incrementProgress(amount);
            }
        }
    }

    public void onPlantPlaced(PlantType plantType) {
        PlantFamily family = PlantFamilyMapper.getFamily(plantType);
        for (Quest q : activeQuests) {
            if (q.isCompleted()) continue;
            if ("use_explosive".equals(q.getConditionKey()) && family == PlantFamily.EXPLOSIVE) {
                q.incrementProgress(1);
            }
        }
    }

    public void onZombieKilled(PlantType killerPlant) {
        long now = System.currentTimeMillis();
        for (Quest q : activeQuests) {
            if (q.isCompleted()) continue;
            String key = q.getConditionKey();

            if ("chapter_zombie_kill".equals(key)) {
                if (getChapterParam(q) == currentChapter) q.incrementProgress(1);
            }
            if (killerPlant != null && "specific_plant_kill".equals(key)) {
                if (getPlantParam(q) == killerPlant) q.incrementProgress(1);
            }
            if (killerPlant != null && "family_kill_only".equals(key)) {
                PlantFamily required = getFamilyParam(q);
                PlantFamily family = PlantFamilyMapper.getFamily(killerPlant);
                if (family != required) q.getRuntimeState().put("familyViolated", true);
                q.getRuntimeState().put("anyKill", true);
            }
            if ("speed_kill".equals(key)) {
                Long start = (Long) q.getRuntimeState().get("waveStartTime");
                int sec = (int) q.getParameters().getOrDefault("seconds", 30);
                if (start != null && (now - start) <= sec * 1000L) {
                    q.incrementProgress(1);
                }
            }
        }
    }

    public void onLevelEnd(LevelResult result) {
        if (!result.isWon()) {
            consecutiveMaxDifficultyWins = 0;
            return;
        }
        if (currentDifficulty == 5) consecutiveMaxDifficultyWins++;
        else consecutiveMaxDifficultyWins = 0;

        for (Quest q : activeQuests) {
            if (q.isCompleted() || q.isClaimed()) continue;
            evaluateQuestCondition(q, result);
        }
        activeQuests.forEach(q -> q.getRuntimeState().clear());
    }

    public void refreshDailyIfNeeded() {
        LocalDate today = LocalDate.now();
        if (lastDailyRefresh != null && lastDailyRefresh.equals(today)) return;

        activeQuests.removeIf(q -> q.getType() == QuestType.DAILY);
        List<Quest> shuffled = new ArrayList<>(DAILY_TEMPLATES);
        Collections.shuffle(shuffled);
        int toAdd = Math.min(3, shuffled.size());
        for (int i = 0; i < toAdd; i++) {
            activeQuests.add(generateDailyQuest(shuffled.get(i)));
        }
        lastDailyRefresh = today;
    }

    private Quest generateDailyQuest(Quest template) {
        Quest q = copyQuest(template, template.getId() + "_" + System.currentTimeMillis());
        switch (q.getConditionKey()) {
            case "collect_sun" -> {
                int[] opts = {3000, 4000, 5000};
                int sun = opts[new Random().nextInt(opts.length)];
                q.getParameters().put("sun_amount", sun);
                q.setTargetCount(sun);
                q.getReward().setAmount(sun / 100);
            }
            case "family_kill_only", "no_family_used" -> q.getParameters().put("family", randomMintFamily());
            case "column_empty" -> q.getParameters().put("col", new Random().nextInt(9) + 1);
            case "row_empty" -> q.getParameters().put("row", new Random().nextInt(5) + 1);
            case "cross_empty" -> {
                q.getParameters().put("col", new Random().nextInt(9) + 1);
                q.getParameters().put("row", new Random().nextInt(5) + 1);
            }
            case "specific_plant_kill" -> {
                if (q.getId().startsWith("daily_plant_pro")) {
                    PlantType p = randomAttackingPlant();
                    q.getParameters().put("plant", p);
                    q.setReward(new Reward(Reward.RewardType.UNLOCK_PLANT, 0, p));
                } else {
                    q.getParameters().put("plant", PlantType.CACTUS);
                }
            }
        }
        return q;
    }

    public String getTimeUntilReset() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = LocalDate.now().plusDays(1).atStartOfDay();
        Duration d = Duration.between(now, next);
        return d.toHours() + "h " + d.toMinutesPart() + "m";
    }

    public List<Quest> getActiveQuests() {
        for (Quest q : activeQuests) {
            if ("lawnmower_kill".equals(q.getConditionKey()) && q.getReward() != null
                && q.getReward().getAmount() == 0 && q.getTargetCount() > 0) {
                q.getReward().setAmount(q.getTargetCount());
            }
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

    public void setLastDailyRefresh(LocalDate d) {
        this.lastDailyRefresh = d;
    }

    private void addInitialQuests() {
        for (Quest template : STORY_TEMPLATES) {
            if (activeQuests.stream().noneMatch(q -> q.getId().equals(template.getId()) && q.isClaimed())) {
                Quest q = copyQuest(template, template.getId());
                if ("max_plant_loss".equals(q.getConditionKey())) {
                    int n = new Random().nextInt(6);
                    q.getParameters().put("n", n);
                    q.getReward().setAmount(20 - n);
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
                    q.getReward().setAmount(n);
                }
                activeQuests.add(q);
            }
        }
        updateChapterQuests();
    }

    public void updateChapterQuests() {
        ChapterEnum[] chapters = ChapterEnum.values();
        boolean anyClaimed = activeQuests.stream()
            .anyMatch(q -> q.getId().startsWith("story_chapter_hunt_") && q.isClaimed());
        String firstId = "story_chapter_hunt_" + chapters[0].name();
        if (!anyClaimed && activeQuests.stream().noneMatch(q -> q.getId().equals(firstId))) {
            addChapterQuest(chapters[0]);
        }
        for (int i = 1; i < chapters.length; i++) {
            String prevId = "story_chapter_hunt_" + chapters[i - 1].name();
            String currId = "story_chapter_hunt_" + chapters[i].name();
            boolean prevClaimed = activeQuests.stream()
                .anyMatch(q -> q.getId().equals(prevId) && q.isClaimed());
            boolean currExists = activeQuests.stream().anyMatch(q -> q.getId().equals(currId));
            if (prevClaimed && !currExists) addChapterQuest(chapters[i]);
        }
    }

    private void addChapterQuest(ChapterEnum chapter) {
        activeQuests.add(new Quest(
            "story_chapter_hunt_" + chapter.name(),
            "Chapter Hunter", "Defeat 50 zombies from {chapter}",
            QuestType.STORY, QuestPriority.HIGH, "chapter_zombie_kill", 50,
            new Reward(Reward.RewardType.SEED_PACKETS, 10, null),
            java.util.Map.of("chapter", chapter)
        ));
    }

    private Quest copyQuest(Quest t, String id) {
        Reward r = t.getReward();
        return new Quest(id, t.getTitle(), t.getDescriptionTemplate(),
            t.getType(), t.getPriority(), t.getConditionKey(),
            t.getTargetCount(), r == null ? null :
            new Reward(r.getType(), r.getAmount(), r.getTargetPlant()),
            t.getParameters());
    }

    private PlantFamily randomMintFamily() {
        PlantFamily[] usableMints = {
            PlantFamily.ENLIGHTEN_MINT,
            PlantFamily.APPEASE_MINT,
            PlantFamily.ARMA_MINT,
            PlantFamily.BOMBARD_MINT,
            PlantFamily.ENFORCE_MINT,
            PlantFamily.REINFORCE_MINT,
            PlantFamily.ENCHANT_MINT,
            PlantFamily.PIERCE_MINT,
            PlantFamily.CAT_TAIL_MINT
        };
        return usableMints[new Random().nextInt(usableMints.length)];
    }

    private PlantType randomAttackingPlant() {
        List<PlantType> list = new ArrayList<>();
        for (PlantType p : PlantType.values()) {
            if (p == PlantType.CACTUS) continue;
            PlantFamily fam = PlantFamilyMapper.getFamily(p);
            if (fam == PlantFamily.SHOOTER || fam == PlantFamily.MELEE ||
                fam == PlantFamily.LOBBER || fam == PlantFamily.EXPLOSIVE) {
                list.add(p);
            }
        }
        return list.isEmpty() ? PlantType.PEASHOOTER : list.get(new Random().nextInt(list.size()));
    }

    private <T extends Enum<T>> T getParamAsEnum(java.util.Map<String, Object> params,
                                                 String key, Class<T> c, T fallback) {
        Object o = params.get(key);
        if (o == null) return fallback;
        if (c.isInstance(o)) return (T) o;
        if (o instanceof String) {
            try {
                return Enum.valueOf(c, (String) o);
            } catch (IllegalArgumentException e) {
                return fallback;
            }
        }
        return fallback;
    }

    private ChapterEnum getChapterParam(Quest q) {
        return getParamAsEnum(q.getParameters(), "chapter", ChapterEnum.class, ChapterEnum.ANCIENT_EGYPT);
    }

    private PlantType getPlantParam(Quest q) {
        return getParamAsEnum(q.getParameters(), "plant", PlantType.class, PlantType.PEASHOOTER);
    }

    private PlantFamily getFamilyParam(Quest q) {
        return getParamAsEnum(q.getParameters(), "family", PlantFamily.class, PlantFamily.GENERAL);
    }

    private void evaluateQuestCondition(Quest q, LevelResult res) {
        switch (q.getConditionKey()) {
            case "symmetry" -> {
                if (res.getFinalMap() != null && checkSymmetry(res.getFinalMap())) q.setCompleted(true);
            }
            case "no_symmetry" -> {
                if (res.getFinalMap() != null && checkNoSymmetry(res.getFinalMap())) q.setCompleted(true);
            }
            case "column_empty" -> {
                if (res.getFinalMap() != null) {
                    int col = (int) q.getParameters().get("col");
                    if (isColumnEmpty(res.getFinalMap(), col)) q.setCompleted(true);
                }
            }
            case "row_empty" -> {
                if (res.getFinalMap() != null) {
                    int row = (int) q.getParameters().get("row");
                    if (isRowEmpty(res.getFinalMap(), row)) q.setCompleted(true);
                }
            }
            case "cross_empty" -> {
                if (res.getFinalMap() != null) {
                    int c = (int) q.getParameters().get("col");
                    int r = (int) q.getParameters().get("row");
                    if (isColumnEmpty(res.getFinalMap(), c) && isRowEmpty(res.getFinalMap(), r)) q.setCompleted(true);
                }
            }
            case "zero_sun_end" -> {
                if (res.getFinalSunCount() == 0) q.setCompleted(true);
            }
            case "max_plant_loss" -> {
                int max = (int) q.getParameters().get("n");
                if (res.getPlantsLost() <= max) q.setCompleted(true);
            }
            case "day_with_mushrooms" -> {
                if (isDayLevel && res.getPlantTypesUsed().stream().allMatch(pt ->
                    PlantFamilyMapper.getFamily(pt) == PlantFamily.MUSHROOM))
                    q.setCompleted(true);
            }
            case "no_family_used" -> {
                if (!res.getPlantFamiliesUsed().contains(getFamilyParam(q))) q.setCompleted(true);
            }
            case "lawnmower_kill" -> {
                int k = res.getZombiesKilledByLawnmower();
                if (k > 0) q.incrementProgress(k);
            }
            case "lawnless_col1_kill" -> {
                int k = res.getLawnlessCol1Kills();
                if (k > 0) q.incrementProgress(k);
            }
            case "streak" -> {
                if (consecutiveMaxDifficultyWins >= q.getTargetCount()) {
                    q.setCurrentCount(q.getTargetCount());
                    q.setCompleted(true);
                }
            }
            case "family_kill_only" -> {
                Boolean violated = (Boolean) q.getRuntimeState().get("familyViolated");
                Boolean anyKill = (Boolean) q.getRuntimeState().get("anyKill");
                if (Boolean.TRUE.equals(anyKill) && (violated == null || !violated)) {
                    q.setCompleted(true);
                }
            }
            case "max_sun_producers" -> {
                if (res.getFinalMap() != null && countSunProducers(res.getFinalMap()) <= q.getTargetCount())
                    q.setCompleted(true);
            }
            case "speed_kill", "use_explosive" -> {
                if (q.getCurrentCount() >= q.getTargetCount()) q.setCompleted(true);
                else q.resetProgress();
            }
        }
    }

    private boolean checkSymmetry(Map map) {
        int cols = map.getCols(), rows = map.getRows();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols / 2; c++) {
                Plant l = map.getPlantAt(r, c), r1 = map.getPlantAt(r, cols - 1 - c);
                if ((l == null) != (r1 == null)) return false;
                if (l != null && !l.getType().equals(r1.getType())) return false;
            }
        return true;
    }

    private boolean checkNoSymmetry(Map map) {
        int cols = map.getCols(), rows = map.getRows(), mid = rows / 2;
        for (int r = 0; r < rows; r++) {
            if (r == mid) continue;
            for (int c = 0; c < cols / 2; c++) {
                Plant l = map.getPlantAt(r, c), r1 = map.getPlantAt(r, cols - 1 - c);
                if ((l == null) != (r1 == null)) continue;
                if (l != null && l.getType().equals(r1.getType())) return false;
            }
        }
        return true;
    }

    private boolean isColumnEmpty(Map map, int colIdx) {
        int col = colIdx - 1;
        for (int r = 0; r < map.getRows(); r++) if (map.getPlantAt(r, col) != null) return false;
        return true;
    }

    private boolean isRowEmpty(Map map, int rowIdx) {
        int row = rowIdx - 1;
        for (int c = 0; c < map.getCols(); c++) if (map.getPlantAt(row, c) != null) return false;
        return true;
    }

    private int countSunProducers(Map map) {
        int cnt = 0;
        for (int r = 0; r < map.getRows(); r++)
            for (int c = 0; c < map.getCols(); c++) {
                Plant p = map.getPlantAt(r, c);
                if (p != null && PlantFamilyMapper.getFamily(p.getType()) == PlantFamily.SUN_PRODUCER) cnt++;
            }
        return cnt;
    }
}
