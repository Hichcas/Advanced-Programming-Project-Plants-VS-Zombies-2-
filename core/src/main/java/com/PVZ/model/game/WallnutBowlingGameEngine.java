package com.PVZ.model.game;

import com.PVZ.model.entity.LawnMower;
import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.minigame.wallnutbowling.BowlingNut;
import com.PVZ.model.minigame.wallnutbowling.NutType;
import com.PVZ.model.minigame.wallnutbowling.WallnutBowlingGame;
import com.PVZ.model.minigame.wallnutbowling.WallnutBowlingTexturePaths;
import com.PVZ.model.enums.MinigameEnum;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.screen.manager.FontManager;
import com.PVZ.view.HealthBarRenderer;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class WallnutBowlingGameEngine extends GameEngine implements ZombieEngine {

    private static final double TICK_SECONDS = 0.1;
    private static final float NUT_HIT_RADIUS = 34f;
    private static final float GAME_OVER_DISPLAY_DURATION = 3.0f;
//mishe avazesh kard vali bara in phase ino gozashtam va ok has das nazanim ham.
    private final List<Plant> plants = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final RegularZombieEngine zombieEngine = new RegularZombieEngine();
    private final BattleController battleController;
    private final Random random = new Random();
    private LawnMower[] lawnMowers;
    private float tickAccumulator = 0f;

    private WallnutBowlingGame game;
    private Texture nutNormalTex;
    private Texture nutExplosiveTex;
    private Texture nutGiantTex;
    private Texture background;
    private BitmapFont font;

    private boolean gameOverTriggered = false;
    private boolean gameOverNavigated = false;
    private boolean gameOverWin = false;
    private float gameOverTimer = 0f;

    public WallnutBowlingGameEngine() {
        super(new GameStatus(), new WallnutBowlingInputProcessor());
        ((WallnutBowlingInputProcessor) inputProcessor).setEngine(this);
        this.battleController = new BattleController(zombieEngine.getZombies(), plants, projectiles, gameStatus);
    }

    public WallnutBowlingGame getGame() {
        return game;
    }

    public void setGame(WallnutBowlingGame game) {
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

    public boolean launchHeldNut(int row, int col) {
        if (game == null || map == null) return false;
        if (!game.canLaunchAt(row, col)) return false;
        NutType type = game.consumeHeldNut();
        if (type == null) return false;

        com.PVZ.model.entity.Tile tile = map.getTile(row, col);
        if (tile == null) return false;
        double startX = tile.getX() + tile.getWidth() / 2.0;
        double startY = tile.getY() + tile.getHeight() / 2.0;
        double speed = game.getNutSpeed();
        game.addNut(new BowlingNut(type, startX, startY, speed, 0));
        return true;
    }

    @Override
    public void update(float delta) {
        if (gameOverTriggered) {
            updateGameOverTimer(delta);
            return;
        }
        if (gameStatus.isGameOver()) return;
        battleController.update(delta);
        tickAccumulator += delta;
        while (tickAccumulator >= TICK_SECONDS) {
            tickAccumulator -= TICK_SECONDS;
            advanceOneTick((float) TICK_SECONDS);
        }
        checkWinLoss();
    }

    private void advanceOneTick(float delta) {
        if (gameStatus.isGameOver()) return;
        updateLawnMowers(delta);
        if (game != null) {
            updateWaves(delta);
            updateNuts(delta);
            game.tickCooldown(delta);
        }
        for (Zombie z : zombieEngine.getZombies()) {
            if (!z.isDead()) z.updateEffects(delta);
        }
    }

    private void updateWaves(float delta) {
        int burst = game.tickWave(delta);
        if (burst <= 0) return;
        for (int row : game.nextWaveRows(burst)) {
            String alias = game.randomZombieAlias();
            zombieEngine.spawnZombie(alias, row, game.getCols() - 1);
        }
    }

    private void updateNuts(float delta) {
        if (map == null) return;
        Iterator<BowlingNut> it = game.getNuts().iterator();
        while (it.hasNext()) {
            BowlingNut nut = it.next();
            if (!nut.isAlive()) {
                it.remove();
                continue;
            }
            nut.move(delta);

            int row = map.worldToRow((float) nut.getY());
            if (row < 0 || row >= game.getRows()) {
                if (nut.getType() == NutType.GIANT) {
                    it.remove();
                    continue;
                }
                double clampedY = row < 0
                        ? map.getStartY() - map.getTileHeight() * 0.05
                        : map.getStartY() - map.getTileHeight() * (game.getRows() - 0.05);
                nut.setY(clampedY);
                applyTurn(nut);
            }

            if (nut.getX() < map.getStartX() - 60 || nut.getX() > map.getStartX() + map.getTileWidth() * game.getCols(
                    ) + 80) {
                it.remove();
                continue;
            }

            Zombie hit = findCollidingZombie(nut);
            if (hit != null) {
                handleHit(nut, hit);
                if (!nut.isAlive()) {
                    it.remove();
                }
            }
        }
    }

    private Zombie findCollidingZombie(BowlingNut nut) {
        Rectangle nutRect = new Rectangle((float) nut.getX() - NUT_HIT_RADIUS / 2f,
                (float) nut.getY() - NUT_HIT_RADIUS / 2f, NUT_HIT_RADIUS, NUT_HIT_RADIUS);
        for (Zombie z : zombieEngine.getZombies()) {
            if (z == null || z.isDead()) continue;
            if (nutRect.overlaps(z.getHitbox())) return z;
        }
        return null;
    }

    private void handleHit(BowlingNut nut, Zombie zombie) {
        switch (nut.getType()) {
            case NORMAL -> {
                zombie.takeDamage(game.getNutDamage());
                applyTurn(nut);
            }
            case EXPLOSIVE -> {
                explode(nut);
                nut.kill();
            }
            case GIANT -> zombieEngine.kill(zombie);
        }
    }

    private void explode(BowlingNut nut) {
        if (map == null) return;
        int centerRow = map.worldToRow((float) nut.getY());
        int centerCol = map.worldToCol((float) nut.getX());
        for (int r = centerRow - 1; r <= centerRow + 1; r++) {
            if (r < 0 || r >= game.getRows()) continue;
            for (Zombie z : zombieEngine.getZombiesInLane(r)) {
                int zCol = map.worldToCol((float) z.getX());
                if (Math.abs(zCol - centerCol) <= 1) {
                    z.takeDamage(game.getExplosionDamage());
                }
            }
        }
    }

    private void applyTurn(BowlingNut nut) {
        double angle = nut.getHits() == 0 ? 45 : 90;
        nut.registerHit();
        double sign = random.nextBoolean() ? 1 : -1;
        nut.rotate(sign * angle);
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

    private void checkWinLoss() {
        if (game == null || game.isFinished() || gameOverTriggered) return;
        if (!game.allZombiesSpawned()) return;
        if (!game.getNuts().isEmpty()) return;
        for (Zombie z : zombieEngine.getZombies()) {
            if (z != null && !z.isDead()) return;
        }
        if (AppStatus.currentUser != null) {
            if (AppStatus.currentUser.progressState != null) {
                AppStatus.currentUser.progressState.clearMinigameStage(MinigameEnum.WALLNUT_BOWLING);
            }
            UserRegistry.touch(AppStatus.currentUser.profile.getUsername());
        }
        triggerGameOver(true);
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
            AppStatus.returnToTravelLog();
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
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
        drawNuts(batch);
    }

    private void drawNuts(SpriteBatch batch) {
        if (game == null || map == null) return;
        ensureTexturesLoaded();

        batch.begin();
        for (BowlingNut nut : game.getNuts()) {
            Texture texture = switch (nut.getType()) {
                case NORMAL -> nutNormalTex;
                case EXPLOSIVE -> nutExplosiveTex;
                case GIANT -> nutGiantTex;
            };
            float size = map.getTileWidth() * 0.5f;
            batch.draw(texture, (float) nut.getX() - size / 2f, (float) nut.getY() - size / 2f, size, size);
        }

        if (!gameOverTriggered) {
            NutType held = game.getHeldNut();
            String cooldown = game.getCooldownRemaining() > 0
                    ? String.format(" | reload: %.1fs", game.getCooldownRemaining())
                    : "";
            String label = "Next nut: " + (held != null ? held.name() : "-")
                    + "  (" + game.getZombiesSpawned() + "/" + game.getTotalZombies() + " zombies)" + cooldown;
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
        if (nutNormalTex != null) return;
        nutNormalTex = new Texture(WallnutBowlingTexturePaths.NUT_NORMAL);
        nutExplosiveTex = new Texture(WallnutBowlingTexturePaths.NUT_EXPLOSIVE);
        nutGiantTex = new Texture(WallnutBowlingTexturePaths.NUT_GIANT);
        background = new Texture(WallnutBowlingTexturePaths.BACKGROUND);
        font = FontManager.getInstance().getEnglishMenuFont();
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
        if (nutNormalTex != null) nutNormalTex.dispose();
        if (nutExplosiveTex != null) nutExplosiveTex.dispose();
        if (nutGiantTex != null) nutGiantTex.dispose();
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
    public int getSunCount() { return 0; }

    @Override
    public void addSun(int amount) { }

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
