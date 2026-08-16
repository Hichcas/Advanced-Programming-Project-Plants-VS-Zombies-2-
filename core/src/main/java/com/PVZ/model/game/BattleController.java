package com.PVZ.model.game;

import com.PVZ.model.entity.LawnMower;
import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.plants.behavior.impl.ProjectileType;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.entity.zombies.types.ranged_caster.ZombieDarkJuggler;
import com.PVZ.model.enums.DamageType;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.status.AppStatus;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Iterator;
import java.util.List;

public class BattleController implements BehaviorContext {

    private final List<Zombie> zombies;
    private final List<Plant> plants;
    private final List<Projectile> projectiles;
    private final List<ZombieProjectile> zombieProjectiles = new java.util.ArrayList<>();
    private final GameStatus gameStatus;
    private Map map;
    private PlantFoodManager plantFoodManager;
    private LootManager lootManager;
    private final java.util.Random lootRandom = new java.util.Random();
    public final java.util.Set<Zombie> questNotifiedZombies = new java.util.HashSet<>();

    public BattleController(List<Zombie> zombies, List<Plant> plants,
                            List<Projectile> projectiles, GameStatus gameStatus) {
        this.zombies = zombies;
        this.plants = plants;
        this.projectiles = projectiles;
        this.gameStatus = gameStatus;
    }

    public void setPlantFoodManager(PlantFoodManager plantFoodManager) {
        this.plantFoodManager = plantFoodManager;
    }

    public void setLootManager(LootManager lootManager) {
        this.lootManager = lootManager;
    }

    public void clearQuestNotifiedZombies() {
        questNotifiedZombies.clear();
    }

    public void notifyZombieKilled(RegularGameEngine engine, Zombie z, com.PVZ.model.enums.PlantType killerPlant) {
        if (questNotifiedZombies.contains(z)) return;
        questNotifiedZombies.add(z);

        if (engine != null && engine.map != null) {
            int col = engine.map.worldToCol((float) z.getX());
            int row = (int) z.getRow();
            if (col == 0 && row >= 0 && row < engine.lawnMowers.length
                && engine.lawnMowers[row] != null && engine.lawnMowers[row].isUsed()) {
                engine.questLawnlessCol1Kills++;
            }
        }

        if (AppStatus.currentUser != null && AppStatus.currentUser.questState != null) {
            AppStatus.currentUser.questState.getQuestManager().onZombieKilled(killerPlant);
        }
    }

    /**
     * Spawns a real Plant Food pickup at the glowing zombie's death position.
     * The food is added to the inventory only when the player collects the pickup.
     */
    public void grantPlantFoodDrop(double x, double y) {
        if (lootManager == null) {
            if (plantFoodManager != null) {
                plantFoodManager.addPlantFood(1);
            }
            return;
        }
        lootManager.spawn(x, y, com.PVZ.model.entity.LootDrop.LootType.PLANT_FOOD);
        System.out.println("A glowing zombie dropped Plant Food at ("
            + String.format("%.1f", x) + ", " + String.format("%.1f", y) + ").");
    }

    public void rollLootDrop(double x, double y) {
        if (lootManager == null || lootRandom.nextDouble() >= 0.10) {
            return;
        }
        com.PVZ.model.entity.LootDrop.LootType[] types = com.PVZ.model.entity.LootDrop.LootType.values();
        com.PVZ.model.entity.LootDrop.LootType type = types[lootRandom.nextInt(types.length)];
        lootManager.spawn(x, y, type);
    }

