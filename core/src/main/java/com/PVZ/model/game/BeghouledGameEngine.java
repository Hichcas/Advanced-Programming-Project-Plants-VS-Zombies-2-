package com.PVZ.model.game;

import com.PVZ.model.entity.LawnMower;
import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.minigame.beghouled.BeghouledGame;
import com.PVZ.model.minigame.beghouled.BeghouledTexturePaths;
import com.PVZ.model.minigame.beghouled.BeghouledUpgrade;
import com.PVZ.view.screen.manager.FontManager;
import com.PVZ.view.HealthBarRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import pvz.skin.PvzSkin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BeghouledGameEngine extends GameEngine implements ZombieEngine {

    private static final double TICK_SECONDS = 0.1;
    private static final float MOVE_CHECK_INTERVAL_SECONDS = 1.0f;
    private static final float GAME_OVER_DISPLAY_DURATION = 3.0f;

    private final List<Plant> plants = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final RegularZombieEngine zombieEngine = new RegularZombieEngine();
    private final BattleController battleController;
    private final Random random = new Random();
    private float tickAccumulator = 0f;

    private BeghouledGame game;
    private Texture background;
    private Texture backgroundRight;
    private Texture pixel;
    private BitmapFont font;
    private BitmapFont tinyFont;

    private LawnMower[] lawnMowers = new LawnMower[0];
    private double zombieSpawnTimer = 0.0;
    private float moveCheckTimer = 0f;

    private boolean gameOverTriggered = false;
    private boolean gameOverNavigated = false;
    private boolean gameOverWin = false;
    private float gameOverTimer = 0f;

    private final List<Rectangle> upgradeButtons = new ArrayList<>();

    public BeghouledGameEngine() {
        super(new GameStatus(), new BeghouledInputProcessor());
        ((BeghouledInputProcessor) inputProcessor).setEngine(this);
        this.battleController = new BattleController(zombieEngine.getZombies(), plants, projectiles, gameStatus);
    }

    public BeghouledGame getGame() {
        return game;
    }

    public void setGame(BeghouledGame game) {
        this.game = game;
    }

    @Override
    public void setMap(Map map) {
        super.setMap(map);
        zombieEngine.bindMap(map);
        battleController.setMap(map);
        initLawnMowers(map);
    }

    public void initializeBoard() {
        if (map == null || game == null) return;
        game.initializeBoard();
        syncBoardToMap();
        zombieSpawnTimer = game.getZombieSpawnIntervalSeconds();
    }

    private void initLawnMowers(Map map) {
        if (map == null || game == null) return;
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
    public LawnMower getLawnMower(int row) {
        if (lawnMowers == null || row < 0 || row >= lawnMowers.length) return null;
        return lawnMowers[row];
    }

    @Override
    public void update(float delta) {
        if (gameOverTriggered) {
            updateGameOverTimer(delta);
            return;
        }
        if (gameStatus.isGameOver() || (game != null && game.isFinished())) return;
        if (game == null || map == null) return;

        battleController.update(delta);
        detectEatenAndMakeCraters();

        tickAccumulator += delta;
        while (tickAccumulator >= TICK_SECONDS) {
            tickAccumulator -= TICK_SECONDS;
            advanceOneTick((float) TICK_SECONDS);
        }

        updateZombieSpawns(delta);
        updateLawnMowers((float) TICK_SECONDS);
        checkWin();

        moveCheckTimer += delta;
        if (moveCheckTimer >= MOVE_CHECK_INTERVAL_SECONDS) {
            moveCheckTimer = 0f;
            if (!game.isFinished() && !game.isAnyMovePossible()) {
                game.resetBoard();
                syncBoardToMap();
            }
        }
    }

    private void advanceOneTick(float delta) {
        if (game == null || map == null) return;
        updatePlants();
        updateProjectiles(delta);
        for (Zombie z : zombieEngine.getZombies()) {
            if (z != null && !z.isDead()) z.updateEffects(delta);
        }
    }

    private void updatePlants() {
        for (int row = 0; row < game.getRows(); row++) {
            for (int col = 0; col < game.getCols(); col++) {
                Plant plant = map.getPlantAt(row, col);
                if (plant == null || plant.isDead()) continue;
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
                game.makeCrater(r, c);
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

    private void updateZombieSpawns(float delta) {
        if (game == null || game.isFinished()) return;
        zombieSpawnTimer -= delta;
        if (zombieSpawnTimer <= 0.0) {
            zombieSpawnTimer = game.getZombieSpawnIntervalSeconds();
            int row = random.nextInt(game.getRows());
            String alias = game.randomZombieAlias();
            zombieEngine.spawnZombie(alias, row, game.getCols() - 1);
        }
    }

    private void detectEatenAndMakeCraters() {
        if (game == null || map == null) return;
        boolean changed = false;
        for (int r = 0; r < game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                if (game.getPlantTypeAt(r, c) == null) continue;
                Plant p = map.getPlantAt(r, c);
                if (p == null || p.isDead()) {
                    game.makeCrater(r, c);
                    changed = true;
                }
            }
        }
        if (changed) syncBoardToMap();
    }

    private void syncBoardToMap() {
        if (game == null || map == null) return;
        for (int r = 0; r < game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                PlantType gridType = game.getPlantTypeAt(r, c);
                Plant existing = map.getPlantAt(r, c);
                if (gridType != null) {
                    if (existing == null || existing.isDead() || existing.getType() != gridType) {
                        if (existing != null) {
                            plants.remove(existing);
                            map.removePlant(r, c);
                        }
                        Plant created = PlantFactory.createPlant(gridType, 1);
                        if (created != null) {
                            map.setPlant(r, c, created);
                            plants.add(created);
                        }
                    }
                } else {
                    if (existing != null) {
                        plants.remove(existing);
                        map.removePlant(r, c);
                    }
                }
            }
        }
    }

    public String trySwap(int r1, int c1, int r2, int c2) {
        if (game == null || map == null) return "No active Beghouled game.";
        String result = game.swap(r1, c1, r2, c2);
        syncBoardToMap();
        checkWin();
        return result;
    }

    public String applyUpgrade(BeghouledUpgrade upgrade) {
        if (game == null || map == null) return "No active Beghouled game.";
        String result = game.tryUpgrade(upgrade);
        syncBoardToMap();
        return result;
    }

    public BeghouledUpgrade getUpgradeAt(float worldX, float worldY) {
        if (game == null) return null;
        List<BeghouledUpgrade> ups = game.getUpgrades();
        for (int i = 0; i < upgradeButtons.size() && i < ups.size(); i++) {
            if (upgradeButtons.get(i).contains(worldX, worldY)) {
                return ups.get(i);
            }
        }
        return null;
    }

    private void updateLawnMowers(float delta) {
        if (game == null) return;
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
            if (mower.isUsed() && gameStatus != null && !gameStatus.isGameOver()) {
                for (Zombie z : getZombiesInLane(mower.getRow())) {
                    if (z != null && !z.isDead() && z.getX() <= mower.getFrontX()) {
                        loseGame();
                        return;
                    }
                }
            }
        }
    }

    private void checkWin() {
        if (game == null || game.isFinished() || gameStatus.isGameOver() || gameOverTriggered) return;
        if (game.getMatchesMade() < game.getTargetMatches()) return;
        for (Zombie z : new ArrayList<>(zombieEngine.getZombies())) {
            zombieEngine.kill(z);
        }
        triggerGameOver(true);
    }

    private void loseGame() {
        triggerGameOver(false);
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
        if (game == null || map == null) return;
        ensureTexturesLoaded();

        zombieEngine.draw(batch);

        batch.begin();
        drawZombiesHealth(batch);
        drawPlantsAndProjectiles(batch);
        drawLawnMowers(batch);
        drawCratersAndSelection(batch);
        batch.end();

        drawHud(batch);
    }

    private void drawZombiesHealth(SpriteBatch batch) {
        for (Zombie z : zombieEngine.getZombies()) {
            if (z == null || z.isDead()) continue;
            HealthBarRenderer.draw(batch, (float) z.getX(), (float) z.getY() + 120 + 2, 100,
                (float) z.getHitpoints() / (float) Math.max(1.0, z.getMaxHitpoints()), false);
        }
    }

    private void drawPlantsAndProjectiles(SpriteBatch batch) {
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
    }

    private void drawLawnMowers(SpriteBatch batch) {
        for (LawnMower mower : lawnMowers) {
            if (mower != null) mower.draw(batch);
        }
    }

    private void drawCratersAndSelection(SpriteBatch batch) {
        if (pixel == null) return;
        float tw = map.getTileWidth();
        float th = map.getTileHeight();
        batch.setColor(0.15f, 0.1f, 0.05f, 0.65f);
        for (int r = 0; r < game.getRows(); r++) {
            for (int c = 0; c < game.getCols(); c++) {
                if (!game.isCrater(r, c)) continue;
                float x = map.getStartX() + c * tw;
                float y = map.getStartY() - (r + 1) * th;
                batch.draw(pixel, x + 6, y + 6, tw - 12, th - 12);
            }
        }
        BeghouledInputProcessor input = (BeghouledInputProcessor) inputProcessor;
        int sr = input.getSelectedRow();
        int sc = input.getSelectedCol();
        if (sr >= 0 && sc >= 0 && game.isPlant(sr, sc)) {
            batch.setColor(1f, 1f, 0.2f, 0.35f);
            float x = map.getStartX() + sc * tw;
            float y = map.getStartY() - (sr + 1) * th;
            batch.draw(pixel, x + 3, y + 3, tw - 6, th - 6);
        }
        batch.setColor(Color.WHITE);
    }

    // پنل مخصوص Beghouled: به‌جای مستطیل‌های رنگی خام، از drawable های همان Skin ای که بقیه‌ی
    // UI بازی (PauseMenuOverlay، WinLoseOverlay و ...) استفاده می‌کنن بهره می‌بریم تا ظاهرش با
    // بقیه‌ی بازی هم‌خوان باشه. اگه به هر دلیلی اسکین لود نشده باشه (مثلاً موقع تست بدون assets)
    // به همون مستطیل رنگی ساده به‌عنوان fallback برمی‌گردیم تا کرش نکنه.
    private static final String PANEL_BG = "image_ui_dialog_asset_inner_bkgd_10";
    private static final String BUTTON_AFFORDABLE = "image_ui_generic_greenbutton_10";
    private static final String BUTTON_LOCKED = "image_ui_generic_brownbutton_10";

    private void drawHud(SpriteBatch batch) {
        ensureTexturesLoaded();
        batch.begin();
        drawStatusPanel(batch);
        drawUpgradeButtons(batch);
        batch.end();
        drawGameOverOverlay(batch);
    }

    private void drawStatusPanel(SpriteBatch batch) {
        float left = map.getStartX() + 10f;
        float top = map.getStartY() + 70f;
        float panelW = 460f;
        float panelH = 56f;

        Drawable panelBg = resolveDrawable(PANEL_BG);
        if (panelBg != null) {
            panelBg.draw(batch, left, top - panelH, panelW, panelH);
        } else if (pixel != null) {
            batch.setColor(0f, 0f, 0f, 0.55f);
            batch.draw(pixel, left, top - panelH, panelW, panelH);
            batch.setColor(Color.WHITE);
        }

        int made = game.getMatchesMade();
        int target = game.getTargetMatches();
        int remaining = Math.max(0, target - made);
        String label = String.format("Sun: %d   |   Matches: %d/%d   |   %d more match%s to win",
            game.getSun(), made, target, remaining, remaining == 1 ? "" : "es");
        font.draw(batch, label, left + 16f, top - panelH / 2f + 12f);
    }

    private void drawUpgradeButtons(SpriteBatch batch) {
        upgradeButtons.clear();
        List<BeghouledUpgrade> ups = game.getUpgrades();
        float btnW = 360f;
        float btnH = 46f;
        float gap = 10f;
        float bx = map.getStartX() + map.getTotalWidth() - btnW - 10f;
        float by = map.getStartY() + 60f;
        for (int i = 0; i < ups.size(); i++) {
            BeghouledUpgrade up = ups.get(i);
            float y = by - i * (btnH + gap);
            Rectangle rect = new Rectangle(bx, y - btnH, btnW, btnH);
            upgradeButtons.add(rect);
            boolean afford = game.getSun() >= up.getCost();

            Drawable buttonBg = resolveDrawable(afford ? BUTTON_AFFORDABLE : BUTTON_LOCKED);
            if (buttonBg != null) {
                buttonBg.draw(batch, rect.x, rect.y, rect.width, rect.height);
            } else if (pixel != null) {
                batch.setColor(afford ? new Color(0.2f, 0.5f, 0.2f, 0.8f) : new Color(0.4f, 0.2f, 0.2f, 0.8f));
                batch.draw(pixel, rect.x, rect.y, rect.width, rect.height);
                batch.setColor(Color.WHITE);
            }
            tinyFont.draw(batch, up.describe(), rect.x + 14f, rect.y + btnH - 14f);
        }
    }

    private static Drawable resolveDrawable(String name) {
        try {
            Skin skin = PvzSkin.get();
            if (skin != null && skin.has(name, Drawable.class)) {
                return skin.getDrawable(name);
            }
        } catch (Exception ignored) {
            // اسکین لود نشده یا drawable وجود نداره؛ کالر به fallback رنگی برمی‌گرده
        }
        return null;
    }

    private void drawGameOverOverlay(SpriteBatch batch) {
        if (!gameOverTriggered) return;
        ensureTexturesLoaded();
        float alpha;
        if (gameOverTimer < 1.0f) alpha = Math.max(0f, gameOverTimer);
        else if (gameOverTimer > 2.5f) alpha = Math.max(0f, 1.0f - (gameOverTimer - 2.5f) / 0.5f);
        else alpha = 1.0f;
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
        // اگه هنوز فایل background.png مخصوص Beghouled رو زیر assets/Minigames/Beghouled/
        // نذاشتی، به‌جای کرش کردن یه پس‌زمینه‌ی خالی fallback می‌گیریم تا بازی بالا بیاد؛
        // همین که فایل رو اضافه کنی خودش پیک می‌شه.
        if (com.badlogic.gdx.Gdx.files.internal(BeghouledTexturePaths.BACKGROUND_LEFT).exists()) {
            background = new Texture(BeghouledTexturePaths.BACKGROUND_LEFT);
        } else {
            System.out.println("[Beghouled] background not found: " + BeghouledTexturePaths.BACKGROUND_LEFT
                + " (falling back to no background)");
            background = null;
        }
        if (com.badlogic.gdx.Gdx.files.internal(BeghouledTexturePaths.BACKGROUND_RIGHT).exists()) {
            backgroundRight = new Texture(BeghouledTexturePaths.BACKGROUND_RIGHT);
        } else {
            System.out.println("[Beghouled] background_right not found: " + BeghouledTexturePaths.BACKGROUND_RIGHT
                + " (falling back to no background)");
            backgroundRight = null;
        }
        font = FontManager.getInstance().getEnglishMenuFont();
        tinyFont = FontManager.getInstance().getEnglishTinyFont();
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        pixel = new Texture(pm);
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
        if (background != null) background.dispose();
        if (pixel != null) pixel.dispose();
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
        return game != null ? game.getSun() : 0;
    }

    @Override
    public void addSun(int amount) {
        if (game != null) game.addSun(amount);
    }

    @Override
    public void spawnProjectile(Projectile p) {
        projectiles.add(p);
    }

    @Override
    public Zombie spawnZombie(String alias,
                              int row, int col) {
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
}
