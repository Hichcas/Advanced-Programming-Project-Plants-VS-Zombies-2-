package com.PVZ.model.game.chapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Human-readable rule explanations for every stage type, shown in the
 * level-start overlay and the pause menu. Texts follow the project spec:
 * CONVEYOR_BELT, LOCKED_PLANTS, NIGHT_OPS, LOVE_YOUR_PLANTS, DEAD_LINE.
 */
public final class StageRules {

    private StageRules() {
    }

    public static List<String> describe(StageConfig config) {
        List<String> out = new ArrayList<>();
        if (config == null) {
            out.add("Don't let the zombies reach your house!");
            return out;
        }

        String type = config.getType() == null ? "" : config.getType().trim().toUpperCase();
        String special = config.getSpecialLevel() == null ? "" : config.getSpecialLevel().trim().toUpperCase();
        boolean deadLine = type.contains("DEADLINE") || special.contains("DEAD_LINE");

        if (deadLine) {
            addDeadlineDescription(out);
        } else if (type.equals("CONVEYOR_BELT")) {
            addConveyorBeltDescription(out, config);
        } else if (type.equals("LOCKED_PLANTS")) {
            addLockedPlantsDescription(out, config);
        } else if (type.equals("NIGHT_OPS") || special.equals("NIGHT_OPS")) {
            addNightOpsDescription(out);
        } else if (type.equals("TIMED_WAR") || special.contains("TIMED_WAR")) {
            addTimedWarDescription(out, config);
        } else if (type.equals("LOVE_YOUR_PLANTS") || special.equals("LOVE_YOUR_PLANTS")) {
            addLoveYourPlantsDescription(out, config);
        } else if (type.equals("SURVIVAL_SCORE")) {
            addSurvivalScoreDescription(out);
        } else if (type.equals("PLANT_WHAT_YOU_GET")) {
            addPlantWhatYouGetDescription(out, config);
        }

        if (out.isEmpty()) {
            out.add("Pick your seeds, collect sun, and stop the zombie waves before they reach your house!");
        } else if (!deadLine) {
            out.add("Clear all waves — don't let the zombies reach your house!");
        }

        if (config.isDisableFallingSun() && !type.equals("NIGHT_OPS") && !special.equals("NIGHT_OPS")
            && !type.equals("PLANT_WHAT_YOU_GET")) {
            out.add("No sun falls from the sky this level!");
        }
        if (config.getPlantLimit() > 0) {
            out.add("You may pick up to " + config.getPlantLimit() + " plants for this level.");
        }
        return out;
    }

    private static void addDeadlineDescription(List<String> out) {
        out.add("DEAD LINE: Every lane has its own red line at a random position of the lawn.");
        out.add("The moment any zombie crosses its lane's red line, you lose the level!");
    }

    private static void addConveyorBeltDescription(List<String> out, StageConfig config) {
        out.add("CONVEYOR BELT: No seed selection — you enter the level directly.");
        double interval = config.getConveyorInterval() > 0 ? config.getConveyorInterval() : 12.0;
        String seconds = interval == Math.floor(interval)
            ? String.valueOf((long) interval) : String.valueOf(interval);
        out.add("The belt in the corner of the screen brings a random plant (from the plants you own) every "
            + seconds + " seconds — the first one arrives the moment you enter!");
    }

    private static void addLockedPlantsDescription(List<String> out, StageConfig config) {
        out.add("LOCKED PLANTS: Some seeds are unavailable in this level.");
        if (config.getLockedFamilies() != null && !config.getLockedFamilies().isEmpty()) {
            String families = config.getLockedFamilies().stream()
                .map(StageConfig.FamilyLockEntry::getFamily)
                .collect(Collectors.joining(", "));
            out.add("Only ONE plant of each locked family may be picked — the rest of the family is locked: "
                + families + ".");
        }
        if (config.getLockedPlants() != null && !config.getLockedPlants().isEmpty()) {
            out.add("These plants are completely locked: " + String.join(", ", config.getLockedPlants()) + ".");
        }
    }

    private static void addNightOpsDescription(List<String> out) {
        out.add("NIGHT OPS: Night has fallen — no sun drops from the sky.");
        out.add("Only sun produced by your plants (like Sunflower) keeps you alive!");
    }

    private static void addTimedWarDescription(List<String> out, StageConfig config) {
        double secs = config.getTimedWarSeconds() > 0 ? config.getTimedWarSeconds() : 120;
        String time = secs == Math.floor(secs)
            ? String.valueOf((long) secs) + " seconds" : String.valueOf(secs) + " seconds";
        out.add("TIMED WAR: A timer at the top of the screen is ticking!");
        boolean anyGoal = false;
        if (config.getTimedWarZombieKills() > 0) {
            out.add("Kill " + config.getTimedWarZombieKills() + " zombies before the "
                + time + " run out!");
            anyGoal = true;
        }
        if (config.getTimedWarSunTarget() > 0) {
            out.add("Produce (collect) " + config.getTimedWarSunTarget() + " sun before the "
                + time + " run out!");
            anyGoal = true;
        }
        if (!anyGoal) {
            out.add("Kill 12 zombies before the " + time + " run out!");
        }
    }

    private static void addLoveYourPlantsDescription(List<String> out, StageConfig config) {
        int q = config.getMaxPlantDeaths() > 0 ? config.getMaxPlantDeaths() : 5;
        out.add("LOVE YOUR PLANTS: If " + q + " of your plants are destroyed or eaten by zombies, you lose!");
    }

    private static void addSurvivalScoreDescription(List<String> out) {
        out.add("MYOPOINT: This is a score-attack level! Surviving isn't enough - how you fight matters.");
        out.add("Quick kills, multi-lane kills, kill combos,"+
            " and last-second saves near your house all earn bonus MyoPoints.");
        out.add("The zombie lineup is the same for everyone today, but changes again tomorrow.");
        out.add("Your best MyoPoint score is saved to your profile and shown on the leaderboard, win or lose!");
    }

    private static void addPlantWhatYouGetDescription(List<String> out, StageConfig config) {
        int startingSun = config.getInitialSun() > 0 ? config.getInitialSun() : 500;
        out.add("PLANT WHAT YOU GET: You start with " + startingSun
            + " sun and no more sun will ever fall from the sky.");
        out.add("No zombies enter the lawn until you're ready — plant freely and instantly, "
            + "with no recharge wait, for as long as you like.");
        out.add("Press the Start Wave button whenever you want to begin — you decide when each wave arrives!");
    }

    public static String summary(StageConfig config) {
        List<String> lines = describe(config);
        return lines.isEmpty() ? "" : lines.get(0);
    }
}