    public String applyLootReward(com.PVZ.model.entity.LootDrop drop) {
        if (drop == null || AppStatus.currentUser == null || AppStatus.currentUser.userStats == null) {
            return "";
        }
        switch (drop.getType()) {
            case DIAMOND -> {
                AppStatus.currentUser.userStats.addDiamonds(drop.getType().getAmount());
                String msg = "A zombie dropped a diamond; you have "
                    + AppStatus.currentUser.userStats.getDiamonds() + " diamonds now.";
                System.out.println(msg);
                return msg;
            }
            case COIN -> {
                AppStatus.currentUser.userStats.addCoins(drop.getType().getAmount());
                String msg = "A zombie dropped a coin; you have "
                    + AppStatus.currentUser.userStats.getCoins() + " coins now.";
                System.out.println(msg);
                return msg;
            }
            case POT -> {
                if (AppStatus.currentUser.greenhouseState != null) {
                    AppStatus.currentUser.greenhouseState.unlockPots(drop.getType().getAmount());
                }
                String msg = "A zombie dropped a pot; you have a new greenhouse slot now.";
                System.out.println(msg);
                return msg;
            }
            case PLANT_FOOD -> {
                if (plantFoodManager == null) {
                    return "Plant Food manager is not available.";
                }
                plantFoodManager.addPlantFood(drop.getType().getAmount());
                String msg = "Collected Plant Food! You now have "
                    + plantFoodManager.getPlantFoodCount() + ".";
                System.out.println(msg);
                return msg;
            }
            default -> {
                return "";
            }
        }
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public Map getMap() {
        return map;
    }

    public void update(float delta) {
        updateZombies(delta);
        updateZombieProjectiles(delta);
        updatePlantProjectiles(delta);
        removeDeadPlants();
        removeExpiredGrapes();
    }

    private void removeExpiredGrapes() {
        java.util.Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            if (p.isFuseExploded()) {
                if (map != null) {
                    int gRow = map.worldToRow((float) p.getPositionY());
                    int gCol = map.worldToCol((float) p.getPositionX());
                    if (gRow >= 0 && gCol >= 0) {
                        for (Zombie z : getZombiesInLane(gRow)) {
                            double colDist = Math.abs(z.getX() - p.getPositionX());
                            if (colDist < map.getTileWidth() * 1.2) {
                                z.takeDamage(p.getDamage());
                            }
                        }
                    }
                }
                p.consumeFuseExplosion();
                it.remove();
            }
        }
    }

    private void updateZombies(float delta) {
        for (int i = zombies.size() - 1; i >= 0; i--) {
            zombies.get(i).update(delta, this);
        }
    }

    private void updateZombieProjectiles(float delta) {
        Iterator<ZombieProjectile> zpIt = zombieProjectiles.iterator();
        while (zpIt.hasNext()) {
            ZombieProjectile zp = zpIt.next();
            zp.update(delta);
            if (zp.isDestroyed()) {
                zpIt.remove();
                continue;
            }

            int zpCol = getTileColumn(zp.getX());
            Plant p = getPlantAt(zp.getRow(), zpCol);
            if (p != null && !p.isDead()) {
                p.takeDamage(zp.getDamage());
                zp.getOwner().onProjectileHit(p);
                zp.destroy();
                zpIt.remove();
            }
        }
    }

