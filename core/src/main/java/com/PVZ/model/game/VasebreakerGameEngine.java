package com.PVZ.model.game;

import com.PVZ.model.entity.LawnMower;
import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.MinigameEnum;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.minigame.vasebreaker.DroppedSeedPacket;
import com.PVZ.model.minigame.vasebreaker.Vase;
import com.PVZ.model.minigame.vasebreaker.VasebreakerGame;
import com.PVZ.model.minigame.vasebreaker.VasebreakerTexturePaths;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.renderer.EntityRenderer;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.HealthBarRenderer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VasebreakerGameEngine extends GameEngine implements ZombieEngine, com.PVZ.model.minigame.vasebreaker
    .VasebreakerEngineCallback {

    private static final double TICK_SECONDS = 0.1;
    private static final float GAME_OVER_DISPLAY_DURATION = 3.0f;

    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Plant> plants = new ArrayList<>();
    private final RegularZombieEngine zombieEngine = new RegularZombieEngine();
    private final BattleController battleController;
    private LawnMower[] lawnMowers;
    private float tickAccumulator = 0f;
    private boolean levelWon = false;

    private boolean gameOverTriggered = false;
    private boolean gameOverNavigated = false;
    private boolean gameOverWin = false;
    private float gameOverTimer = 0f;

    private VasebreakerGame game;
    private Texture seedPacketGround;
    private Texture background;
    private Texture backgroundRight;
    private BitmapFont font;
    private float vaseAnimTime = 0f;
    /** ظرف‌هایی که همین الان شکسته شده‌اند: مقدار = زمان سپری‌شده از لحظه‌ی شکستن (برای پخش کلیپ break). */
    private final HashMap<Vase, Float> breakingVases = new HashMap<>();
    private static final float BREAK_CLIP_DURATION = 1.8f;

    /**
     * مسیر PAM واقعی هر نوع کوزه، طبق pam_animations.json: کوزه‌ی معمولی قهوه‌ای (VASE_BROWN)،
     * کوزه‌ای که گیاه داخلش هست سبز رنگ (VASE_GREEN)، و کوزه‌ی گارگانتوار (VASE_GARGANTUAR).
     */
    private static String vasePamPath(com.PVZ.model.minigame.vasebreaker.VaseType type) {
        return switch (type) {
            case NORMAL -> "768/FULL/VASEBREAKER/VASE_BROWN/VASE_BROWN.PAM";
            case PLANT -> "768/FULL/VASEBREAKER/VASE_GREEN/VASE_GREEN.PAM";
            case GARGANTUAR -> "768/FULL/VASEBREAKER/VASE_GARGANTUAR/VASE_GARGANTUAR.PAM";
        };
    }

    public VasebreakerGameEngine() {
        super(new GameStatus(), new VasebreakerInputProcessor());
        ((VasebreakerInputProcessor) inputProcessor).setEngine(this);
        this.battleController = new BattleController(zombieEngine.getZombies(), plants, projectiles, gameStatus);
    }

    public VasebreakerGame getGame() {
        return game;
    }

    public void setGame(VasebreakerGame game) {
        this.game = game;
    }

    @Override
    public void setMap(Map map) {
        super.setMap(map);
        zombieEngine.bindMap(map);
        battleController.setMap(map);
        initLawnMowers(map);
    }

    private void initLawnMowers(Map map) {
        if (map == null) return;
        int rows = game != null ? game.getRows() : 5;
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
    public LawnMower getLawnMower(int row) {
        if (lawnMowers == null || row < 0 || row >= lawnMowers.length) return null;
        return lawnMowers[row];
    }

    @Override
    public void update(float delta) {
        vaseAnimTime += delta;
        if (game != null) {
            for (int row = 0; row < game.getRows(); row++) {
                for (int col = 0; col < game.getCols(); col++) {
                    Vase vase = game.getVase(row, col);
                    if (vase != null && vase.isBroken() && !breakingVases.containsKey(vase)) {
                        breakingVases.put(vase, 0f);
                    }
                }
            }
            breakingVases.replaceAll((v, t) -> t + delta);
        }
        if (gameOverTriggered) {
            updateGameOverTimer(delta);
            return;
        }
        if (gameStatus.isGameOver()) return;
        if (game != null) game.update(delta);
        battleController.update(delta);
        tickAccumulator += delta;
        while (tickAccumulator >= TICK_SECONDS) {
            tickAccumulator -= TICK_SECONDS;
            advanceOneTick();
        }
    }

    private void advanceOneTick() {
        if (gameStatus.isGameOver()) return;
        updatePlants();
        for (Projectile p : projectiles) {
            p.update((float) TICK_SECONDS);
        }
        projectiles.removeIf(Projectile::isDestroyed);
        updateLawnMowers((float) TICK_SECONDS);
        for (Zombie z : zombieEngine.getZombies()) {
            if (!z.isDead()) z.updateEffects((float) TICK_SECONDS);
        }
    }

    private void updatePlants() {
        if (map == null) return;
        List<Plant> snapshot = new ArrayList<>(plants);
        for (Plant plant : snapshot) {
            plant.update(battleController, TICK_SECONDS);
            if (plant.isDead()) {
                int row = asInt(plant.getRuntimeState("row"));
                int col = asInt(plant.getRuntimeState("col"));
                map.removePlant(row, col);
                plants.remove(plant);
            }
        }
    }

    private void updateLawnMowers(float delta) {
        if (lawnMowers == null) return;
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
                        triggerGameOver(false);
                        return;
                    }
                }
            }
        }
    }

    private void triggerGameOver(boolean win) {
        if (gameOverTriggered) return;
        gameOverTriggered = true;
        gameOverTimer = 0f;
        gameOverWin = win;
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

    @Override
    public void draw(SpriteBatch batch) {
        batch.begin();
        for (Plant plant : plants) {
            plant.draw(batch);
            com.badlogic.gdx.math.Rectangle box = plant.getHitbox();
            HealthBarRenderer.draw(batch, box.x, box.y + box.height + 2, box.width,
                (float) plant.getCurrentHp() / Math.max(1, plant.getMaxHp()), true);
        }
        for (Projectile p : projectiles) {
            p.draw(batch);
        }
        batch.end();
        zombieEngine.draw(batch);
        batch.begin();
        for (Zombie z : zombieEngine.getZombies()) {
            if (z == null || z.isDead()) continue;
            HealthBarRenderer.draw(batch, (float) z.getX(), (float) z.getY() + 120 + 2, 100,
                (float) z.getHitpoints() / (float) Math.max(1.0, z.getMaxHitpoints()), false);
        }
        batch.end();
        if (lawnMowers != null) {
            batch.begin();
            for (LawnMower mower : lawnMowers) {
                if (mower != null) mower.draw(batch);
            }
            batch.end();
        }
        drawVases(batch);
    }

    private void drawVases(SpriteBatch batch) {
        if (game == null || map == null) return;
        ensureVaseTexturesLoaded();

        batch.begin();
        for (int row = 0; row < game.getRows(); row++) {
            for (int col = 0; col < game.getCols(); col++) {
                Vase vase = game.getVase(row, col);
                if (vase == null) continue;
                com.PVZ.model.entity.Tile tile = map.getTile(row, col);
                if (tile == null) continue;

                String pamPath = vasePamPath(vase.getType());
                float cx = tile.getX() + tile.getWidth() / 2f;
                float cy = tile.getY() + tile.getHeight() / 2f;

                if (!vase.isBroken()) {
                    EntityRenderer.getInstance().renderPam(batch, pamPath, "idle", vaseAnimTime, cx, cy);
                } else {
                    Float breakElapsed = breakingVases.get(vase);
                    if (breakElapsed != null && breakElapsed <= BREAK_CLIP_DURATION) {
                        EntityRenderer.getInstance().renderPam(batch, pamPath, "break", breakElapsed, cx, cy);
                    }
                }
            }
        }

        for (DroppedSeedPacket packet : game.getGroundSeedPackets()) {
            com.PVZ.model.entity.Tile tile = map.getTile(packet.getRow(), packet.getCol());
            if (tile == null) continue;
            float w = tile.getWidth() * 0.5f;
            float h = tile.getHeight() * 0.5f;
            batch.draw(seedPacketGround, tile.getX() + (tile.getWidth() - w) / 2f,
                tile.getY() + (tile.getHeight() - h) / 2f, w, h);
        }

        batch.end();
        drawGameOverOverlay(batch);
    }

    private void drawGameOverOverlay(SpriteBatch batch) {
        if (!gameOverTriggered) return;
        ensureVaseTexturesLoaded();
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

    private void ensureVaseTexturesLoaded() {
        if (seedPacketGround != null) return;
        seedPacketGround = new Texture(VasebreakerTexturePaths.SEED_PACKET_GROUND);
        background = new Texture(VasebreakerTexturePaths.BACKGROUND_LEFT);
        backgroundRight = new Texture(VasebreakerTexturePaths.BACKGROUND_RIGHT);
        font = FontManager.getInstance().getEnglishMenuFont();
    }

    @Override
    public Texture getBackgroundOverride() {
        ensureVaseTexturesLoaded();
        return background;
    }

    @Override
    public Texture getBackgroundOverrideRight() {
        ensureVaseTexturesLoaded();
        return backgroundRight;
    }

    @Override
    public void dispose() {
        zombieEngine.dispose();
        battleController.dispose();
        if (seedPacketGround != null) seedPacketGround.dispose();
        if (background != null) background.dispose();
    }

    @Override
    public void releaseZombieFromVase(String alias, int row, int col) {
        zombieEngine.spawnZombie(alias, row, col);
    }

    @Override
    public void plantAt(int row, int col, PlantType plantType) {
        if (map == null || map.getPlantAt(row, col) != null) return;
        Plant plant = PlantFactory.createPlant(plantType, 1);
        map.setPlant(row, col, plant);
        plants.add(plant);
    }

    @Override
    public void onLevelWon() {
        levelWon = true;
        if (AppStatus.currentUser != null) {
            if (AppStatus.currentUser.progressState != null) {
                AppStatus.currentUser.progressState.clearMinigameStage(MinigameEnum.VASEBREAKER);
            }
            UserRegistry.touch(AppStatus.currentUser.profile.getUsername());
        }
        triggerGameOver(true);
    }

    public boolean isLevelWon() {
        return levelWon;
    }

    @Override
    public void kill(Object entity) {
        zombieEngine.kill(entity);
    }

    @Override
    public void takeDamage(Object entity, double amount) {
        zombieEngine.takeDamage(entity, amount);
    }

    @Override
    public Plant getPlantAt(int row, int col) {
        return map != null ? map.getPlantAt(row, col) : null;
    }

    @Override
    public List<Zombie> getZombiesInLane(int lane) {
        return zombieEngine.getZombiesInLane(lane);
    }

    @Override
    public int getSunCount() {
        return 0;
    }

    @Override
    public void addSun(int amount) {
    }

    @Override
    public void spawnProjectile(Projectile p) {
        projectiles.add(p);
    }

    @Override
    public Zombie spawnZombie(String alias, int row, int col) {
        return zombieEngine.spawnZombie(alias, row, col);
    }

    @Override
    public void removePlant(int row, int col) {
        if (map != null) map.removePlant(row, col);
    }

    @Override
    public int getTileColumn(float worldX) {
        return map != null ? map.worldToCol(worldX) : 0;
    }

    public boolean isGameOver() {
        return gameStatus.isGameOver();
    }

    private static int asInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }
}
