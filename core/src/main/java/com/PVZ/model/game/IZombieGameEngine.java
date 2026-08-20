package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.minigame.izombie.IZombieGame;
import com.PVZ.model.minigame.izombie.IZombieTexturePaths;
import com.PVZ.model.minigame.izombie.ZombieOption;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.model.enums.MinigameEnum;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.HealthBarRenderer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class IZombieGameEngine extends GameEngine implements ZombieEngine {

    private static final double TICK_SECONDS = 0.1;
    private static final float GAME_OVER_DISPLAY_DURATION = 3.0f;

    private boolean gameOverTriggered = false;
    private boolean gameOverNavigated = false;
    private boolean gameOverWin = false;
    private float gameOverTimer = 0f;

    private final List<Plant> plants = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final RegularZombieEngine zombieEngine = new RegularZombieEngine();
    private final BattleController battleController;
    private final Random random = new Random();
    private float tickAccumulator = 0f;

    private final HashMap<Integer, Zombie> sunZombiesByRow = new HashMap<>();

    private IZombieGame game;
    private Texture background;
    private Texture backgroundRight;
    private BitmapFont font;
    private BitmapFont tinyFont;
    private final ZombiePacketBar zombiePacketBar = new ZombiePacketBar();
    private Texture hudPixel;
    private float hudAnimTime = 0f;

    public IZombieGameEngine() {
        super(new GameStatus(), new IZombieInputProcessor());
        ((IZombieInputProcessor) inputProcessor).setEngine(this);
        this.battleController = new BattleController(zombieEngine.getZombies(), plants, projectiles, gameStatus);
    }

    public IZombieGame getGame() { return game; }

    public void setGame(IZombieGame game) { this.game = game; }

    @Override
    public void setMap(Map map) {
        super.setMap(map);
        zombieEngine.bindMap(map);
        battleController.setMap(map);
    }

    public void initializeBoard() {
        if (map == null || game == null) return;
        seedRandomPlants();
        spawnSunZombies();
        layoutZombieBar();
    }

    private void layoutZombieBar() {
        // Horizontal roster bar centered along the top of the screen (like the
        // plant-selection seed-packet bar), instead of a vertical column on the side.
        // Centering uses the actual roster size, since the player's selection can be
        // anywhere from 1 to 8 zombies.
        float slotSize = 130f;
        float gap = 14f;
        int count = Math.max(1, game.getRoster().size());
        float barWidth = count * slotSize + (count - 1) * gap;
        float barX = (2560f - barWidth) / 2f;
        float topY = 1440f - 40f - slotSize;
        zombiePacketBar.layout(game, barX, topY);
    }

    public ZombiePacketBar getZombiePacketBar() { return zombiePacketBar; }

    private void seedRandomPlants() {
        PlantType[] allPlants = PlantType.values();
        for (int row = 0; row < game.getRows(); row++) {
            for (int col = 0; col < game.getRedLineCol(); col++) {
                PlantType type = allPlants[random.nextInt(allPlants.length)];
                Plant plant = PlantFactory.createPlant(type, 1);
                if (plant == null) continue;
                map.setPlant(row, col, plant);
                plants.add(plant);
            }
        }
    }

    private void spawnSunZombies() {
        String alias = game.getSunZombieAlias();
        for (int row = 0; row < game.getRows(); row++) {
            Zombie z = zombieEngine.spawnZombie(alias, row, game.getCols() - 1);
            if (z != null) sunZombiesByRow.put(row, z);
        }
    }

    public String deployZombie(String alias, int row, int col) {
        if (game == null || map == null) return "No active I, Zombie game.";
        if (!map.isWithinBounds(row, col)) return "Invalid tile.";
        if (!game.isInDeployZone(col)) {
            return "Can't deploy there. Col must be greater than " + game.getRedLineCol()
                    + " (right of the red line).";
        }
        ZombieOption option = game.findOption(alias);
        if (option == null) return "Unknown zombie: " + alias;
        if (!game.trySpend(option)) return "Not enough sun.";

        Zombie z = zombieEngine.spawnZombie(option.getAlias(), row, col);
        if (z == null) {
            game.addSun(option.getCost());
            return "Could not deploy zombie.";
        }
        return "Deployed " + option.getDisplayName() + " at row " + row + ", col " + col
                + ". Sun remaining: " + game.getSun();
    }

    @Override
    public void update(float delta) {
        hudAnimTime += delta;
        if (gameOverTriggered) {
            updateGameOverTimer(delta);
            return;
        }
        if (gameStatus.isGameOver() || (game != null && game.isFinished())) return;
        battleController.update(delta);
        tickAccumulator += delta;
        while (tickAccumulator >= TICK_SECONDS) {
            tickAccumulator -= TICK_SECONDS;
            advanceOneTick((float) TICK_SECONDS);
        }
        checkLoss();
    }

    private void advanceOneTick(float delta) {
        if (game == null || map == null) return;
        updatePlants(delta);
        updateProjectiles(delta);
        updateSunZombies();
        game.tickSunProduction(delta);
        checkBrains();
        for (Zombie z : zombieEngine.getZombies()) {
            if (!z.isDead()) z.updateEffects(delta);
        }
    }

    private void updatePlants(float delta) {
        for (int row = 0; row < game.getRows(); row++) {
            for (int col = 0; col < game.getCols(); col++) {
                Plant plant = map.getPlantAt(row, col);
                if (plant == null) continue;
                plant.putRuntimeState("row", row);
                plant.putRuntimeState("col", col);
                plant.putRuntimeState("lane", row);
                plant.update(battleController, (float) TICK_SECONDS);
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

    private void updateSunZombies() {
        for (Entry<Integer, Zombie> entry : sunZombiesByRow.entrySet()) {
            int row = entry.getKey();
            Zombie z = entry.getValue();
            if (z != null && z.isDead() && game.isSunZombieAlive(row)) {
                game.markSunZombieDead(row);
            }
        }
    }

    private void checkBrains() {
        float brainLineX = map.getStartX() + map.getTileWidth() * 0.1f;
        for (Zombie z : new ArrayList<>(zombieEngine.getZombies())) {
            if (z == null || z.isDead()) continue;
            int row = (int) z.getRow();
            if (game.isBrainEaten(row)) continue;
            if (z.getX() <= brainLineX) {
                game.eatBrain(row);
                zombieEngine.kill(z);
                if (!gameOverTriggered && !gameStatus.isGameOver() && game.isWon()) {
                    if (AppStatus.currentUser != null) {
                        if (AppStatus.currentUser.progressState != null) {
                            AppStatus.currentUser.progressState.clearMinigameStage(MinigameEnum.I_ZOMBIE);
                        }
                        UserRegistry.touch(AppStatus.currentUser.profile.getUsername());
                    }
                    triggerGameOver(true);
                }
            }
        }
    }

    private void checkLoss() {
        if (game == null || game.isFinished() || gameOverTriggered) return;
        boolean anyZombieAlive = false;
        for (Zombie z : zombieEngine.getZombies()) {
            if (z != null && !z.isDead()) { anyZombieAlive = true; break; }
        }
        if (anyZombieAlive) return;

        boolean canAffordAnything = false;
        for (ZombieOption option : game.getRoster()) {
            if (game.getSun() >= option.getCost()) { canAffordAnything = true; break; }
        }
        if (!canAffordAnything) {
            triggerGameOver(false);
        }
    }

    private void triggerGameOver(boolean win) {
        if (gameOverTriggered) return;
        gameOverTriggered = true;
        gameOverTimer = 0f;
        gameOverWin = win;
        if (game != null) {
            if (win) game.markWon();
            else game.markLost();
        }
        gameStatus.setWon(win);
        gameStatus.setGameOver(true);
    }

    private void updateGameOverTimer(float delta) {
        if (!gameOverTriggered || gameOverNavigated) return;
        gameOverTimer += delta;
        if (gameOverTimer >= GAME_OVER_DISPLAY_DURATION) {
            gameOverNavigated = true;
            com.PVZ.model.status.AppStatus.returnToChapterAndLevelSelection(null);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        zombieEngine.draw(batch);
        batch.begin();
        drawRedLineAndBrains(batch);
        for (Zombie z : zombieEngine.getZombies()) {
            if (z == null || z.isDead()) continue;
            HealthBarRenderer.draw(batch, (float) z.getX(), (float) z.getY() + 120 + 2, 100,
                    (float) z.getHitpoints() / (float) Math.max(1.0, z.getMaxHitpoints()), false);
        }
        for (Plant p : plants) {
            if (p == null || p.isDead()) continue;
            p.draw(batch);
            Rectangle box = p.getHitbox();
            HealthBarRenderer.draw(batch, box.x, box.y + box.height + 2, box.width,
                    (float) p.getCurrentHp() / Math.max(1, p.getMaxHp()), true);
        }
        for (Projectile p : projectiles) {
            if (p != null) p.draw(batch);
        }
        batch.end();
        drawHud(batch);
    }

    private void drawRedLineAndBrains(SpriteBatch batch) {
        if (map == null || game == null || hudPixel == null) return;
        float tw = map.getTileWidth();
        float th = map.getTileHeight();
        float redX = map.getStartX() + game.getRedLineCol() * tw;
        float boardHeight = game.getRows() * th;
        batch.setColor(0.95f, 0.12f, 0.10f, 0.95f);
        batch.draw(hudPixel, redX - 5f, map.getStartY() - boardHeight, 10f, boardHeight);
        batch.setColor(Color.WHITE);
        for (int row = 0; row < game.getRows(); row++) {
            if (game.isBrainEaten(row)) continue;
            float y = map.getStartY() - (row + 1) * th + 18f;
            EntityRenderer.getInstance().renderPam(batch, 
                    "768/FULL/EFFECTS/BRAIN_EFFECT/BRAIN_EFFECT.PAM",
                    "animation", hudAnimTime, map.getStartX() - 100f, y);
        }
    }

    private void drawHud(SpriteBatch batch) {
        if (game == null || map == null) return;
        ensureTexturesLoaded();
        batch.begin();
        String selectedAlias = ((IZombieInputProcessor) inputProcessor).getSelectedAlias();
        zombiePacketBar.draw(batch, font, tinyFont, game, selectedAlias);
        if (!gameOverTriggered) {
            String label = String.format("Sun: %d | Brains left: %d/%d | Sun rate: %.1f",
                    game.getSun(), game.getBrainsRemaining(), game.getRows(), game.getCurrentSunRate());
            font.draw(batch, label, map.getStartX() + 20f, map.getStartY() + 40f);
        }
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
        background = new Texture(IZombieTexturePaths.BACKGROUND_LEFT);
        backgroundRight = new Texture(IZombieTexturePaths.BACKGROUND_RIGHT);
        font = FontManager.getInstance().getEnglishMenuFont();
        tinyFont = FontManager.getInstance().getEnglishTinyFont();
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        hudPixel = new Texture(pm);
        pm.dispose();
    }

    @Override
    public Texture getBackgroundOverride() {
        ensureTexturesLoaded();
        return background;
    }

    @Override
    public Texture getBackgroundOverrideRight() {
        ensureTexturesLoaded();
        return backgroundRight;
    }

    @Override
    public void dispose() {
        zombieEngine.dispose();
        battleController.dispose();
        zombiePacketBar.dispose();
        if (background != null) background.dispose();
        if (hudPixel != null) hudPixel.dispose();
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
    public int getSunCount() { return game != null ? game.getSun() : 0; }

    @Override
    public void addSun(int amount) { if (game != null) game.addSun(amount); }

    @Override
    public void spawnProjectile(Projectile p) { projectiles.add(p); }

    @Override
    public Zombie spawnZombie(String alias, int row, int col) { return zombieEngine.spawnZombie(alias, row, col); }

    @Override
    public void removePlant(int row, int col) {
        if (map != null) map.removePlant(row, col);
    }

    @Override
    public int getTileColumn(float worldX) { return map != null ? map.worldToCol(worldX) : 0; }

    public boolean isGameOver() { return gameStatus.isGameOver(); }
}
