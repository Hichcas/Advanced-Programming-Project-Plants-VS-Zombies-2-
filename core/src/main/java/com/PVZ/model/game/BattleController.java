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
     * Handles projectile collision with tombstone/necromancy tiles.
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
            if (z.isProjectileImmune()) {
                break;
            }

            // Check reflection by Dark Juggler
            if (z instanceof ZombieDarkJuggler jj && jj.reflectProjectile()) {
                reflectProjectile(p, jj);
                return true;
            }

            // Apply damage and effects
            applyProjectileEffect(p, z);
            return true;
        }
        return false;
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

        p.initWorldPosition(worldX, worldY, (float) (horizontalSign * speedPxPerSec), (float) verticalSpeed);
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
        for (Zombie zombie : getZombiesInLane(lane)) {
            if (zombie != null) {
                zombie.takeDamage(damage);
            }
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
            if ((int) z.getRow() == row && getTileColumn((float) z.getX()) == col) {
                return z;
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
