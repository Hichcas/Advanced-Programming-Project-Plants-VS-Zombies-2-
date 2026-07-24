package com.PVZ.model.game;

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
import com.badlogic.gdx.math.Rectangle;

import java.util.Iterator;
import java.util.List;

/**
 * Controls the battle logic between plants and zombies.
 * Refactored to comply with Checkstyle and PMD (method length ≤ 50 lines).
 */
public class BattleController implements BehaviorContext {

    private final List<Zombie> zombies;
    private final List<Plant> plants;
    private final List<Projectile> projectiles;
    private final List<ZombieProjectile> zombieProjectiles = new java.util.ArrayList<>();
    private final GameStatus gameStatus;
    private Map map;

    public BattleController(List<Zombie> zombies, List<Plant> plants,
                            List<Projectile> projectiles, GameStatus gameStatus) {
        this.zombies = zombies;
        this.plants = plants;
        this.projectiles = projectiles;
        this.gameStatus = gameStatus;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public Map getMap() {
        return map;
    }

    /**
     * Main update loop, broken into smaller methods.
     */
    public void update(float delta) {
        updateZombies(delta);
        updateZombieProjectiles(delta);
        updatePlantProjectiles(delta);
        removeDeadPlants();
        removeExpiredGrapes();
    }

    /**
     * Removes grapes whose fuse has expired after bouncing. The grape's isFuseExploded()
     * flag is set by Projectile.updateFreeMotion when the countdown reaches zero.
     * At that point the grape detonates with a small area-of-effect at its current
     * position before being removed.
     */
    private void removeExpiredGrapes() {
        java.util.Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            if (p.isFuseExploded()) {
                // Small detonation at the grape's final position
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
                p.consumeFuseExplosion(); // marks destroyed + removes from list
                it.remove();
            }
        }
    }

    /**
     * Updates all zombies.
     */
    private void updateZombies(float delta) {
        for (int i = zombies.size() - 1; i >= 0; i--) {
            zombies.get(i).update(delta, this);
        }
    }

    /**
     * Updates zombie projectiles and checks collisions with plants.
     */
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

    /**
     * Updates plant projectiles: handles collisions with tiles and zombies.
     */
    private void updatePlantProjectiles(float delta) {
        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();

            // Homing projectiles (Cat-tail) re-aim toward the nearest zombie every tick.
            if (p.isHoming() && p.isFreeMotion()) {
                steerHoming(p);
            }

            // Check collision with tombstone tiles (block straight projectiles)
            if (handleTileCollision(p)) {
                projIt.remove();
                continue;
            }

            // Check collision with zombies
            if (handleZombieCollision(p)) {
                projIt.remove();
            }
        }
    }

    /**
     * Handles projectile collision with tombstone/necromancy tiles and octopus.
     * Returns true if projectile should be removed.
     */
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

    /**
     * Handles projectile collision with zombies.
     * Returns true if projectile should be removed.
     */
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
            // A piercing projectile (Cactus spike, Fume-shroom smoke) must not damage the
            // same zombie on every overlapping tick — skip ones it already hit.
            if (hasHitZombie(p, z)) {
                continue;
            }

            // Check reflection by Dark Juggler
            if (z instanceof ZombieDarkJuggler jj && jj.reflectProjectile()) {
                reflectProjectile(p, jj);
                return true;
            }

            // Apply damage and effects
            applyProjectileEffect(p, z);
            markHitZombie(p, z);

            // Pierce: while pierce remains, the projectile passes through and keeps flying
            // (Cactus pierces 3 zombies, Fume-shroom smoke passes through the whole lane).
            if (p.getPierce() > 1) {
                p.setPierce(p.getPierce() - 1);
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * Re-aims a homing projectile (Cat-tail) at the nearest living zombie, keeping its speed
     * constant so it visibly curves toward and lands on the closest target.
     */
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

    @SuppressWarnings("unchecked")
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

    /**
     * Reflects a projectile back via ZombieDarkJuggler.
     */
    private void reflectProjectile(Projectile p, ZombieDarkJuggler jj) {
        ZombieProjectile reflected = new ZombieProjectile(
            (float) jj.getX(), (float) jj.getY() + 30,
            (int) p.getDamage(), 300f, (int) jj.getRow(), jj);
        this.addZombieProjectile(reflected);
        System.out.println(jj.getAlias() + " reflected a projectile");
    }

    /**
     * Applies projectile damage and special effects to a zombie.
     */
    private void applyProjectileEffect(Projectile p, Zombie z) {
        ProjectileType type = p.getType();
        int damage = (int) p.getDamage();

        // Fire vs Ice interactions
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

        // Stun effect (butter)
        boolean isButter = Boolean.TRUE.equals(p.getExtra("stunOnHit"))
            || (p.getExtra("plantType") instanceof com.PVZ.model.enums.PlantType pt
            && pt == com.PVZ.model.enums.PlantType.KERNEL_PULT);
        if (isButter) {
            z.freeze(1.5f);
        }
    }

    /**
     * Removes dead plants from the list and map.
     */
    private void removeDeadPlants() {
        Iterator<Plant> pit = plants.iterator();
        while (pit.hasNext()) {
            Plant p = pit.next();
            if (p.isDead()) {
                int r = asInt(p.getRuntimeState("row"), 0);
                int c = asInt(p.getRuntimeState("col"), 0);
                if (map != null) {
                    map.removePlant(r, c);
                }
                pit.remove();
            }
        }
    }

    // ------------------------------------------------------------------------
    // BehaviorContext implementation (unchanged but split for readability)
    // ------------------------------------------------------------------------

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

        float worldX = startX + col * tileWidth + tileWidth * 0.5f;
        float worldY = startY - (row + 1) * tileHeight + tileHeight * 0.35f;

        float speedPxPerSec = tileWidth * 1.5f;
        if (p.getType() == ProjectileType.LOB) {
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

        if (p.getType() == com.PVZ.model.entity.plants.behavior.impl.ProjectileType.LOB) {
            // Lobbed shots (Cabbage-pult, Kernel-pult, Melon-pult, ...) arc up and over
            // obstacles: a parabola in screen space (rise then land) instead of a flat line.
            p.initArcPosition(worldX, worldY, (float) (horizontalSign * speedPxPerSec));
        } else {
            p.initWorldPosition(worldX, worldY, (float) (horizontalSign * speedPxPerSec), (float) verticalSpeed);
        }
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
        // 3x3 explosion: lanes row-1, row, row+1; column check ~1.5 tiles
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
        double speed = tileW * 0.28; // slowed down further per feedback so grapes drift gently
        for (int i = 0; i < Math.max(1, Math.min(count, 20)); i++) {
            double angle = 2 * Math.PI * i / count + (java.util.concurrent.ThreadLocalRandom.current().nextDouble() - 0.5) * 0.4;
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

    // ------------------------------------------------------------------------
    // Public methods for external use
    // ------------------------------------------------------------------------

    public int getTileColumn(float worldX) {
        return map != null ? map.worldToCol(worldX) : 0;
    }

    public void triggerGameOver() {
        if (gameStatus == null || gameStatus.isGameOver()) {
            return;
        }
        System.out.println("GAME OVER — zombie reached the house!");
        gameStatus.setGameOver(true);
        AppStatus.returnToMainMenu("GAME OVER");
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

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------

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
