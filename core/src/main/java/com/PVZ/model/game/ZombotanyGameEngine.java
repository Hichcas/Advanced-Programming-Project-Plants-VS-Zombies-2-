package com.PVZ.model.game;

import com.PVZ.model.entity.LawnMower;
import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Sun;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.minigame.zombotany.ZombotanyGame;
import com.PVZ.model.minigame.zombotany.ZombotanyTexturePaths;
import com.PVZ.model.status.AppStatus;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.HealthBarRenderer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

public class ZombotanyGameEngine extends GameEngine implements ZombieEngine, SeedBarEngine {

    private static final double TICK_SECONDS = 0.1;
    private static final double SKY_SUN_INTERVAL_SECONDS = 10.0;
    private static final float GAME_OVER_DISPLAY_DURATION = 3.0f;

    private final List<Plant> plants = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final RegularZombieEngine zombieEngine = new RegularZombieEngine();
    private final SunManager sunManager = new SunManager();
    private final SeedPacketBar seedPacketBar = new SeedPacketBar();
    private final Random random = new Random();
    private final java.util.Map<PlantType, Double> rechargeRemaining = new EnumMap<>(PlantType.class);
    private final BattleController battleController;

    private float tickAccumulator = 0f;
    private double skySunTimer = 0.0;

    private ZombotanyGame game;
    private WaveManager waveManager;
    private LawnMower[] lawnMowers = new LawnMower[0];

    private PlantType selectedPlantType;

    private boolean gameOverTriggered = false;
    private boolean gameOverNavigated = false;
    private boolean gameOverWin = false;
    private float gameOverTimer = 0f;

    private Texture background;
    private BitmapFont font;
    private BitmapFont tinyFont;

    public ZombotanyGameEngine() {
        super(new GameStatus(), new ZombotanyInputProcessor());
        ((ZombotanyInputProcessor) inputProcessor).setEngine(this);
        this.battleController = new BattleController(zombieEngine.getZombies(), plants, projectiles, gameStatus);
    }

    public ZombotanyGame getGame() { return game; }

    public void setGame(ZombotanyGame game) { this.game = game; }

    @Override
    public void setMap(Map map) {
        super.setMap(map);
        zombieEngine.bindMap(map);
        battleController.setMap(map);
    }

    public void initializeBoard() {
        if (map == null || game == null) return;

        gameStatus.setSunflower(game.getStartingSun());

        seedPacketBar.layout(game.getPlantPool(), 40f, 1290f);

        initLawnMowers();

        waveManager = new WaveManager(game.getWaves());
        waveManager.start();
        skySunTimer = 0.0;
    }

    private void initLawnMowers() {
        int rows = game.getRows();
        lawnMowers = new LawnMower[rows];
        float tileWidth = map.getTileWidth();
        float tileHeight = map.getTileHeight();
        float startX = map.getStartX();
        float startY = map.getStartY();
        double triggerX = startX - tileWidth * 0.75;
        double travelLimitX = 2560 + tileWidth;
        for (int row = 0; row < rows; row++) {
            LawnMower mower = new LawnMower();
            double parkY = startY - (row + 1) * tileHeight + tileHeight * 0.15;
            mower.init(row, parkY, triggerX, travelLimitX);
            lawnMowers[row] = mower;
        }
    }

    @Override
    public void update(float delta) {
        if (gameOverTriggered) {
            updateGameOverTimer(delta);
            return;
        }
        if (gameStatus.isGameOver() || (game != null && game.isFinished())) return;

        if (waveManager != null) {
            waveManager.update(delta, zombieEngine);
        }

        battleController.update(delta);

        tickAccumulator += delta;
        while (tickAccumulator >= TICK_SECONDS) {
            tickAccumulator -= TICK_SECONDS;
            advanceOneTick((float) TICK_SECONDS);
        }

        checkWin();
    }

    private void advanceOneTick(float delta) {
        if (game == null || map == null) return;
        updatePlants();
        updateProjectiles(delta);
        sunManager.update(delta);
        updateSkySun(delta);
        updateLawnMowers(delta);
        rechargeRemaining.replaceAll((type, remaining) -> Math.max(0.0, remaining - TICK_SECONDS));
    }

    private void updatePlants() {
        for (int row = 0; row < game.getRows(); row++) {
            for (int col = 0; col < game.getCols(); col++) {
                Plant plant = map.getPlantAt(row, col);
                if (plant == null) continue;
                plant.putRuntimeState("row", row);
                plant.putRuntimeState("col", col);
                plant.putRuntimeState("lane", row);
                plant.update(battleController, TICK_SECONDS);
            }
        }
        plants.removeIf(p -> {
            if (p != null && p.isDead()) {
                int r = intState(p, "row");
                int c = intState(p, "col");
                map.removePlant(r, c);
                return true;
            }
            return false;
        });
    }

    private int intState(Plant p, String key) {
        Object v = p.getRuntimeState(key);
        return v instanceof Number ? ((Number) v).intValue() : 0;
    }

    private void updateProjectiles(float delta) {
        for (Projectile p : projectiles) p.update(delta);
        projectiles.removeIf(Projectile::isDestroyed);
    }