    private void updatePlantProjectiles(float delta) {
        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();

            if (p.isHoming() && p.isFreeMotion()) {
                steerHoming(p);
            }

            if (handleTileCollision(p)) {
                projIt.remove();
                continue;
            }

            if (handleZombieCollision(p)) {
                projIt.remove();
            }
        }
    }

    private boolean handleTileCollision(Projectile p) {
        if (p.getType() == ProjectileType.LOB || map == null) {
            return false;
        }

        int pRow = map.worldToRow((float) p.getPositionY());
        int pCol = map.worldToCol((float) p.getPositionX());
        if (!map.isWithinBounds(pRow, pCol)) {
            return false;
        }

        Tile tile = map.getTile(pRow, pCol);
        if (tile == null) {
            return false;
        }

        if (tile.getOctopusHp() > 0) {
            int dmg = Math.max(1, (int) p.getDamage());
            int newHp = tile.getOctopusHp() - dmg;
            if (newHp <= 0) {
                tile.setOctopusHp(0);
                Plant octoPlant = tile.getPlant();
                if (octoPlant != null) {
                    octoPlant.putRuntimeState("disabledTicks", 0);
                }
                System.out.println("Octopus destroyed on tile (" + pRow + "," + pCol + ") — plant freed!");
            } else {
                tile.setOctopusHp(newHp);
            }
            return true;
        }

        TileType type = tile.getType();
        if (type != TileType.TOMBSTONE && type != TileType.NECROMANCY) {
            return false;
        }

        int newHp = tile.getHp() - (int) p.getDamage();
        if (newHp <= 0) {
            tile.setType(TileType.NORMAL);
            tile.setHp(0);
        } else {
            tile.setHp(newHp);
        }
        return true;
    }

    private boolean handleZombieCollision(Projectile p) {
        for (Zombie z : zombies) {
            if (z.isDead()) {
                continue;
            }
            if (!p.getHitbox().overlaps(z.getHitbox())) {
                continue;
            }
            if (z.isProjectileImmune() && p.getType() != ProjectileType.LOB) {
                break;
            }

            if (hasHitZombie(p, z)) {
                continue;
            }

            if (z instanceof ZombieDarkJuggler jj && jj.reflectProjectile()) {
                reflectProjectile(p, jj);
                return true;
            }

            applyProjectileEffect(p, z);
            markHitZombie(p, z);

            if (p.getPierce() > 1) {
                p.setPierce(p.getPierce() - 1);
                continue;
            }
            return true;
        }
        return false;
    }

    private void steerHoming(Projectile p) {
        Zombie nearest = null;
        double best = Double.MAX_VALUE;
        for (Zombie z : zombies) {
            if (z == null || z.isDead()) {
                continue;
            }
            double dx = z.getX() - p.getPositionX();
            double dy = z.getY() - p.getPositionY();
            double d2 = dx * dx + dy * dy;
            if (d2 < best) {
                best = d2;
                nearest = z;
            }
        }
        if (nearest == null) {
            return;
        }
        double dx = nearest.getX() - p.getPositionX();
        double dy = nearest.getY() - p.getPositionY();
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 1e-6) {
            return;
        }
        double speed = Math.sqrt(p.getVelX() * p.getVelX() + p.getVelY() * p.getVelY());
        if (speed < 1e-6) {
            speed = 320.0;
        }
        p.setVelocity(dx / dist * speed, dy / dist * speed);
    }

    private java.util.Set<Integer> hitSet(Projectile p) {
        Object obj = p.getExtra("hitZombies");
        if (obj instanceof java.util.Set) {
            return (java.util.Set<Integer>) obj;
        }
        java.util.Set<Integer> set = new java.util.HashSet<>();
        p.putExtra("hitZombies", set);
        return set;
    }

    private boolean hasHitZombie(Projectile p, Zombie z) {
        return hitSet(p).contains(System.identityHashCode(z));
    }

    private void markHitZombie(Projectile p, Zombie z) {
        hitSet(p).add(System.identityHashCode(z));
    }

    private void reflectProjectile(Projectile p, ZombieDarkJuggler jj) {
        ZombieProjectile reflected = new ZombieProjectile(
            (float) jj.getX(), (float) jj.getY() + 30,
            (int) p.getDamage(), 300f, (int) jj.getRow(), jj);
        this.addZombieProjectile(reflected);
        System.out.println(jj.getAlias() + " reflected a projectile");
    }

    private void applyProjectileEffect(Projectile p, Zombie z) {
        applySingleHit(p, z);
        if (p.isAreaDamage()) {
            applyAreaSplash(p, z);
        }
    }

    private void applyAreaSplash(Projectile p, Zombie primaryTarget) {
        int centerRow = (int) primaryTarget.getRow();
        double cx = primaryTarget.getX();
        for (Zombie z : zombies) {
            if (z == null || z.isDead() || z == primaryTarget) {
                continue;
            }
            int dr = Math.abs((int) z.getRow() - centerRow);
            if (dr > 1) {
                continue;
            }
            if (Math.abs(z.getX() - cx) > p.getAreaRadiusPx()) {
                continue;
            }
            applySingleHit(p, z);
        }
    }

    private void applySingleHit(Projectile p, Zombie z) {
        ProjectileType type = p.getType();
        int damage = (int) p.getDamage();

        if (type == ProjectileType.FIRE_PEA && z.isFrozen()) {
            z.thaw();
        } else if (type == ProjectileType.ICE_PEA && z.isFrozen()) {
            z.setIceHp(z.getIceHp() - damage);
            if (z.getIceHp() <= 0) {
                z.thaw();
            }
        } else {
            if (type == ProjectileType.ICE_PEA) {
                Object cd = p.getExtra("chillDuration");
                if (cd instanceof Number) {
                    z.setChillDuration(((Number) cd).floatValue());
                }
            }
            z.takeDamage(damage, resolveDamageType(p));
        }

        if (z.isDead()) {
            com.PVZ.model.enums.PlantType killer = null;
            Object obj = p.getExtra("plantType");
            if (obj instanceof com.PVZ.model.enums.PlantType pt) {
                killer = pt;
            }
            RegularGameEngine regEngine = AppStatus.getGameEngine() instanceof RegularGameEngine re ? re : null;
            notifyZombieKilled(regEngine, z, killer);
        }

        boolean isButter = Boolean.TRUE.equals(p.getExtra("stunOnHit"))
            || (p.getExtra("plantType") instanceof com.PVZ.model.enums.PlantType pt
            && pt == com.PVZ.model.enums.PlantType.KERNEL_PULT);
        if (isButter) {
            z.freeze(1.5f);
        }
    }

    private void removeDeadPlants() {
        if (map != null) {
            for (int r = 0; r < map.getRows(); r++) {
                for (int c = 0; c < map.getCols(); c++) {
                    Tile t = map.getTile(r, c);
                    if (t != null) {
                        Plant p = t.getPlant();
                        if (p != null && p.isDead()) {
                            map.removePlant(r, c);
                        }
                        Plant bp = t.getBasePlant();
                        if (bp != null && bp.isDead()) {
                            map.removeBasePlant(r, c);
                        }
                    }
                }
            }
        }
        plants.removeIf(p -> p == null || p.isDead());
    }

    @Override
    public List<Zombie> getZombiesInLane(int lane) {
        java.util.List<Zombie> out = new java.util.ArrayList<>();
        for (Zombie zombie : zombies) {
            if (zombie != null && !zombie.isDead() && (int) zombie.getRow() == lane) {
                out.add(zombie);
            }
        }
        return out;
    }

    @Override
    public int getRowCount() {
        return map != null ? map.getRows() : 5;
    }

    @Override
    public List<Zombie> getAllZombies() {
        return new java.util.ArrayList<>(zombies);
    }

    @Override
    public Plant getPlantAt(int row, int col) {
        return map != null ? map.getPlantAt(row, col) : null;
    }

    @Override
    public List<Plant> getAllPlants() {
        return new java.util.ArrayList<>(plants);
    }

    public List<Plant> getPlants() {
        return plants;
    }

    @Override
    public void removePlant(int row, int col) {
        if (map != null) {
            map.removePlant(row, col);
        }
        plants.removeIf(p -> p != null && !p.isDead()
            && asInt(p.getRuntimeState("row"), Integer.MIN_VALUE) == row
            && asInt(p.getRuntimeState("col"), Integer.MIN_VALUE) == col);
    }

    @Override
    public void spawnProjectile(Object projectile) {
        if (projectile instanceof Projectile p) {
            placeProjectileOnMap(p);
            projectiles.add(p);
        }
    }

    private void placeProjectileOnMap(Projectile p) {
        if (p == null || p.isWorldPositioned()) {
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

        float worldX = startX + col * tileWidth + tileWidth * resolveDx(p);
        float worldY = startY - (row + 1) * tileHeight + tileHeight * resolveDy(p);

        float speedPxPerSec = tileWidth * 1.5f;
        if (p.getType() == ProjectileType.LOB) {
            speedPxPerSec = tileWidth * 0.9f;
        }
        float speedMultiplier = (float) Math.max(0.1, Math.abs(p.getSpeed()));
        speedPxPerSec *= speedMultiplier;

        double horizontalSign = p.getSpeed() < 0 ? -1.0 : 1.0;
        if (Boolean.TRUE.equals(p.getExtra("reverseDirection"))) {
            horizontalSign = -horizontalSign;
        }
        double verticalSpeed = 0.0;
        Object targetLaneState = p.getExtra("targetLane");
        if (targetLaneState instanceof Number number) {
            int targetLane = number.intValue();
            if (targetLane != row) {
                verticalSpeed = Math.signum(targetLane - row) * speedPxPerSec;
            }
        }

        if (p.getType() == com.PVZ.model.entity.plants.behavior.impl.ProjectileType.LOB) {
            p.initArcPosition(worldX, worldY, (float) (horizontalSign * speedPxPerSec));
        } else {
            p.initWorldPosition(worldX, worldY, (float) (horizontalSign * speedPxPerSec), (float) verticalSpeed);
        }
    }

    private static float resolveDx(Projectile p) {
        Object plantType = p.getExtra("plantType");
        return com.PVZ.model.entity.plants.behavior.impl.ProjectileSpawnOffsets
            .dx(plantType == null ? null : plantType.toString());
    }

    private static float resolveDy(Projectile p) {
        Object plantType = p.getExtra("plantType");
        return com.PVZ.model.entity.plants.behavior.impl.ProjectileSpawnOffsets
            .dy(plantType == null ? null : plantType.toString());
    }

    @Override
    public void spawnSun(int amount) {
        addSun(amount);
    }

    @Override
    public void addSun(int amount) {
        if (gameStatus == null || amount == 0) {
            return;
        }
        gameStatus.setSunflower(Math.max(0, gameStatus.getSunflower() + amount));
    }

    @Override
    public void damageArea(int lane, int row, int damage) {
        if (damage <= 0) {
            return;
        }

        int[] lanes = {row - 1, row, row + 1};
        for (int r : lanes) {
            if (r < 0) continue;
            Tile tile0 = map != null ? map.getTile(r, 0) : null;
            float centreX = tile0 != null ? tile0.getX() + lane * tile0.getWidth() : 0;
            float tileW = tile0 != null ? tile0.getWidth() : 177f;
            for (Zombie zombie : getZombiesInLane(r)) {
                if (zombie != null) {
                    double colDist = Math.abs(zombie.getX() - centreX);
                    if (colDist < tileW * 1.6) {
                        zombie.takeDamage(damage);
                    }
                }
            }
        }
    }

    @Override
    public void damageSingleTarget(Object target, int damage) {
        if (target instanceof Zombie z && z != null && !z.isDead()) {
            z.takeDamage(damage);
        }
    }

    @Override
    public void freezeZombiesInLane(int lane, double seconds) {
        for (Zombie z : getZombiesInLane(lane)) {
            if (z != null && !z.isDead()) {
                z.freeze((float) seconds);
            }
        }
    }

    public void freezeClosestZombieInLane(int lane, double seconds) {
        Zombie nearest = null;
        double bestX = Double.MAX_VALUE;
        for (Zombie z : getZombiesInLane(lane)) {
            if (z == null || z.isDead()) {
                continue;
            }
            double x = z.getX();
            if (x < bestX) {
                bestX = x;
                nearest = z;
            }
        }
        if (nearest != null) {
            nearest.freeze((float) seconds);
        }
    }

    @Override
    public void freezeAllZombies(double seconds) {
        for (Zombie z : zombies) {
            if (z != null && !z.isDead()) {
                z.freeze((float) seconds);
            }
        }
    }

    @Override
    public void killClosestZombieInLane(int lane) {
        Zombie nearest = null;
        double bestX = Double.MAX_VALUE;
        for (Zombie z : getZombiesInLane(lane)) {
            if (z == null || z.isDead()) {
                continue;
            }
            double x = z.getX();
            if (x < bestX) {
                bestX = x;
                nearest = z;
            }
        }
        if (nearest != null) {
            nearest.takeDamage(Integer.MAX_VALUE / 2);
        }
    }

    @Override
    public void meltIceInLane(int lane) {
        for (Zombie z : getZombiesInLane(lane)) {
            if (z != null && z.isFrozen()) {
                z.thaw();
            }
        }
    }

    @Override
    public void hypnotizeZombiesInLane(int lane, double seconds) {
        for (Zombie z : getZombiesInLane(lane)) {
            if (z != null && !z.isDead()) {
                z.hypnotize((float) seconds);
            }
        }
    }

    @Override
    public void killRandomZombies(int count) {
        java.util.List<Zombie> alive = new java.util.ArrayList<>();
        for (Zombie z : zombies) {
            if (z != null && !z.isDead()) {
                alive.add(z);
            }
        }
        java.util.Collections.shuffle(alive);
        for (int i = 0; i < Math.min(count, alive.size()); i++) {
            alive.get(i).takeDamage(Integer.MAX_VALUE / 2);
        }
    }

    @Override
    public void spawnBouncingProjectiles(int lane, int row, int count,
                                         int damagePerGrape, double lifespanSeconds) {
        if (map == null) return;
        float tileW = map.getTileWidth();
        float tileH = map.getTileHeight();
        float centreX = map.getStartX() + lane * tileW + tileW * 0.5f;
        float centreY = map.getStartY() - (row + 1) * tileH + tileH * 0.5f;
        float minX = map.getStartX();
        float maxX = minX + map.getCols() * tileW;
        float minY = map.getStartY() - map.getRows() * tileH;
        float maxY = map.getStartY();
        double fuseSec = Math.max(lifespanSeconds, 4.0);
        double speed = tileW * 0.28;
        for (int i = 0; i < Math.max(1, Math.min(count, 20)); i++) {
            double angle = 2 * Math.PI * i / count +
                (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 0.4;
            float vx = (float) (Math.cos(angle) * speed);
            float vy = (float) (Math.sin(angle) * speed * 0.6);
            Projectile grape = new Projectile();
            grape.setType(ProjectileType.GRAPE);
            grape.setDamage(Math.max(1, damagePerGrape));
            grape.setPierce(0);
            grape.initFreePosition(centreX, centreY, vx, vy);
            grape.setBouncing(true);
            grape.setFuse(fuseSec);
            grape.setBounds(minX, maxX, minY, maxY);
            projectiles.add(grape);
        }
    }


    public int getTileColumn(float worldX) {
        return map != null ? map.worldToCol(worldX) : 0;
    }

    public void triggerGameOver() {
        if (gameStatus == null || gameStatus.isGameOver()) {
            return;
        }
        System.out.println("GAME OVER — zombie reached the house!");
        gameStatus.setGameOver(true);
        AppStatus.returnToChapterAndLevelSelection("GAME OVER");
    }

    public void removeZombie(Zombie zombie) {
        zombies.remove(zombie);
    }

    private DamageType resolveDamageType(Projectile p) {
        if (p.getType() == ProjectileType.ICE_PEA) {
            return DamageType.ICE;
        }
        Object dt = p.getExtra("damageType");
        if (dt instanceof DamageType) {
            return (DamageType) dt;
        }
        return DamageType.NORMAL;
    }

    public Zombie findZombieAt(int col, int row) {
        for (Zombie z : zombies) {
            if ((int) z.getRow() == row) {
                int zCol = getTileColumn((float) z.getX());
                if (zCol == col || zCol == col + 1) {
                    return z;
                }
            }
        }
        return null;
    }

    public void addZombieProjectile(ZombieProjectile p) {
        zombieProjectiles.add(p);
    }

    public void removeZombieProjectile(ZombieProjectile p) {
        zombieProjectiles.remove(p);
    }

    public void drawProjectiles(SpriteBatch batch) {
        for (ZombieProjectile zp : zombieProjectiles) {
            zp.draw(batch);
        }
    }

    public void addZombie(Zombie z) {
        zombies.add(z);
    }

    public TileType getTileTypeAt(int row, int col) {
        Tile tile = map.getTile(row, col);
        return tile != null ? tile.getType() : TileType.NORMAL;
    }

    public void setTileTypeAt(int row, int col, TileType type) {
        Tile tile = map.getTile(row, col);
        if (tile != null) {
            tile.setType(type);
        }
    }

    public void dispose() {
        zombieProjectiles.clear();
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
}
