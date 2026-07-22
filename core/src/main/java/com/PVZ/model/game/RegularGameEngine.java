package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.game.chapter.sepecialLevel.SpecialLevel;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.UserRegistry;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Main game engine for regular levels.
 * Delegates specific responsibilities to handler classes.
 */
public class RegularGameEngine extends GameEngine implements ZombieEngine, BehaviorContext, SeedBarEngine {

    // ---------- Constants ----------
    private static final double TICK_SECONDS = 0.1;
    private static final int ROWS = 5;
    private static final int COLS = 9;
    private static final float GAME_OVER_DISPLAY_DURATION = 3.0f;
    private static final double DEFAULT_CONVEYOR_INTERVAL_SECONDS = 12.0;
    private static final double SKY_SUN_INTERVAL_SECONDS = 10.0;

    // ---------- Fields (used by handlers) ----------
    final List<Projectile> projectiles = new ArrayList<>();
    final List<Zombie> zombies = new ArrayList<>();
    final SunManager sunManager = new SunManager();
    final PlantFoodManager plantFoodManager = new PlantFoodManager();
    final Random random = new Random();
    float tickAccumulator = 0f;
    boolean zombieWavesStarted = false;
    PlantType selectedPlantType;
    final java.util.Map<PlantType, Double> rechargeRemaining = new java.util.EnumMap<>(PlantType.class);

    boolean conveyorBeltMode = false;
    double conveyorIntervalSeconds = DEFAULT_CONVEYOR_INTERVAL_SECONDS;
    double conveyorTimer = 0.0;
    final List<PlantType> conveyorBeltQueue = new ArrayList<>();
    boolean lockedPlantsMode = false;
    final java.util.Set<PlantType> lockedPlantsForStage = new java.util.LinkedHashSet<>();

    final SeedPacketBar seedPacketBar = new SeedPacketBar();
    final RegularZombieEngine zombieEngine;
    final List<Plant> plants = new ArrayList<>();
    WaveManager waveManager;
    BattleController battleController;

    boolean gameOverTriggered = false;
    boolean gameOverNavigated = false;
    float gameOverTimer = 0f;
    boolean gameOverWin = false;

    SpecialLevel specialLevel;
    String backgroundTexturePath;
    com.badlogic.gdx.graphics.Texture backgroundOverrideTexture;
    double skySunTimer = 0.0;
    final com.PVZ.model.entity.LawnMower[] lawnMowers = new com.PVZ.model.entity.LawnMower[ROWS];
    com.badlogic.gdx.graphics.Texture iceOverlayTex;

    // ---------- Constructor ----------
    public RegularGameEngine(GameStatus gameStatus) {
        this(gameStatus, createDefaultWaves());
    }

    public RegularGameEngine(GameStatus gameStatus, List<Wave> waves) {
        super(gameStatus, new RegularInputProcessor());
        ((RegularInputProcessor) inputProcessor).setRegularGameEngine(this);
        if (gameStatus != null && gameStatus.getSunflower() <= 0) {
            gameStatus.setSunflower(0);
        }
        this.zombieEngine = new RegularZombieEngine();
        this.battleController = new BattleController(zombieEngine.getZombies(), plants, projectiles, gameStatus);
        this.waveManager = new WaveManager(waves);
    }

    private static List<Wave> createDefaultWaves() {
        List<Wave> waves = new ArrayList<>();
        List<Wave.WaveEntry> e = new ArrayList<>();
        e.add(new Wave.WaveEntry("ZombieTutorialDefault", 5, 1.5f));
        waves.add(new Wave(e, 5f));
        return waves;
    }

    // ---------- Overrides ----------
    @Override
    public void setMap(Map map) {
        super.setMap(map);
        if (zombieEngine != null) zombieEngine.bindMap(map);
        if (battleController != null) battleController.setMap(map);
        BoardHandler.initLawnMowers(this, map);
    }

    @Override
    public void update(float delta) {
        UpdateHandler.update(this, delta);
    }

    @Override
    public void draw(SpriteBatch batch) {
        DrawHandler.draw(this, batch);
    }

    @Override
    public void dispose() {
        projectiles.clear();
        zombies.clear();
        sunManager.clear();
        plantFoodManager.reset();
        if (backgroundOverrideTexture != null) {
            backgroundOverrideTexture.dispose();
            backgroundOverrideTexture = null;
        }
        if (zombieEngine != null) zombieEngine.dispose();
    }

    // ---------- Interface: ZombieEngine ----------
    @Override
    public List<Zombie> getZombiesInLane(int lane) {
        return CombatHandler.getZombiesInLane(this, lane);
    }

    @Override
    public List<Zombie> getAllZombies() {
        return Collections.unmodifiableList(getZombieList());
    }

    @Override
    public void kill(Object entity) {
        // delegated in CombatHandler if needed
    }

    @Override
    public void takeDamage(Object entity, double amount) {
        // delegated
    }

