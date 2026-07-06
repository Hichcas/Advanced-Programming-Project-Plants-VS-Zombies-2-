package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.plants.behavior.impl.ProjectileType;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.DamageType;

import java.util.Iterator;
import java.util.List;

public class BattleController {

    private final List<Zombie> zombies;
    private final List<Plant> plants;
    private final List<Projectile> projectiles;
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

    public void update(float delta) {
        for (int i = zombies.size() - 1; i >= 0; i--) {
            zombies.get(i).update(delta, this);
        }

        Iterator<Projectile> projIt = projectiles.iterator();
        while (projIt.hasNext()) {
            Projectile p = projIt.next();
            for (Zombie z : zombies) {
                if (z.isDead()) continue;
                if (p.getHitbox().overlaps(z.getHitbox())) {
                    z.takeDamage((int) p.getDamage(), resolveDamageType(p));
                    projIt.remove();
                    break;
                }
            }
        }

        Iterator<Plant> pit = plants.iterator();
        while (pit.hasNext()) {
            Plant p = pit.next();
            if (p.isDead()) {
                int r = (int) p.getRuntimeState("row");
                int c = (int) p.getRuntimeState("col");
                if (map != null) map.removePlant(r, c);
                pit.remove();
            }
        }
    }

    // ── zombie callback methods ──

    public Plant getPlantAt(int row, int col) {
        return map != null ? map.getPlantAt(row, col) : null;
    }

    public int getTileColumn(float worldX) {
        return map != null ? map.worldToCol(worldX) : 0;
    }

    public void triggerGameOver() {
        System.out.println("GAME OVER — zombie reached the house!");
        gameStatus.setGameOver(true);
    }

    public void addSun(int amount) {
    }

    public void removeZombie(Zombie zombie) {
        zombies.remove(zombie);
    }

    private DamageType resolveDamageType(Projectile p) {
        if (p.getType() == ProjectileType.ICE_PEA) return DamageType.ICE;
        Object dt = p.getExtra("damageType");
        if (dt instanceof DamageType) return (DamageType) dt;
        return DamageType.NORMAL;
    }

    public Zombie findZombieAt(int col, int row) {
        for (Zombie z : zombies) {
            if ((int)z.getRow() == row && getTileColumn((float)z.getX()) == col)
                return z;
        }
        return null;
    }
}