    private void updateSkySun(float delta) {
        if (map == null) return;
        if (gameStatus.isNoSkySun()) return;
        skySunTimer += delta;
        if (skySunTimer < SKY_SUN_INTERVAL_SECONDS) return;
        skySunTimer = 0.0;

        int row = random.nextInt(Math.max(1, game.getRows()));
        int col = random.nextInt(Math.max(1, game.getCols()));
        com.PVZ.model.entity.Tile tile = map.getTile(row, col);
        if (tile == null) return;

        double landingX = tile.getX() + tile.getWidth() / 2.0;
        double groundY = tile.getY() + tile.getHeight() / 2.0;
        double startY = map.getStartY() + map.getTileHeight() * 2.0;
        sunManager.spawnFalling(landingX, startY, 25, groundY);
    }

    private void updateLawnMowers(float delta) {
        for (LawnMower mower : lawnMowers) {
            if (mower == null) continue;

            if (!mower.isTriggered() && !mower.isUsed()) {
                for (Zombie z : getZombiesInLane(mower.getRow())) {
                    if (z != null && !z.isDead() && z.getX() <= mower.getFrontX()) {
                        mower.trigger();
                        zombieEngine.kill(z);
                        break;
                    }
                }
            }

            if (mower.isTriggered() && !mower.isUsed()) {
                mower.advance(delta);
                for (Zombie z : getZombiesInLane(mower.getRow())) {
                    if (z != null && !z.isDead() && mower.getHitbox().overlaps(z.getHitbox())) {
                        zombieEngine.kill(z);
                    }
                }
            }

            if (mower.isUsed() && !gameStatus.isGameOver()) {
                for (Zombie z : getZombiesInLane(mower.getRow())) {
                    if (z != null && !z.isDead() && z.getX() <= mower.getFrontX()) {
                        triggerLoss();
                        return;
                    }
                }
            }
        }
    }

    private void checkWin() {
        if (game == null || game.isFinished() || gameStatus.isGameOver()) return;
        if (waveManager == null || !waveManager.isFinished()) return;
        for (Zombie z : zombieEngine.getZombies()) {
            if (z != null && !z.isDead()) return;
        }
        triggerWin();
    }

    private void triggerWin() {
        triggerGameOver(true);
    }

    private void triggerLoss() {
        triggerGameOver(false);
    }

    private void triggerGameOver(boolean win) {
        if (gameOverTriggered || game == null) return;
        gameOverTriggered = true;
        gameOverTimer = 0f;
        gameOverWin = win;
        if (win) {
            game.markWon();
        } else {
            game.markLost();
        }
        gameStatus.setWon(win);
        gameStatus.setGameOver(true);
    }

    private void updateGameOverTimer(float delta) {
        if (!gameOverTriggered || gameOverNavigated) return;
        gameOverTimer += delta;
        if (gameOverTimer >= GAME_OVER_DISPLAY_DURATION) {
            gameOverNavigated = true;
            AppStatus.returnToChapterAndLevelSelection(null);
        }
    }

    public SeedPacketBar getSeedPacketBar() { return seedPacketBar; }

    public void selectPlant(PlantType type) {
        this.selectedPlantType = (selectedPlantType == type) ? null : type;
    }

    public PlantType getSelectedPlantType() { return selectedPlantType; }

    public void clearSelection() { this.selectedPlantType = null; }

    public boolean isOnCooldown(PlantType type) {
        return rechargeRemaining.getOrDefault(type, 0.0) > 0.0;
    }

    public double getRechargeRemainingSeconds(PlantType type) {
        return rechargeRemaining.getOrDefault(type, 0.0);
    }

    public String plantSelectedAt(int col, int row) {
        if (selectedPlantType == null) return "No seed selected.";
        String result = plantPlant(selectedPlantType, col, row);
        clearSelection();
        return result;
    }

    public String plantPlant(PlantType type, int col, int row) {
        if (map == null) return "Map is not ready.";
        if (type == null) return "Unknown plant type.";
        if (!map.isWithinBounds(row, col)) return "Invalid tile.";
        if (map.getPlantAt(row, col) != null) return "Tile is occupied.";
        if (game != null && !game.isPlantAllowed(type)) {
            return "Plant not available in this level: " + type.getDisplayName();
        }
        if (isOnCooldown(type)) return type.getDisplayName() + " is still recharging.";

        Plant plant = PlantFactory.createPlant(type, 1);
        if (plant == null) return "Cannot create plant.";

        int cost = plant.getStats().getCost();
        if (getSunCount() < cost) return "Not enough sun.";
        addSun(-cost);

        plant.setPlanted(true);
        plant.putRuntimeState("row", row);
        plant.putRuntimeState("col", col);
        plant.putRuntimeState("lane", row);
        map.setPlant(row, col, plant);
        plants.add(plant);

        double recharge = plant.getStats().getRechargeSeconds();
        if (recharge > 0) rechargeRemaining.put(type, recharge);

        return "Planted " + type.getDisplayName() + " at (" + col + ", " + row + ").";
    }

