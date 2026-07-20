package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Sun;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantTag;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.screen.manager.FontManager;
import com.PVZ.view.HealthBarRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
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
    private float tickAccumulator = 0f;
    private boolean zombieWavesStarted = false;
    private PlantType selectedPlantType;
    private final java.util.Map<PlantType, Double> rechargeRemaining = new java.util.EnumMap<>(PlantType.class);

    private final SeedPacketBar seedPacketBar = new SeedPacketBar();

    private final RegularZombieEngine zombieEngine;
    private final List<Plant> plants = new ArrayList<>();
    private WaveManager waveManager;
    private BattleController battleController;

    /** Per-stage background texture path (from StageConfig.mapTexture); null = use default Frontyard. */
    private String backgroundTexturePath;
    private com.badlogic.gdx.graphics.Texture backgroundOverrideTexture;

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

    @Override
    public void setMap(Map map) {
        super.setMap(map);
        if (zombieEngine != null) {
            zombieEngine.bindMap(map);
        }
        if (battleController != null) {
            battleController.setMap(map);
        }
        initLawnMowers(map);
    }

    private final com.PVZ.model.entity.LawnMower[] lawnMowers = new com.PVZ.model.entity.LawnMower[ROWS];

    private void initLawnMowers(Map map) {
        if (map == null) {
            return;
        }
        float tileWidth = map.getTileWidth();
        float tileHeight = map.getTileHeight();
        float startX = map.getStartX();
        float startY = map.getStartY();
        double triggerX = startX - tileWidth * 0.75;
        double travelLimitX = 2560 + tileWidth;
        for (int row = 0; row < ROWS; row++) {
            com.PVZ.model.entity.LawnMower mower = new com.PVZ.model.entity.LawnMower();
            double parkY = startY - (row + 1) * tileHeight + tileHeight * 0.15;
            mower.init(row, parkY, triggerX, travelLimitX);
            lawnMowers[row] = mower;
        }
    }

    private void updateLawnMowers(float delta) {
        for (com.PVZ.model.entity.LawnMower mower : lawnMowers) {
            if (mower == null) {
                continue;
            }

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
                        System.out.println("The zombie ate your brain; LOSER!!!");
                        if (battleController != null) {
                            battleController.triggerGameOver();
                        }
                        resetBoardAfterGameOver();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void update(float delta) {
        if (gameStatus != null && gameStatus.isGameOver()) {
            return;
        }
        if (waveManager != null) {
            waveManager.update(delta, zombieEngine);
        }
        if (battleController != null) {
            battleController.update(delta);
        }

        tickAccumulator += delta;
        while (tickAccumulator >= TICK_SECONDS) {
            tickAccumulator -= TICK_SECONDS;
            advanceOneTick();
        }

        // win condition: all waves finished and all zombies dead
        if (gameStatus != null && !gameStatus.isGameOver() && !gameStatus.isWon()
                && waveManager != null && waveManager.isFinished()) {
            boolean anyAlive = false;
            for (Zombie z : getZombieList()) {
                if (z != null && !z.isDead()) {
                    anyAlive = true;
                    break;
                }
            }
            if (!anyAlive) {
                System.out.println("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                gameStatus.setWon(true);
                gameStatus.setGameOver(true);
                resetBoardAfterGameOver();
                AppStatus.returnToMainMenu();
            }
        }
    }

    public void advanceTicks(int ticks) {
        int safeTicks = Math.max(0, ticks);
        for (int i = 0; i < safeTicks; i++) {
            update((float) TICK_SECONDS);
        }
    }

    private void advanceOneTick() {
        if (gameStatus != null && gameStatus.isGameOver()) {
            return;
        }
        updatePlants();
        updateProjectiles((float) TICK_SECONDS);
        updateSunManager((float) TICK_SECONDS);
        updateSkySun((float) TICK_SECONDS);
        updateLawnMowers((float) TICK_SECONDS);

        for (Zombie z : getZombieList()) {
            if (z != null && !z.isDead()) {
                z.updateEffects((float) TICK_SECONDS);
            }
        }

        rechargeRemaining.replaceAll((type, remaining) -> Math.max(0.0, remaining - TICK_SECONDS));

        if (gameStatus != null) {
            gameStatus.setRemainingZombieWaveInPercent(
                zombieWavesStarted ? Math.min(100, gameStatus.getRemainingZombieWaveInPercent() + 1) : 0);
        }

        if (AppStatus.currentChapter != null) {
            AppStatus.currentChapter.update(map, this);
        }
    }


    private void resetBoardAfterGameOver() {
        if (map != null) {
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    map.removePlant(row, col);
                }
            }
        }
        projectiles.clear();
        zombies.clear();
        if (zombieEngine != null) {
            zombieEngine.getZombies().clear();
        }
        initLawnMowers(map);
        zombieWavesStarted = false;
        tickAccumulator = 0f;
        selectedPlantType = null;
        rechargeRemaining.clear();
        sunManager.clear();
        plantFoodManager.reset();
        if (gameStatus != null) {
            gameStatus.setRemainingZombieWaveInPercent(0);
        }
    }

    protected void updatePlants() {
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

                Object freezeLv = plant.getRuntimeState("freezeLevel");
                boolean isPlantFrozen = freezeLv instanceof Number && ((Number) freezeLv).intValue() >= 3;
                if (!isPlantFrozen) {
                    plant.update(this, TICK_SECONDS);
                }

                if (plant.isDead()) {
                    if (plant.getStats().getBooleanExtra("explodeOnDeath", false)) {
                        damageArea(row, row,
                            Math.max(plant.getStats().getExplodeDamage(), plant.getStats().getDamage()));
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

    private static final double SKY_SUN_INTERVAL_SECONDS = 10.0;
    private double skySunTimer = 0.0;


    private void updateSkySun(float delta) {
        if (map == null || !zombieWavesStarted) {
            return;
        }
        if (gameStatus != null && gameStatus.isNoSkySun()) {
            return;
        }
        skySunTimer += delta;
        if (skySunTimer < SKY_SUN_INTERVAL_SECONDS) {
            return;
        }
        skySunTimer = 0.0;

        int row = random.nextInt(ROWS);
        int col = random.nextInt(COLS);
        Tile tile = map.getTile(row, col);
        if (tile == null) {
            return;
        }

        double landingX = tile.getX() + tile.getWidth() / 2.0;
        double groundY = tile.getY() + tile.getHeight() / 2.0;
        double startY = map.getStartY() + map.getTileHeight() * 2.0;

        sunManager.spawnFalling(landingX, startY, 25, groundY);
    }

    private void decrementTimers(java.util.Map<Zombie, Integer> timers, boolean restoreMovement) {
        List<Zombie> toRestore = new ArrayList<>();
        for (java.util.Map.Entry<Zombie, Integer> entry : timers.entrySet()) {
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
        if (zombieEngine != null) {
            zombieEngine.draw(batch);
        }
        batch.begin();
        if (battleController != null) {
            battleController.drawProjectiles(batch);
        }

        for (Projectile projectile : projectiles) {
            projectile.draw(batch);
        }

        for (com.PVZ.model.entity.Sun sun : sunManager.getSuns()) {
            sun.draw(batch);
        }

        for (com.PVZ.model.entity.LawnMower mower : lawnMowers) {
            if (mower != null) {
                mower.draw(batch);
            }
        }
        BitmapFont plantFont = FontManager.getInstance().getEnglishTinyFont();
        plantFont.setColor(Color.WHITE);
        if (map != null) {
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    Plant plant = map.getPlantAt(row, col);
                    if (plant == null || plant.isDead()) {
                        continue;
                    }
                    plant.draw(batch);
                    Rectangle box = plant.getHitbox();
                    HealthBarRenderer.draw(batch, box.x, box.y + box.height + 2, box.width,
                        (float) plant.getCurrentHp() / Math.max(1, plant.getMaxHp()), true);
                    String label = plant.getType() + " (" + plant.getCurrentHp() + "hp)";
                    plantFont.draw(batch, label, box.x, box.y + box.height + 4);

                    Object pFreezeLv = plant.getRuntimeState("freezeLevel");
                    if (pFreezeLv instanceof Number && ((Number) pFreezeLv).intValue() >= 3) {
                        Color c = batch.getColor();
                        batch.setColor(0.3f, 0.6f, 1f, 0.45f);
                        batch.draw(iceOverlayTexture(), box.x, box.y, box.width, box.height);
                        batch.setColor(c);
                    }
                }
            }
        }

        BitmapFont font = FontManager.getInstance().getEnglishTinyFont();
        font.setColor(Color.BLACK);
        for (Zombie z : getZombieList()) {
            if (z != null && !z.isDead()) {
                HealthBarRenderer.draw(batch, (float) z.getX(), (float) z.getY() + 120 + 2, 100,
                    (float) z.getHitpoints() / (float) Math.max(1.0, z.getMaxHitpoints()), false);

                if (z.isFrozen()) {
                    Color c = batch.getColor();
                    batch.setColor(0.3f, 0.6f, 1f, 0.45f);
                    batch.draw(iceOverlayTexture(), (float) z.getX(), (float) z.getY(), 100, 120);
                    batch.setColor(c);
                }
            }
        }
        font.setColor(Color.WHITE);

        batch.end();
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

        if (zombieEngine != null) {
            zombieEngine.dispose();
        }
    }

    @Override
    public List<Zombie> getZombiesInLane(int lane) {
        if (lane < 0) {
            return List.of();
        }
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : getZombieList()) {
            if (zombie != null && (int) zombie.getRow() == lane && !zombie.isDead()) {
                result.add(zombie);
            }
        }
        return result;
    }

    @Override
    public List<Zombie> getAllZombies() {
        return Collections.unmodifiableList(getZombieList());
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
    public List<Plant> getAllPlants() {
        if (map == null) {
            return List.of();
        }
        List<Plant> result = new ArrayList<>();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Plant plant = map.getPlantAt(row, col);
                if (plant != null && !plant.isDead()) {
                    result.add(plant);
                }
            }
        }
        return result;
    }

    @Override
    public void spawnProjectile(Object projectile) {
        if (projectile instanceof Projectile p) {
            placeProjectileOnMap(p);
            projectiles.add(p);
        }
    }


    private void placeProjectileOnMap(Projectile p) {
        if (p.isWorldPositioned()) {
            return;
        }
        int row = p.getRow();
        float tileWidth = 177f;
        float tileHeight = 234f;
        float startX = 550f;
        float startY = 1240f;
        if (map != null) {
            tileWidth = map.getTileWidth();
            tileHeight = map.getTileHeight();
            startX = map.getStartX();
            startY = map.getStartY();
        }

        int col = 0;
        Object colState = p.getExtra("originCol");
        if (colState instanceof Number number) {
            col = number.intValue();
        }

        float worldX = startX + col * tileWidth + tileWidth * 0.5f;
        float worldY = startY - (row + 1) * tileHeight + tileHeight * 0.35f;
        float speedPxPerSec = tileWidth * 1.5f;
        if (p.getType() == com.PVZ.model.entity.plants.behavior.impl.ProjectileType.LOB) {
            speedPxPerSec = tileWidth * 0.9f;
        }
        float speedMultiplier = (float) Math.max(0.1, Math.abs(p.getSpeed()));
        speedPxPerSec *= speedMultiplier;

        double horizontalSign = p.getSpeed() < 0 ? -1.0 : 1.0;
        double verticalSpeed = 0.0;
        Object targetLaneState = p.getExtra("targetLane");
        if (targetLaneState instanceof Number number) {
            int targetLane = number.intValue();
            if (targetLane != row) {
                verticalSpeed = Math.signum(targetLane - row) * speedPxPerSec;
            }
        }

        p.initWorldPosition(worldX, worldY, (float) (horizontalSign * speedPxPerSec), (float) verticalSpeed);
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
        for (Zombie zombie : getZombiesInLane(lane)) {
            if (zombie != null) {
                zombie.freeze((float) seconds);
            }
        }
    }

    @Override
    public void freezeAllZombies(double seconds) {
        for (Zombie zombie : getZombieList()) {
            if (zombie != null && !zombie.isDead()) {
                zombie.freeze((float) seconds);
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
        for (Zombie zombie : getZombieList()) {
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
        for (Zombie zombie : getZombiesInLane(lane)) {
            if (zombie != null) {
                zombie.hypnotize((float) seconds);
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
        if (p != null) {
            placeProjectileOnMap(p);
            projectiles.add(p);
        }
    }

    @Override
    public Zombie spawnZombie(String alias, int row, int x) {
        if (zombieEngine != null) {
            return zombieEngine.spawnZombie(alias, row, x);
        }
        return null;
    }



    @Override
    public void removePlant(int row, int col) {
        if (map == null) {
            return;
        }
        map.removePlant(row, col);
    }

    @Override
    public int getTileColumn(float worldX) {
        return 0;
    }

    public void setBackgroundTexturePath(String path) {
        this.backgroundTexturePath = path;
    }

    @Override
    public com.badlogic.gdx.graphics.Texture getBackgroundOverride() {
        if (backgroundTexturePath == null || backgroundTexturePath.isEmpty()) {
            return null;
        }
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

    public String plantPlant(String plantType, int x, int y) {
        PlantType type;
        try {
            type = PlantType.fromName(plantType);
        } catch (Exception ex) {
            return "Unknown plant type: " + plantType;
        }
        return plantPlant(type, x, y);
    }

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

        TileType targetTileType = map.getTile(row, col).getType();
        boolean plantIsAquatic = plant.getDefinition() != null
                && plant.getDefinition().hasTag(PlantTag.WATER);
        if ((targetTileType == TileType.WATER || targetTileType == TileType.TIDE) && !plantIsAquatic) {
            return "Non-aquatic plants cannot be planted on water tiles.";
        }
        if (plantIsAquatic && targetTileType != TileType.WATER && targetTileType != TileType.TIDE) {
            return "Aquatic plants must be planted on water tiles.";
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

        if (AppStatus.boostedPlants.contains(type)) {
            plant.applyPlantFood(this);
        }

        return "Planted " + type.getDisplayName() + " at (" + col+ ", " + row + ").";
    }

    public boolean isOnCooldown(PlantType type) {
        return rechargeRemaining.getOrDefault(type, 0.0) > 0.0;
    }

    public double getRechargeRemainingSeconds(PlantType type) {
        return rechargeRemaining.getOrDefault(type, 0.0);
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
        return "Plant plucked from (" + col + ", " + row + ")";
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
        return 	"Plant fed at (" + col + ", " + row + ")";
    }

    public int collectSunAtWorldPoint(float worldX, float worldY) {
        Rectangle pointer = new Rectangle(worldX - 8f, worldY - 8f, 16f, 16f);
        int collected = sunManager.collectAt(pointer);
        if (collected > 0) {
            addSun(collected);
        }
        return collected;
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

    private static String tileGlyph(TileType type) {
        if (type == null) return ".";
        switch (type) {
            case TOMBSTONE: return "T";
            case WATER: return "~";
            case TIDE: return "^";
            case ICE: return "*";
            case SLIPPERY_UP: return "U";
            case SLIPPERY_DOWN: return "D";
            case NECROMANCY: return "N";
            case LOW_COAST: return "L";
            case CRATER: return "C";
            default: return ".";
        }
    }

    public String showMapText() {
        StringBuilder builder = new StringBuilder();
        builder.append("Sun: ").append(getSunCount())
            .append(" | Wave: ").append(waveManager == null ? 0 : waveManager.getCurrentWave())
            .append('\n');

        boolean[][] zombieAt = new boolean[ROWS][COLS];
        for (Zombie z : getZombieList()) {
            if (z == null || z.isDead()) {
                continue;
            }
            int row = (int) Math.round(z.getRow());
            int col = map != null ? map.worldToCol((float) z.getX()) : -1;
            if (row >= 0 && row < ROWS && col >= 0 && col < COLS) {
                zombieAt[row][col] = true;
            }
        }

        builder.append("Map:\n");
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Plant plant = map == null ? null : map.getPlantAt(row, col);
                char cell;
                if (zombieAt[row][col] && plant != null) {
                    cell = '#';
                } else if (zombieAt[row][col]) {
                    cell = 'Z';
                } else if (plant != null) {
                    cell = plant.getType().name().charAt(0);
                } else if (map != null) {
                    Tile tile = map.getTile(row, col);
                    cell = tile == null ? '.' : tileGlyph(tile.getType()).charAt(0);
                } else {
                    cell = '.';
                }
                builder.append(cell);
                if (col + 1 < COLS) {
                    builder.append(' ');
                }
            }
            builder.append('\n');
        }

        // Debug: full-name listing of every special (non-NORMAL) tile + coordinates.
        builder.append("Tile debug:\n");
        boolean any = false;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (map == null) break;
                Tile tile = map.getTile(row, col);
                if (tile == null || tile.getType() == TileType.NORMAL) {
                    continue;
                }
                any = true;
                builder.append("  (").append(col).append(", ").append(row).append(") = ")
                        .append(tile.getType().name());
                if (tile.getType() == TileType.TOMBSTONE) {
                    builder.append(" hp=").append(tile.getHp());
                }
                builder.append('\n');
            }
        }
        if (!any) {
            builder.append("  (none)\n");
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
                        .append(col)
                        .append(", ")
                        .append(row)
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
            return "Tile (" + col + ", " + row + ") is empty.";
        }
        return "Tile (" + col + ", " + row + ") contains " + plant.getType().getDisplayName()
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
        skySunTimer = 0.0;
        if (waveManager != null) {
            waveManager.start();
        }
        return "Zombie waves started.";
    }

    public String zombiesInfoText() {
        StringBuilder sb = new StringBuilder();
        for (Zombie zombie : getZombieList()) {
            if (zombie != null && !zombie.isDead()) {
                sb.append(zombie.getStatusString()).append('\n');
            }
        }
        return sb.length() == 0 ? "No zombies." : sb.toString().trim();
    }

    public String currentMenuText() {
        return AppStatus.currentMenuType == null ? "" : AppStatus.currentMenuType.name();
    }

    public String advanceTimeText(int ticks) {
        advanceTicks(ticks);
        return "Advanced time by " + ticks + " ticks.";
    }

    public RegularZombieEngine getZombieEngine() {
        return zombieEngine;
    }

    public SunManager getSunManager() {
        return sunManager;
    }

    public BattleController getBattleController() {
        return battleController;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public void startWaves() {
        zombieWavesStarted = true;
        skySunTimer = 0.0;
        if (waveManager != null) {
            waveManager.start();
        }
    }

    private List<Zombie> getZombieList() {
        if (zombieEngine != null && zombieEngine.getZombies() != null) {
            return zombieEngine.getZombies();
        }
        return zombies;
    }

    private int normalizeIndex(int value) {
        return value;
    }

    private com.badlogic.gdx.graphics.Texture iceOverlayTex;

    private com.badlogic.gdx.graphics.Texture iceOverlayTexture() {
        if (iceOverlayTex == null) {
            com.badlogic.gdx.graphics.Pixmap pm = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pm.setColor(0.4f, 0.7f, 1f, 1f);
            pm.fill();
            iceOverlayTex = new com.badlogic.gdx.graphics.Texture(pm);
            pm.dispose();
        }
        return iceOverlayTex;
    }

}