    @Override
    public Zombie spawnZombie(String alias, int row, int x) {
        return CombatHandler.spawnZombie(this, alias, row, x);
    }

    // ---------- Interface: BehaviorContext ----------
    @Override
    public Plant getPlantAt(int row, int col) {
        return map == null ? null : map.getPlantAt(row, col);
    }

    @Override
    public List<Plant> getAllPlants() {
        if (map == null) return List.of();
        List<Plant> result = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Plant p = map.getPlantAt(r, c);
                if (p != null && !p.isDead()) result.add(p);
            }
        }
        return result;
    }

    @Override
    public void spawnProjectile(Object projectile) {
        CombatHandler.spawnProjectile(this, projectile);
    }

    @Override
    public void spawnProjectile(Projectile p) {
        CombatHandler.spawnProjectile(this, p);
    }

    @Override
    public void spawnSun(int amount) {
        SunHandler.spawnSun(this, amount);
    }

    @Override
    public void spawnSunAt(int row, int col, int amount) {
        SunHandler.spawnSunAt(this, row, col, amount);
    }

    @Override
    public void damageArea(int lane, int row, int damage) {
        CombatHandler.damageArea(this, lane, row, damage);
    }

    @Override
    public void freezeZombiesInLane(int lane, double seconds) {
        CombatHandler.freezeZombiesInLane(this, lane, seconds);
    }

    @Override
    public void freezeAllZombies(double seconds) {
        CombatHandler.freezeAllZombies(this, seconds);
    }

    @Override
    public void disarmZombiesInLane(int lane) {
        CombatHandler.disarmZombiesInLane(this, lane);
    }

    @Override
    public void moveZombiesFromLane(int sourceLane, int targetLane) {
        CombatHandler.moveZombiesFromLane(this, sourceLane, targetLane);
    }

    @Override
    public void pullAdjacentZombiesToLane(int lane) {
        CombatHandler.pullAdjacentZombiesToLane(this, lane);
    }

    @Override
    public void killRandomZombies(int count) {
        CombatHandler.killRandomZombies(this, count);
    }

    @Override
    public void killClosestZombieInLane(int lane) {
        CombatHandler.killClosestZombieInLane(this, lane);
    }

    @Override
    public void hypnotizeZombiesInLane(int lane, double seconds) {
        CombatHandler.hypnotizeZombiesInLane(this, lane, seconds);
    }

    @Override
    public void healPlantAt(int row, int col, int amount) {
        Plant plant = getPlantAt(row, col);
        if (plant != null) plant.heal(Math.max(0, amount));
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
        if (gameStatus == null || amount == 0) return;
        int next = gameStatus.getSunflower() + amount;
        gameStatus.setSunflower(Math.max(0, next));
    }

    @Override
    public void removePlant(int row, int col) {
        if (map != null) map.removePlant(row, col);
    }

    @Override
    public int getTileColumn(float worldX) {
        return map != null ? map.worldToCol(worldX) : 0;
    }

    // ---------- Interface: SeedBarEngine ----------
    @Override
    public boolean isConveyorBeltMode() { return conveyorBeltMode; }

    @Override
    public boolean isOnCooldown(PlantType type) {
        return rechargeRemaining.getOrDefault(type, 0.0) > 0.0;
    }

    @Override
    public double getRechargeRemainingSeconds(PlantType type) {
        return rechargeRemaining.getOrDefault(type, 0.0);
    }

    // ---------- Public methods (delegated) ----------
    public void triggerGameOver(boolean win) { UpdateHandler.triggerGameOver(this, win); }
    public boolean isGameOverTriggered() { return gameOverTriggered; }
    public float getGameOverTimer() { return gameOverTimer; }
    public boolean isGameOverWin() { return gameOverWin; }
    public void resetGameOverState() { UpdateHandler.resetGameOverState(this); }
    public void updateGameOverTimer(float delta) { UpdateHandler.updateGameOverTimer(this, delta); }

    public void advanceTicks(int ticks) { UpdateHandler.advanceTicks(this, ticks); }
    public void enableConveyorBelt(double intervalSeconds) { WaveHandler.enableConveyorBelt(this, intervalSeconds); }
    public void enableLockedPlants(java.util.Collection<PlantType> locked) {
        WaveHandler.enableLockedPlants(this, locked);
    }

    public String plantPlant(String plantType, int x, int y) {
        PlantType type;
        try { type = PlantType.fromName(plantType); } catch (Exception e) { return "Unknown plant type: " + plantType; }
        return PlantHandler.plantPlant(this, type, x, y);
    }

    public String plantPlant(PlantType type, int x, int y) {
        return PlantHandler.plantPlant(this, type, x, y);
    }

    public void selectPlant(PlantType type) {
        this.selectedPlantType = (selectedPlantType == type) ? null : type;
    }
    public PlantType getSelectedPlantType() { return selectedPlantType; }
    public void clearSelection() { this.selectedPlantType = null; }

    public String plantSelectedAt(int x, int y) {
        if (selectedPlantType == null) return "No seed selected.";
        String result = plantPlant(selectedPlantType, x, y);
        clearSelection();
        return result;
    }

    public String pluckPlant(int x, int y) {
        if (map == null) return "Map is not ready.";
        int row = normalizeIndex(y);
        int col = normalizeIndex(x);
        Plant plant = map.getPlantAt(row, col);
        if (plant == null) return "No plant at selected tile.";
        map.removePlant(row, col);
        return "Plant plucked from (" + col + ", " + row + ")";
    }

    public String feedPlant(int x, int y) {
        if (map == null) return "Map is not ready.";
        int row = normalizeIndex(y);
        int col = normalizeIndex(x);
        Plant plant = map.getPlantAt(row, col);
        if (plant == null) return "No plant at selected tile.";
        if (!plantFoodManager.consumePlantFood()) return "No plant food available.";
        plant.applyPlantFood(this);
        return "Plant fed at (" + col + ", " + row + ")";
    }

    public int collectSunAtWorldPoint(float worldX, float worldY) {
        return SunHandler.collectSunAtWorldPoint(this, worldX, worldY);
    }

    public String collectSunAt(int x, int y) {
        return SunHandler.collectSunAt(this, x, y);
    }

    public String showSunAmountText() { return "Sun amount: " + getSunCount(); }

    public String showMapText() { return MapHandler.showMapText(this); }
    public String showPlantsStatusText() { return MapHandler.showPlantsStatusText(this); }
    public String showTileStatusText(int x, int y) { return MapHandler.showTileStatusText(this, x, y); }

    public String addSunsCheat(int amount) { addSun(amount); return "Added " + amount + " suns."; }
    public String addPlantFoodCheat() { plantFoodManager.addPlantFood(1); return "Added one plant food."; }
    public String removeCooldownCheat() { return PlantHandler.removeCooldownCheat(this); }
    public String startZombieWavesText() { return WaveHandler.startZombieWavesText(this); }
    public String zombiesInfoText() { return CombatHandler.zombiesInfoText(this); }
    public String currentMenuText() { return AppStatus.currentMenuType == null ? "" : AppStatus.currentMenuType.name(); }
    public String advanceTimeText(int ticks) { advanceTicks(ticks); return "Advanced time by " + ticks + " ticks."; }

    // ---------- Getters ----------
    public SpecialLevel getSpecialLevel() { return specialLevel; }
    public void setSpecialLevel(SpecialLevel specialLevel) { this.specialLevel = specialLevel; }
    public SeedPacketBar getSeedPacketBar() { return seedPacketBar; }
    public java.util.Set<PlantType> getLockedPlantsForStage() { return Collections.unmodifiableSet(lockedPlantsForStage); }
    public List<PlantType> getConveyorBeltQueue() { return Collections.unmodifiableList(conveyorBeltQueue); }
    public RegularZombieEngine getZombieEngine() { return zombieEngine; }
    public SunManager getSunManager() { return sunManager; }
    public BattleController getBattleController() { return battleController; }
    public WaveManager getWaveManager() { return waveManager; }
    public void startWaves() { WaveHandler.startWaves(this); }

    // ---------- ADDED: getter for lockedPlantsMode ----------
    public boolean isLockedPlantsMode() {
        return lockedPlantsMode;
    }

    public void setBackgroundTexturePath(String path) { this.backgroundTexturePath = path; }
    @Override
    public com.badlogic.gdx.graphics.Texture getBackgroundOverride() {
        if (backgroundTexturePath == null || backgroundTexturePath.isEmpty()) return null;
        if (backgroundOverrideTexture == null) {
            String internalPath = backgroundTexturePath;
            if (com.badlogic.gdx.Gdx.files.internal(internalPath).exists()) {
                backgroundOverrideTexture = new com.badlogic.gdx.graphics.Texture(
                    com.badlogic.gdx.Gdx.files.internal(internalPath));
            } else {
                System.out.println("RegularGameEngine: background not found: " + internalPath);
                return null;
            }
        }
        return backgroundOverrideTexture;
    }

    // ---------- Package-private helpers ----------
    List<Zombie> getZombieList() {
        if (zombieEngine != null && zombieEngine.getZombies() != null) return zombieEngine.getZombies();
        return zombies;
    }

    int normalizeIndex(int value) { return value; }

    com.badlogic.gdx.graphics.Texture iceOverlayTexture() {
        if (iceOverlayTex == null) {
            com.badlogic.gdx.graphics.Pixmap pm = new com.badlogic.gdx.graphics.Pixmap(1, 1,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pm.setColor(0.4f, 0.7f, 1f, 1f);
            pm.fill();
            iceOverlayTex = new com.badlogic.gdx.graphics.Texture(pm);
            pm.dispose();
        }
        return iceOverlayTex;
    }

    // ---------- Static utilities (moved from original) ----------
    public static String tileDebugLabel(TileType type) { return MapHandler.tileDebugLabel(type); }
    public static String tileDebugList(Map map) { return MapHandler.tileDebugList(map); }
}