    public int collectSunAtWorldPoint(float worldX, float worldY) {
        Rectangle pointer = new Rectangle(worldX - 8f, worldY - 8f, 16f, 16f);
        int collected = sunManager.collectAt(pointer);
        if (collected > 0) addSun(collected);
        return collected;
    }

    @Override
    public void draw(SpriteBatch batch) {
        zombieEngine.draw(batch);

        batch.begin();
        battleController.drawProjectiles(batch);
        for (Projectile p : projectiles) {
            if (p != null) p.draw(batch);
        }
        for (Sun sun : sunManager.getSuns()) {
            sun.draw(batch);
        }
        for (LawnMower mower : lawnMowers) {
            if (mower != null) mower.draw(batch);
        }
        if (map != null && game != null) {
            for (int row = 0; row < game.getRows(); row++) {
                for (int col = 0; col < game.getCols(); col++) {
                    Plant plant = map.getPlantAt(row, col);
                    if (plant == null || plant.isDead()) continue;
                    plant.draw(batch);
                    Rectangle box = plant.getHitbox();
                    HealthBarRenderer.draw(batch, box.x, box.y + box.height + 2, box.width,
                            (float) plant.getCurrentHp() / Math.max(1, plant.getMaxHp()), true);
                }
            }
        }
        for (Zombie z : zombieEngine.getZombies()) {
            if (z == null || z.isDead()) continue;
            HealthBarRenderer.draw(batch, (float) z.getX(), (float) z.getY() + 120 + 2, 100,
                    (float) z.getHitpoints() / (float) Math.max(1.0, z.getMaxHitpoints()), false);
        }
        batch.end();

        drawHud(batch);
    }

    private void drawHud(SpriteBatch batch) {
        if (game == null || map == null) return;
        ensureTexturesLoaded();
        batch.begin();

        float hudX = map.getStartX() + 20f;
        float hudY = map.getStartY() + 290f;
        int waveNum = waveManager != null ? Math.min(waveManager.getCurrentWave() + 1, waveManager.getTotalWaves()) : 0;
        int waveTotal = waveManager != null ? waveManager.getTotalWaves() : 0;
        String selected = selectedPlantType != null ? selectedPlantType.getDisplayName() : "none";
        String label = String.format("Sun: %d | Wave: %d/%d | Selected: %s",
                getSunCount(), waveNum, waveTotal, selected);
        font.draw(batch, label, hudX, hudY);
        batch.end();

        drawGameOverOverlay(batch);
    }

    private void drawGameOverOverlay(SpriteBatch batch) {
        if (!gameOverTriggered) return;
        ensureTexturesLoaded();
        float alpha;
        if (gameOverTimer < 1.0f) {
            alpha = Math.max(0f, gameOverTimer);
        } else if (gameOverTimer > 2.5f) {
            alpha = Math.max(0f, 1.0f - (gameOverTimer - 2.5f) / 0.5f);
        } else {
            alpha = 1.0f;
        }
        batch.begin();
        String message = gameOverWin ? "YOU WIN!" : "YOU LOSE!";
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, message);
        float x = 1280f - layout.width / 2f;
        float y = 720f + layout.height / 2f;
        font.setColor(1f, 1f, 1f, alpha);
        font.draw(batch, message, x, y);
        font.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private void ensureTexturesLoaded() {
        if (font != null) return;
        background = new Texture(ZombotanyTexturePaths.BACKGROUND);
        font = FontManager.getInstance().getEnglishMenuFont();
        tinyFont = FontManager.getInstance().getEnglishTinyFont();
    }

    @Override
    public Texture getBackgroundOverride() {
        ensureTexturesLoaded();
        return background;
    }

    @Override
    public void dispose() {
        zombieEngine.dispose();
        battleController.dispose();
        sunManager.clear();
        if (background != null) background.dispose();
    }

    @Override
    public void kill(Object entity) { zombieEngine.kill(entity); }

    @Override
    public void takeDamage(Object entity, double amount) { zombieEngine.takeDamage(entity, amount); }

    @Override
    public Plant getPlantAt(int row, int col) { return map != null ? map.getPlantAt(row, col) : null; }

    @Override
    public List<Zombie> getZombiesInLane(int lane) { return zombieEngine.getZombiesInLane(lane); }

    @Override
    public int getSunCount() { return gameStatus == null ? 0 : gameStatus.getSunflower(); }

    @Override
    public void addSun(int amount) {
        if (gameStatus == null || amount == 0) return;
        gameStatus.setSunflower(Math.max(0, gameStatus.getSunflower() + amount));
    }

    @Override
    public void spawnProjectile(Projectile p) { if (p != null) projectiles.add(p); }

    @Override
    public Zombie spawnZombie(String alias, int row, int col) { return zombieEngine.spawnZombie(alias, row, col); }

    @Override
    public void removePlant(int row, int col) { if (map != null) map.removePlant(row, col); }

    @Override
    public int getTileColumn(float worldX) { return map != null ? map.worldToCol(worldX) : 0; }

    public boolean isGameOver() { return gameStatus.isGameOver(); }
}
