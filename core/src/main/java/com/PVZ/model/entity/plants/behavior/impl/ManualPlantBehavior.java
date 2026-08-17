package com.PVZ.model.entity.plants.behavior.impl;

import com.PVZ.model.entity.plants.AbilitySpec;
import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class ManualPlantBehavior implements PlantBehavior {
    private final PlantDefinition definition;
    private final AbilitySpec abilitySpec;

    public ManualPlantBehavior(PlantDefinition definition, AbilitySpec abilitySpec) {
        this.definition = definition;
        this.abilitySpec = abilitySpec;
    }

    @Override
    public void onUpdate(PlantInstance plant, BehaviorContext context, double deltaTime) {
        if (plant == null || context == null) {
            return;
        }

        String plantKey = definition == null ? "" : normalize(definition.getPlantKey());
        if ("imitater".equals(plantKey) && Boolean.TRUE.equals(plant.getRuntimeState().get("copyDone"))) {
            Object copiedKeyValue = plant.getRuntimeState().get("copiedPlantKey");
            if (copiedKeyValue != null) {
                PlantDefinition copiedDef = com.PVZ.model.entity.plants.PlantLibrary.all().stream()
                    .filter(d -> d.getPlantKey() != null && d.getPlantKey().equalsIgnoreCase(String.valueOf(copiedKeyValue)))
                    .findFirst().orElse(null);
                if (copiedDef != null && !"imitater".equalsIgnoreCase(copiedDef.getPlantKey())) {
                    com.PVZ.model.entity.plants.behavior.PlantBehavior copiedBehavior =
                        com.PVZ.model.entity.plants.behavior.BehaviorFactory.createMainBehavior(copiedDef);
                    copiedBehavior.onUpdate(plant, context, deltaTime);
                    return;
                }
            }
        }
        int row = asInt(plant.getRuntimeState().getOrDefault("row", 0), 0);
        int col = asInt(plant.getRuntimeState().getOrDefault("col", 0), 0);
        int lane = asInt(plant.getRuntimeState().getOrDefault("lane", row), row);

        if (handleSpecificPlantKey(plantKey, plant, context, lane, deltaTime, row, col)) {
            return;
        }

        handleDefaultBehavior(plant, context, lane, deltaTime, row, col);
    }

    private boolean handleSpecificPlantKey(String plantKey, PlantInstance plant, BehaviorContext context,
                                           int lane, double deltaTime, int row, int col) {
        switch (plantKey) {
            case "gold_bloom":
                handleGoldBloom(plant, context, row, col, deltaTime);
                return true;
            case "rotobaga":
                handleDiagonalShot(plant, context, lane, deltaTime);
                return true;
            case "threepeater":
                handleTriLaneShot(plant, context, lane, deltaTime);
                return true;
            case "split_pea":
                handleFrontBackShot(plant, context, lane, deltaTime);
                return true;
            case "starfruit":
                handleStarShot(plant, context, lane, deltaTime);
                return true;
            case "cat_tail":
            case "cattail":
                handleHomingNearest(plant, context, lane, deltaTime);
                return true;
            case "bowling_bulb":
                handleBounceMultiLane(plant, context, lane, deltaTime);
                return true;
            case "pea_pod":
                handleStackShot(plant, context, lane, deltaTime);
                return true;
            default:
                return false;
        }
    }


    private void handleDefaultBehavior(PlantInstance plant, BehaviorContext context, int lane,
                                       double deltaTime, int row, int col) {
        String behaviorId = normalize(resolveBehaviorId());

        switch (behaviorId) {
            case "instant_sun":
                handleInstantSun(plant, context, row, col);
                break;
            case "move_zombies":
                handleMoveZombies(plant, context, lane, deltaTime);
                break;
            case "magnet_disarm":
            case "magnet_pulse":
                handleMagnet(plant, context, lane, deltaTime);
                break;
            case "hypnotize":
                handleHypnotize(plant, context, lane, deltaTime);
                break;
            case "copy_plant":
                handleCopyPlant(plant);
                break;
            case "water_support":
                handleWaterSupport(plant);
                break;
            case "melee_eat":
                handleMelee(plant, context, lane, row, deltaTime);
                break;
            case "sun_production":
                handleSunBeanLike(plant, context, row, col, lane, deltaTime);
                break;
            default:
                if (definition != null && definition.getCategoryEnum().name().equals("MELEE")) {
                    handleMelee(plant, context, lane, row, deltaTime);
                }
                break;
        }
    }

    private void handleGoldBloom(PlantInstance plant, BehaviorContext context, int row, int col, double deltaTime) {
        String stage = String.valueOf(plant.getRuntimeState().getOrDefault("goldBloomStage", "WAIT"));
        double timer = asDouble(plant.getRuntimeState().getOrDefault("goldBloomTimer", 0.0), 0.0);
        timer += deltaTime;

        if ("WAIT".equals(stage)) {
            // Let Gold Bloom visibly exist on the board before doing anything.
            if (timer < 2.0) {
                plant.putRuntimeState("goldBloomTimer", timer);
                return;
            }

            int amount = plant.getStats().getSunAmount();
            if (amount <= 0 && abilitySpec != null) {
                amount = abilitySpec.getIntParam("sunAmount", 375);
            }
            if (amount <= 0) amount = 375;

            com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 2.4);
            context.spawnSunAt(row, col, amount);
            plant.putRuntimeState("goldBloomStage", "BURST");
            plant.putRuntimeState("goldBloomTimer", 0.0);
            return;
        }

        // Keep the attack frame visible for a short moment, then remove the plant.
        if (timer >= 0.35) {
            context.removePlant(row, col);
            return;
        }
        plant.putRuntimeState("goldBloomTimer", timer);
    }

    private void handleInstantSun(PlantInstance plant, BehaviorContext context, int row, int col) {
        boolean triggered = asBoolean(plant.getRuntimeState().getOrDefault("instantSunTriggered", Boolean.FALSE),
            false);
        if (triggered) {
            return;
        }

        int amount = plant.getStats().getSunAmount();
        if (amount <= 0 && abilitySpec != null) {
            amount = abilitySpec.getIntParam("sunAmount", 375);
        }
        if (amount <= 0) {
            amount = 375;
        }
        System.out.println("plant " + plant.getDefinition().getName() + " produced a sun at (" + row + ", " + col +
            ")");
        context.spawnSunAt(row, col, amount);
        plant.putRuntimeState("instantSunTriggered", Boolean.TRUE);
        context.removePlant(row, col);
    }

    private void handleMoveZombies(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("moveTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = Math.max(1.0, plant.getStats().getActionIntervalSeconds());
        if (timer < cooldown) {
            plant.putRuntimeState("moveTimer", timer);
            return;
        }
        timer = 0.0;
        int targetLane = lane > 0 ? lane - 1 : lane + 1;
        context.moveZombiesFromLane(lane, targetLane);
        plant.putRuntimeState("moveTimer", timer);
    }

    private void handleMagnet(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("magnetTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = Math.max(1.0, plant.getStats().getActionIntervalSeconds());
        if (timer < cooldown) {
            plant.putRuntimeState("magnetTimer", timer);
            return;
        }
        timer = 0.0;
        List<Zombie> candidates = new ArrayList<>(context.getZombiesInLane(lane));
        candidates.removeIf(z -> z == null || z.isDead() || z.getArmor() == null);
        candidates.sort(java.util.Comparator.comparingDouble(Zombie::getX));
        if (!candidates.isEmpty()) {
            candidates.get(0).setArmor(null);
        }
        plant.putRuntimeState("magnetVisual", Boolean.TRUE);
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "special", 0.6667);
        plant.putRuntimeState("magnetTimer", timer);
    }

    private void handleHypnotize(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("hypnoTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = Math.max(1.0, plant.getStats().getActionIntervalSeconds());
        if (timer < cooldown) {
            plant.putRuntimeState("hypnoTimer", timer);
            return;
        }

        String plantKey = definition == null || definition.getPlantKey() == null
            ? ""
            : normalize(definition.getPlantKey());

        timer = 0.0;
        if ("caulipower".equals(plantKey)) {
            List<Zombie> zombies = new ArrayList<>(context.getAllZombies());
            Collections.shuffle(zombies);
            if (!zombies.isEmpty()) {
                Zombie target = zombies.get(0);
                if (target != null && !target.isDead()) {
                    Projectile magic = ProjectileFactory.createProjectile(plant, 0);
                    magic.putExtra("visualKey", "CAULIPOWER");
                    magic.initFreePosition((float) target.getX(), (float) target.getY(), 0f, 0f);
                    magic.setFuse(0.65);
                    context.spawnProjectile(magic);
                    target.hypnotize(5.0f);
                }
            }
        } else {
            context.hypnotizeZombiesInLane(lane, 3.0);
        }
        plant.putRuntimeState("hypnoTimer", timer);
    }

    private void handleCopyPlant(PlantInstance plant) {
        if (Boolean.TRUE.equals(plant.getRuntimeState().getOrDefault("copyDone", Boolean.FALSE))) {
            return;
        }
        Object copied = plant.getStats().getExtra("copiedPlantType");
        if (copied == null) {
            copied = plant.getRuntimeState().get("copiedPlantType");
        }
        if (copied == null) {
            return;
        }
        String copiedKey = String.valueOf(copied).trim().toLowerCase();
        if (copiedKey.isEmpty()) {
            return;
        }
        String originalCopiedKey = copiedKey;
        com.PVZ.model.entity.plants.PlantDefinition def =
            com.PVZ.model.entity.plants.PlantLibrary.findByName(copiedKey)
                .orElse(null);
        if (def == null) {
            for (com.PVZ.model.entity.plants.PlantDefinition d
                : com.PVZ.model.entity.plants.PlantLibrary.all()) {
                if (d.getPlantKey() != null
                    && d.getPlantKey().equalsIgnoreCase(copiedKey)) {
                    def = d;
                    break;
                }
            }
        }
        if (def != null) {
            plant.putRuntimeState("copiedPlantKey", def.getPlantKey());
            plant.putRuntimeState("copyDone", Boolean.TRUE);
            plant.putRuntimeState("imitaterDisplayState", "copied");
            plant.putRuntimeState("copiedPlantType", def.getPlantKey());
            com.PVZ.model.entity.PlantAnimation.trigger(plant, "idle", 0.5);
        }
    }

    private void handleWaterSupport(PlantInstance plant) {
        plant.putRuntimeState("waterSupport", Boolean.TRUE);
    }

    private void handleMelee(PlantInstance plant, BehaviorContext context, int lane, int row, double deltaTime) {
        Double timer = asDouble(plant.getRuntimeState().getOrDefault("meleeTimer", 0.0), 0.0);
        timer += deltaTime;
        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }
        if (timer < cooldown) {
            plant.putRuntimeState("meleeTimer", timer);
            return;
        }
        timer = 0.0;
        int damage = Math.max(plant.getStats().getDamage(), plant.getStats().getAoeDamage());
        if (damage <= 0) {
            damage = 30;
        }
        context.damageArea(lane, row, damage);
        if (plant.getStats().getEatTimeSeconds() > 0) {
            plant.putRuntimeState("digestTimer", plant.getStats().getEatTimeSeconds());
        }
        plant.putRuntimeState("meleeTimer", timer);
    }

    private void handleSunBeanLike(PlantInstance plant, BehaviorContext context, int row, int col, int lane,
                                   double deltaTime) {
        List<Zombie> zombies = context.getZombiesInLane(lane);
        if (zombies.isEmpty()) {
            return;
        }

        Double timer = asDouble(plant.getRuntimeState().getOrDefault("sunBeanTimer", 0.0), 0.0);
        timer += deltaTime;
        if (timer >= 1.0) {
            int amount = plant.getStats().getSunDropAmount();
            if (amount <= 0) {
                amount = 5;
            }
            plant.putRuntimeState("sunDropScale", 0.70);
            System.out.println("plant " + plant.getDefinition().getName() + " produced a sun at (" + row + ", " + col +
                ")");
            context.spawnSunAt(row, col, amount);
            timer = 0.0;
        }
        plant.putRuntimeState("sunBeanTimer", timer);
    }

    private int computeDamage(PlantInstance plant) {
        int damage = Math.max(0, plant.getStats().getDamage());
        double multiplier = plant.getStats().getDoubleExtra("damageMultiplier", 1.0);
        if (plant.isPlantFoodActive()) {
            multiplier = Math.max(multiplier, plant.getStats().getDoubleExtra("plantFoodDamageMultiplier", 2.0));
        }
        return (int) Math.round(damage * multiplier);
    }

    private void fireInto(PlantInstance plant, BehaviorContext context, int targetLane, boolean backward) {
        if (targetLane < 0) {
            return;
        }
        int damage = computeDamage(plant);
        Projectile projectile = ProjectileFactory.createProjectile(plant, damage);
        projectile.setLane(targetLane);
        projectile.putExtra("targetLane", targetLane);
        if (backward) {
            projectile.setSpeed(-Math.abs(projectile.getSpeed()));
        }
        context.spawnProjectile(projectile);
    }

    private Projectile fireParallel(PlantInstance plant, BehaviorContext context, int targetLane, boolean backward) {
        return fireParallel(plant, context, targetLane, backward, 0.0);
    }

    private Projectile fireParallel(PlantInstance plant, BehaviorContext context, int targetLane,
                                    boolean backward, double spawnXOffset) {
        if (targetLane < 0) {
            return null;
        }
        int damage = computeDamage(plant);
        Projectile projectile = ProjectileFactory.createProjectile(plant, damage);
        projectile.setRow(targetLane);
        projectile.setLane(targetLane);
        if (backward) {
            projectile.setSpeed(-Math.abs(projectile.getSpeed()));
        }
        if (Math.abs(spawnXOffset) > 0.001) {
            projectile.putExtra("spawnXOffset", spawnXOffset);
        }
        context.spawnProjectile(projectile);
        return projectile;
    }

    private void fireHoming(PlantInstance plant, BehaviorContext context, Zombie target) {
        int damage = computeDamage(plant);
        Projectile projectile = ProjectileFactory.createProjectile(plant, damage);
        double px = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
        double py = asDouble(plant.getRuntimeState().getOrDefault("worldY", 0.0), 0.0);
        double tw = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0), 177.0);
        double th = asDouble(plant.getRuntimeState().getOrDefault("tileHeight", 234.0), 234.0);
        float startX = (float) (px + tw * 0.5);
        float startY = (float) (py + th * 0.5);
        double dx = target.getX() - startX;
        double dy = target.getY() - startY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        double speed = 320.0;
        float vx = (float) (dist < 1e-6 ? speed : dx / dist * speed);
        float vy = (float) (dist < 1e-6 ? 0.0 : dy / dist * speed);
        projectile.initFreePosition(startX, startY, vx, vy);
        projectile.setHoming(true);
        if (plant.getDefinition() != null && "cat_tail".equals(plant.getDefinition().getPlantKey())) {
            projectile.putExtra("visualKey", "CATTAIL_HOMING");
        }
        context.spawnProjectile(projectile);
    }

    private void handleDiagonalShot(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        if (!tickCooldown(plant, "diagonalTimer", deltaTime)) {
            return;
        }
        int volleys = plant.isPlantFoodActive() ? 2 : 1;
        for (int v = 0; v < volleys; v++) {
            fireInto(plant, context, lane - 1, false);
            fireInto(plant, context, lane + 1, false);
            fireInto(plant, context, lane - 1, true);
            fireInto(plant, context, lane + 1, true);
        }
    }

    private void handleTriLaneShot(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        if (!tickCooldown(plant, "triLaneTimer", deltaTime)) {
            return;
        }
        int top = Math.max(0, lane - 1);
        int bottom = Math.min(4, lane + 1);
        boolean anyTarget = !context.getZombiesInLane(top).isEmpty()
            || !context.getZombiesInLane(lane).isEmpty()
            || !context.getZombiesInLane(bottom).isEmpty();
        if (!anyTarget) {
            return;
        }

        int volleys = plant.isPlantFoodActive() ? 2 : 1;
        for (int v = 0; v < volleys; v++) {
            fireParallel(plant, context, top, false, 0.0);
            if (lane != top && lane != bottom) {
                fireParallel(plant, context, lane, false, 0.0);
            }
            fireParallel(plant, context, bottom, false, 0.0);
        }
    }

    private void handleFrontBackShot(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        if (!tickCooldown(plant, "frontBackTimer", deltaTime)) {
            return;
        }
        if (context.getZombiesInLane(lane).isEmpty()) {
            return;
        }
        int volleys = plant.isPlantFoodActive() ? 2 : 1;
        for (int v = 0; v < volleys; v++) {
            fireInto(plant, context, lane, false);
            fireInto(plant, context, lane, true);
        }
    }

    private void handleStarShot(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        if (!tickCooldown(plant, "starTimer", deltaTime)) {
            return;
        }
        int volleys = plant.isPlantFoodActive() ? 2 : 1;
        for (int v = 0; v < volleys; v++) {
            // Five fixed star directions: right, up-right, down-right, up-left, down-left.
            fireStarVector(plant, context, 1.0, 0.0);
            fireStarVector(plant, context, 0.82, 0.82);
            fireStarVector(plant, context, 0.82, -0.82);
            fireStarVector(plant, context, -0.82, 0.82);
            fireStarVector(plant, context, -0.82, -0.82);
        }
        com.PVZ.model.entity.PlantAnimation.trigger(plant, "attack", 1.0);
    }

    private void fireStarVector(PlantInstance plant, BehaviorContext context, double dx, double dy) {
        int damage = computeDamage(plant);
        Projectile projectile = ProjectileFactory.createProjectile(plant, damage);
        double px = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
        double py = asDouble(plant.getRuntimeState().getOrDefault("worldY", 0.0), 0.0);
        double tw = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0), 177.0);
        double th = asDouble(plant.getRuntimeState().getOrDefault("tileHeight", 234.0), 234.0);
        double startX = px + tw * 0.63;
        double startY = py + th * 0.54;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-6) return;
        double speed = tw * 1.7;
        projectile.initFreePosition((float) startX, (float) startY,
            (float) (dx / len * speed), (float) (dy / len * speed));
        projectile.putExtra("starDirection", new double[]{dx, dy});
        context.spawnProjectile(projectile);
    }

    private void handleHomingNearest(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        if (!tickCooldown(plant, "homingTimer", deltaTime)) {
            return;
        }
        List<Zombie> all = context.getAllZombies();
        if (all.isEmpty()) {
            return;
        }
        double px = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
        double py = asDouble(plant.getRuntimeState().getOrDefault("worldY", 0.0), 0.0);
        Zombie nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (Zombie z : all) {
            if (z == null || z.isDead()) {
                continue;
            }
            double dx = z.getX() - px;
            double dy = z.getY() - py;
            double distance = dx * dx + dy * dy;
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = z;
            }
        }
        if (nearest == null) {
            return;
        }
        fireHoming(plant, context, nearest);
    }

    private void handleBounceMultiLane(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        java.util.List<Integer> tiers = plant.getDefinition() != null && plant.getDefinition().getDamageSpec() != null
            ? plant.getDefinition().getDamageSpec().getTiers()
            : java.util.List.of();
        int cyanDamage = tiers.size() > 0 ? tiers.get(0) : computeDamage(plant);
        int blueDamage = tiers.size() > 1 ? tiers.get(1) : computeDamage(plant);
        int orangeDamage = tiers.size() > 2 ? tiers.get(2) : computeDamage(plant);

        launchBulbOnCycle(plant, context, lane, deltaTime, 2.0, "cyanTimer", cyanDamage, "BOWLING_BULB_1", 0.65);
        launchBulbOnCycle(plant, context, lane, deltaTime, 5.0, "blueTimer", blueDamage, "BOWLING_BULB_2", -0.72);
        launchBulbOnCycle(plant, context, lane, deltaTime, 10.0, "orangeTimer", orangeDamage, "BOWLING_BULB_3", 0.82);
    }

    private void launchBulbOnCycle(PlantInstance plant, BehaviorContext context, int lane, double deltaTime,
                                   double periodSeconds, String timerKey, int damage, String visualKey, double verticalSign) {
        double timer = asDouble(plant.getRuntimeState().getOrDefault(timerKey, 0.0), 0.0);
        timer += deltaTime;
        if (timer < periodSeconds) {
            plant.putRuntimeState(timerKey, timer);
            return;
        }
        plant.putRuntimeState(timerKey, timer - periodSeconds);

        double px = asDouble(plant.getRuntimeState().getOrDefault("worldX", 0.0), 0.0);
        double py = asDouble(plant.getRuntimeState().getOrDefault("worldY", 0.0), 0.0);
        double tw = asDouble(plant.getRuntimeState().getOrDefault("tileWidth", 177.0), 177.0);
        double th = asDouble(plant.getRuntimeState().getOrDefault("tileHeight", 234.0), 234.0);
        double minX = px;
        double maxX = px + tw * 6.0;
        double minY = py - (4 * th) + th * 0.5;
        double maxY = py + th * 0.5;
        Projectile projectile = ProjectileFactory.createProjectile(plant, damage);
        projectile.setType(ProjectileType.LOB);
        projectile.setPierce(999);
        projectile.putExtra("visualKey", visualKey);
        projectile.putExtra("bowlingBulb", Boolean.TRUE);
        projectile.initFreePosition((float) (px + tw * 0.63), (float) (py + th * 0.42),
            (float) (tw * 0.9), (float) (verticalSign * th * 0.45));
        projectile.setBouncing(true);
        projectile.setBounds(minX, maxX, minY, maxY);
        projectile.setFuse(14.0);
        context.spawnProjectile(projectile);
    }

    private void handleStackShot(PlantInstance plant, BehaviorContext context, int lane, double deltaTime) {
        if (!tickCooldown(plant, "stackTimer", deltaTime)) {
            return;
        }
        if (context.getZombiesInLane(lane).isEmpty()) {
            return;
        }

        int heads = asInt(plant.getRuntimeState().getOrDefault("peaPodHeads", 1), 1);
        heads = Math.max(1, Math.min(5, heads));
        int volleys = plant.isPlantFoodActive() ? 2 : 1;
        String attackState = heads == 1 ? "attack" : "attack " + heads;
        com.PVZ.model.entity.PlantAnimation.trigger(plant, attackState, 1.0);
        for (int v = 0; v < volleys; v++) {
            for (int i = 0; i < heads; i++) {
                double offset = (i - (heads - 1) / 2.0) * 18.0;
                fireParallel(plant, context, lane, false, offset);
            }
        }
    }

    private boolean tickCooldown(PlantInstance plant, String key, double deltaTime) {
        double timer = asDouble(plant.getRuntimeState().getOrDefault(key, 0.0), 0.0);
        timer += deltaTime;
        double cooldown = plant.getStats().getActionIntervalSeconds();
        if (cooldown <= 0) {
            cooldown = 1.5;
        }
        if (timer < cooldown) {
            plant.putRuntimeState(key, timer);
            return false;
        }
        plant.putRuntimeState(key, 0.0);
        return true;
    }

    private String resolveBehaviorId() {
        if (abilitySpec != null) {
            if (abilitySpec.getBehaviorId() != null && !abilitySpec.getBehaviorId().isBlank()) {
                return abilitySpec.getBehaviorId();
            }
            if (abilitySpec.getId() != null && !abilitySpec.getId().isBlank()) {
                return abilitySpec.getId();
            }
        }
        if (definition != null && definition.getBaseAbility() != null) {
            return definition.getBaseAbility().getResolvedBehaviorId();
        }
        return "";
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }

    private static boolean asBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value == null ? defaultValue : Boolean.parseBoolean(String.valueOf(value));
    }

    private static int asInt(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return value == null ? defaultValue : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public static double asDouble(Object value, double defaultValue) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return value == null ? defaultValue : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
