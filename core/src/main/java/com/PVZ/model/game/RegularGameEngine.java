package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Sun;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.enums.PlantType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RegularGameEngine extends GameEngine implements ZombieEngine, BehaviorContext {
    private static final double TICK_SECONDS = 0.1;
    private static final int ROWS = 5;
    private static final int COLS = 9;

    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Zombie> zombies = new ArrayList<>();
    private final SunManager sunManager = new SunManager();
    private final PlantFoodManager plantFoodManager = new PlantFoodManager();
    private final Random random = new Random();
    private final Map<Zombie, Integer> freezeTimers = new IdentityHashMap<>();
    private final Map<Zombie, Integer> hypnotizeTimers = new IdentityHashMap<>();
    private float tickAccumulator = 0f;
    private boolean zombieWavesStarted = false;

    /** Currently selected seed packet (set by clicking the SeedPacketBar), or null if none. */
    private PlantType selectedPlantType;

    /** Per plant-type recharge cooldown remaining, in seconds. */
    private final Map<PlantType, Double> rechargeRemaining = new java.util.EnumMap<>(PlantType.class);

    private final SeedPacketBar seedPacketBar = new SeedPacketBar();

    public RegularGameEngine(GameStatus gameStatus) {
        super(gameStatus, new RegularInputProcessor());
        ((RegularInputProcessor) inputProcessor).setRegularGameEngine(this);
        if (gameStatus != null && gameStatus.getSunflower() <= 0) {
            gameStatus.setSunflower(0);
        }
    }

    @Override
    public void update(float delta) {
        tickAccumulator += delta;
        while (tickAccumulator >= TICK_SECONDS) {
            tickAccumulator -= TICK_SECONDS;
            advanceOneTick();
        }
    }

    public void advanceTicks(int ticks) {
        int safeTicks = Math.max(0, ticks);
        for (int i = 0; i < safeTicks; i++) {
            advanceOneTick();
        }
    }

    private void advanceOneTick() {
        updatePlants();
        updateProjectiles((float) TICK_SECONDS);
        updateSunManager((float) TICK_SECONDS);
        updateStatusEffects();
        rechargeRemaining.replaceAll((type, remaining) -> Math.max(0.0, remaining - TICK_SECONDS));

        if (gameStatus != null) {
            gameStatus.setRemainingZombieWaveInPercent(zombieWavesStarted ? Math.min(100, gameStatus.getRemainingZombieWaveInPercent() + 1) : 0);
        }
    }

    private void updatePlants() {
        if (map == null) {
            return;
        }

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Plant plant = map.getPlantAt(row, col);
                if (plant == null) {
                    continue;
                }

                plant.putRuntimeState("row", row);
                plant.putRuntimeState("col", col);
                plant.putRuntimeState("lane", row);
                plant.update(this, TICK_SECONDS);

                if (plant.isDead()) {
                    if (plant.getStats().getBooleanExtra("explodeOnDeath", false)) {
                        damageArea(row, row, Math.max(plant.getStats().getExplodeDamage(), plant.getStats().getDamage()));
                    }
                    map.removePlant(row, col);
                }
            }
        }
    }

    private void updateProjectiles(float delta) {
        if (projectiles.isEmpty()) {
            return;
        }

        for (Projectile projectile : projectiles) {
            projectile.update(delta);
        }

        projectiles.removeIf(Projectile::isDestroyed);
    }

    private void updateSunManager(float delta) {
        sunManager.update(delta);
    }

    private void updateStatusEffects() {
        decrementTimers(freezeTimers, true);
        decrementTimers(hypnotizeTimers, true);
    }

    private void decrementTimers(Map<Zombie, Integer> timers, boolean restoreMovement) {
        List<Zombie> toRestore = new ArrayList<>();
        for (Map.Entry<Zombie, Integer> entry : timers.entrySet()) {
            int remaining = Math.max(0, entry.getValue() - 1);
            entry.setValue(remaining);
            if (remaining == 0) {
                toRestore.add(entry.getKey());
            }
        }
        for (Zombie zombie : toRestore) {
            if (restoreMovement && zombie != null) {
                zombie.startMoving();
            }
            timers.remove(zombie);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
    }

    @Override
    public void dispose() {
        projectiles.clear();
        zombies.clear();
        sunManager.clear();
        plantFoodManager.reset();
        freezeTimers.clear();
        hypnotizeTimers.clear();
    }

    @Override
    public List<Zombie> getZombiesInLane(int lane) {
        if (lane < 0) {
            return List.of();
        }
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : zombies) {
            if (zombie != null && (int) zombie.getRow() == lane && !zombie.isDead()) {
                result.add(zombie);
            }
        }
        return result;
    }

    @Override
    public List<Zombie> getAllZombies() {
        return Collections.unmodifiableList(zombies);
    }

    @Override
    public void kill(Object entity) {

    }

    @Override
    public void takeDamage(Object entity, double amount) {

    }

    @Override
    public Plant getPlantAt(int row, int col) {
        if (map == null) {
            return null;
        }
        return map.getPlantAt(row, col);
    }

    @Override
    public void spawnProjectile(Object projectile) {
        if (projectile instanceof Projectile p) {
            projectiles.add(p);
        }
    }

    @Override
    public void spawnSun(int amount) {
        spawnSunAt(0, 0, amount);
    }

    @Override
    public void spawnSunAt(int row, int col, int amount) {
        if (amount <= 0) {
            return;
        }

        double x = 0;
        double y = 0;
        if (map != null) {
            Tile tile = map.getTile(row, col);
            if (tile != null) {
                x = tile.getX() + tile.getWidth() / 2.0;
                y = tile.getY() + tile.getHeight() / 2.0;
            }
        }
        sunManager.spawnFalling(x, y, amount, y - 120);
    }

    @Override
    public void damageArea(int lane, int row, int damage) {
        if (damage <= 0) {
            return;
        }
        for (Zombie zombie : getZombiesInLane(lane)) {
            if (zombie != null) {
                zombie.takeDamage(damage);
            }
        }
    }

    @Override
    public void freezeZombiesInLane(int lane, double seconds) {
        int ticks = Math.max(1, (int) Math.ceil(seconds / TICK_SECONDS));
        for (Zombie zombie : getZombiesInLane(lane)) {
            if (zombie != null) {
                zombie.stopMoving();
                freezeTimers.put(zombie, ticks);
            }
        }
    }

    @Override
    public void freezeAllZombies(double seconds) {
        for (Zombie zombie : zombies) {
            if (zombie != null && !zombie.isDead()) {
                zombie.stopMoving();
                freezeTimers.put(zombie, Math.max(1, (int) Math.ceil(seconds / TICK_SECONDS)));
            }
        }
    }

    @Override
    public void disarmZombiesInLane(int lane) {
        for (Zombie zombie : getZombiesInLane(lane)) {
            if (zombie != null) {
                zombie.setArmor(null);
            }
        }
    }

    @Override
    public void moveZombiesFromLane(int sourceLane, int targetLane) {
        int clampedTarget = Math.max(0, Math.min(ROWS - 1, targetLane));
        for (Zombie zombie : getZombiesInLane(sourceLane)) {
            if (zombie != null) {
                zombie.setRow(clampedTarget);
                zombie.setY(clampedTarget * 100.0);
            }
        }
    }

    @Override
    public void pullAdjacentZombiesToLane(int lane) {
        moveZombiesFromLane(lane - 1, lane);
        moveZombiesFromLane(lane + 1, lane);
    }

    @Override
    public void killRandomZombies(int count) {
        List<Zombie> alive = new ArrayList<>();
        for (Zombie zombie : zombies) {
            if (zombie != null && !zombie.isDead()) {
                alive.add(zombie);
            }
        }
        Collections.shuffle(alive, random);
        for (int i = 0; i < Math.min(count, alive.size()); i++) {
            alive.get(i).takeDamage(Double.MAX_VALUE);
        }
    }

    @Override
    public void killClosestZombieInLane(int lane) {
        List<Zombie> laneZombies = new ArrayList<>(getZombiesInLane(lane));
        if (laneZombies.isEmpty()) {
            return;
        }
        laneZombies.sort(Comparator.comparingDouble(Zombie::getX));
        laneZombies.get(0).takeDamage(Double.MAX_VALUE);
    }

    @Override
    public void hypnotizeZombiesInLane(int lane, double seconds) {
        int ticks = Math.max(1, (int) Math.ceil(seconds / TICK_SECONDS));
        for (Zombie zombie : getZombiesInLane(lane)) {
            if (zombie != null) {
                zombie.stopMoving();
                hypnotizeTimers.put(zombie, ticks);
            }
        }
    }

    @Override
    public void healPlantAt(int row, int col, int amount) {
        Plant plant = getPlantAt(row, col);
        if (plant != null) {
            plant.heal(Math.max(0, amount));
        }
    }

    @Override
    public void fortifyPlantAt(int row, int col, int amount) {
        healPlantAt(row, col, amount);
    }

    @Override
    public void consumePlantFood(PlantInstance plant) {
        plantFoodManager.consumePlantFood();
    }

    @Override
    public int getSunCount() {
        return gameStatus == null ? 0 : gameStatus.getSunflower();
    }

    @Override
    public void addSun(int amount) {
        if (gameStatus == null || amount == 0) {
            return;
        }
        int next = gameStatus.getSunflower() + amount;
        gameStatus.setSunflower(Math.max(0, next));
    }

    @Override
    public void spawnProjectile(Projectile p) {

    }

    @Override
    public void spawnZombie(String alias, int row, double x) {
        // zombie side intentionally kept minimal in this patch
    }

    @Override
    public void removePlant(int row, int col) {
        if (map == null) {
            return;
        }
        map.removePlant(row, col);
    }

    public String plantPlant(String plantType, int x, int y) {
        PlantType type;
        try {
            type = PlantType.fromName(plantType);
        } catch (Exception ex) {
            return "Unknown plant type: " + plantType;
        }
        return plantPlant(type, x, y);
    }

    /** Places {@code type} at grid location (x, y) per the design doc's 1-based "-l (x, y)" convention. */
    public String plantPlant(PlantType type, int x, int y) {
        if (map == null) {
            return "Map is not ready.";
        }
        if (type == null) {
            return "Unknown plant type.";
        }

        int row = normalizeIndex(y);
        int col = normalizeIndex(x);
        if (!map.isWithinBounds(row, col)) {
            return "Invalid tile.";
        }
        if (map.getPlantAt(row, col) != null) {
            return "Tile is occupied.";
        }
        if (isOnCooldown(type)) {
            return type.getDisplayName() + " is still recharging.";
        }

        // Only plants chosen in the pre-level Plant Selection Menu (AppStatus.selectedPlants)
        // can actually be planted during the level, per the design doc.
        if (!AppStatus.selectedPlants.isEmpty() && !AppStatus.selectedPlants.contains(type)) {
            return "Plant was not selected for this level: " + type.getDisplayName();
        }

        int userLevel = 1;
        if (AppStatus.currentUser != null && AppStatus.currentUser.collectionState != null) {
            var collection = AppStatus.currentUser.collectionState;
            if (!collection.isPlantUnlocked(type)) {
                return "Plant is locked: " + type.getDisplayName();
            }
            userLevel = collection.getPlantLevel(type) + 1;
        }

        Plant plant = PlantFactory.createPlant(type, userLevel);
        if (plant == null) {
            return "Cannot create plant.";
        }

        int cost = plant.getStats().getCost();
        if (getSunCount() < cost) {
            return "Not enough sun.";
        }

        addSun(-cost);
        plant.setPlanted(true);
        plant.putRuntimeState("row", row);
        plant.putRuntimeState("col", col);
        plant.putRuntimeState("lane", row);
        map.setPlant(row, col, plant);

        double recharge = plant.getStats().getRechargeSeconds();
        if (recharge > 0) {
            rechargeRemaining.put(type, recharge);
        }

        // A plant boosted (2 diamonds) in the selection menu has its plant-food effect
        // triggered immediately every time it's planted during this level.
        if (AppStatus.boostedPlants.contains(type)) {
            plant.applyPlantFood(this);
        }

        return "Planted " + type.getDisplayName() + " at (" + row + ", " + col + ").";
    }

    public boolean isOnCooldown(PlantType type) {
        return rechargeRemaining.getOrDefault(type, 0.0) > 0.0;
    }

    public void selectPlant(PlantType type) {
        this.selectedPlantType = (selectedPlantType == type) ? null : type;
    }

    public PlantType getSelectedPlantType() {
        return selectedPlantType;
    }

    public void clearSelection() {
        this.selectedPlantType = null;
    }

    public SeedPacketBar getSeedPacketBar() {
        return seedPacketBar;
    }

    /** Plants whatever seed is currently selected at (x, y) and clears the selection afterwards. */
    public String plantSelectedAt(int x, int y) {
        if (selectedPlantType == null) {
            return "No seed selected.";
        }
        String result = plantPlant(selectedPlantType, x, y);
        clearSelection();
        return result;
    }

    public String pluckPlant(int x, int y) {
        if (map == null) {
            return "Map is not ready.";
        }
        int row = normalizeIndex(y);
        int col = normalizeIndex(x);
        Plant plant = map.getPlantAt(row, col);
        if (plant == null) {
            return "No plant at selected tile.";
        }
        map.removePlant(row, col);
        return "Plant plucked from (" + row + ", " + col + ").";
    }

    public String feedPlant(int x, int y) {
        if (map == null) {
            return "Map is not ready.";
        }
        int row = normalizeIndex(y);
        int col = normalizeIndex(x);
        Plant plant = map.getPlantAt(row, col);
        if (plant == null) {
            return "No plant at selected tile.";
        }
        if (!plantFoodManager.consumePlantFood()) {
            return "No plant food available.";
        }
        plant.applyPlantFood(this);
        return "Plant fed at (" + row + ", " + col + ").";
    }

    public String collectSunAt(int x, int y) {
        int row = normalizeIndex(y);
        int col = normalizeIndex(x);
        double worldX = x * 100.0;
        double worldY = y * 100.0;
        if (map != null) {
            Tile tile = map.getTile(row, col);
            if (tile != null) {
                worldX = tile.getX() + tile.getWidth() / 2.0;
                worldY = tile.getY() + tile.getHeight() / 2.0;
            }
        }
        int collected = sunManager.collectAt(worldX, worldY);
        addSun(collected);
        return collected > 0 ? "Collected " + collected + " sun." : "No sun at selected location.";
    }

    public String showSunAmountText() {
        return "Sun amount: " + getSunCount();
    }

    public String showMapText() {
        StringBuilder builder = new StringBuilder();
        builder.append("Map:\n");
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Plant plant = map == null ? null : map.getPlantAt(row, col);
                builder.append(plant == null ? "." : plant.getType().name().charAt(0));
                if (col + 1 < COLS) {
                    builder.append(' ');
                }
            }
            builder.append('\n');
        }
        return builder.toString().trim();
    }

    public String showPlantsStatusText() {
        if (map == null) {
            return "Map is not ready.";
        }
        StringBuilder builder = new StringBuilder();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Plant plant = map.getPlantAt(row, col);
                if (plant != null) {
                    builder.append(plant.getType().getDisplayName())
                            .append(" at (")
                            .append(row)
                            .append(", ")
                            .append(col)
                            .append(") hp=")
                            .append(plant.getCurrentHp())
                            .append(plant.isPlantFoodActive() ? " [plant food]" : "")
                            .append('\n');
                }
            }
        }
        return builder.length() == 0 ? "No plants." : builder.toString().trim();
    }

    public String showTileStatusText(int x, int y) {
        if (map == null) {
            return "Map is not ready.";
        }
        int row = normalizeIndex(y);
        int col = normalizeIndex(x);
        if (!map.isWithinBounds(row, col)) {
            return "Invalid tile.";
        }
        Tile tile = map.getTile(row, col);
        Plant plant = tile == null ? null : tile.getPlant();
        if (plant == null) {
            return "Tile (" + row + ", " + col + ") is empty.";
        }
        return "Tile (" + row + ", " + col + ") contains " + plant.getType().getDisplayName()
                + " hp=" + plant.getCurrentHp()
                + (plant.isPlantFoodActive() ? " [plant food]" : "");
    }

    public String addSunsCheat(int amount) {
        addSun(amount);
        return "Added " + amount + " suns.";
    }

    public String addPlantFoodCheat() {
        plantFoodManager.addPlantFood(1);
        return "Added one plant food.";
    }

    public String removeCooldownCheat() {
        rechargeRemaining.clear();
        if (map != null) {
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    Plant plant = map.getPlantAt(row, col);
                    if (plant == null) {
                        continue;
                    }
                    // Reset every "*Timer" runtime-state key generically (attackTimer, lobTimer,
                    // sunTimer, meleeTimer, sunDropTimer, magnetTimer, hypnoTimer, moveTimer, ...)
                    // instead of hardcoding a handful of names, so new behaviors are covered too.
                    for (String key : new ArrayList<>(plant.getRuntimeState().keySet())) {
                        if (key.endsWith("Timer")) {
                            plant.putRuntimeState(key, 999.0);
                        }
                    }
                }
            }
        }
        return "Cooldowns removed.";
    }

    public String startZombieWavesText() {
        zombieWavesStarted = true;
        return "Zombie waves started.";
    }

    public String zombiesInfoText() {
        return "Zombies: " + zombies.size();
    }

    public String currentMenuText() {
        return AppStatus.currentMenuType == null ? "" : AppStatus.currentMenuType.name();
    }

    public String advanceTimeText(int ticks) {
        advanceTicks(ticks);
        return "Advanced time by " + ticks + " ticks.";
    }

    private int normalizeIndex(int value) {
        return Math.max(0, value - 1);
    }
}
