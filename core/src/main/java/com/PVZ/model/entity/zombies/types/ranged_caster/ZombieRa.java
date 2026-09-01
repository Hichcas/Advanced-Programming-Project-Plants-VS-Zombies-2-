package com.PVZ.model.entity.zombies.types.ranged_caster;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Sun;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieProjectile;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.SunManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ZombieRa extends AbstractRangedCasterZombie {
    private int stolenSunAmount;

    public ZombieRa() {
        super("ZombieRa", 380, 100, 0.185, 700, 3000, defaultScaledProps(),
              0, 0, 3.0, 0);
        this.stolenSunAmount = 0;
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("ProjectileDamage", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        return list;
    }

    private enum StealState { IDLE, POWER_UP, POWER, POWER_DOWN }
    private StealState stealState = StealState.IDLE;
    private float stealTimer = 0.0f;

    @Override
    public void update(float delta, BattleController ctrl) {
        super.update(delta, ctrl);
        if (!isDead() && ctrl != null) {
            com.PVZ.model.game.RegularGameEngine engine = com.PVZ.model.status.AppStatus.getGameEngine()
                instanceof com.PVZ.model.game.RegularGameEngine re ? re : null;
            if (engine != null && engine.getSunManager() != null) {
                stealNearbySun(engine.getSunManager());
            }
        }
    }

    @Override
    public void shoot(BattleController controller, Plant target) {
        controller.addZombieProjectile(new ZombieProjectile(
            (float) x, (float) y, (int) projectileDamage, (float) projectileSpeed, (int) row, this));
    }

    @Override
    public void onHit(Plant target) { }

    @Override
    public void stealNearbySun(SunManager sunManager) {
        if (sunManager == null || isDead()) {return;}
        double range = 400.0;
        List<Sun> nearbySuns = new ArrayList<>();
        for (Sun sun : sunManager.getSuns()) {
            if (sun != null && !sun.isCollected() && !sun.isTimedOut()) {
                double dx = sun.getX() - x;
                double dy = sun.getY() - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < range) {nearbySuns.add(sun);}}}
        if (nearbySuns.isEmpty()) {
            if (stealState == StealState.POWER || stealState == StealState.POWER_UP) {
                stealState = StealState.POWER_DOWN;
                stealTimer = 0;
                com.PVZ.model.entity.zombies.base.ZombieAnimation.trigger(this, "power_down", 1.2667);
                System.out.println("[ZombieRa] Finished stealing sun. Lowering staff (power_down).");
            }return;}
        if (stealState == StealState.IDLE) {
            stealState = StealState.POWER_UP;stealTimer = 0;
            com.PVZ.model.entity.zombies.base.ZombieAnimation.trigger(this, "power_up", 0.6667);
            System.out.println("[ZombieRa] Sun detected nearby! Raising Anubis staff (power_up).");
        } else if (stealState == StealState.POWER_UP) {
            stealTimer += 0.016f;
            if (stealTimer >= 0.6667f) {
                stealState = StealState.POWER;stealTimer = 0;
                com.PVZ.model.entity.zombies.base.ZombieAnimation.trigger(this, "power", 1.0);
                System.out.println("[ZombieRa] Staff raised! Activating Anubis beam (power).");}
        } else if (stealState == StealState.POWER) {
            com.PVZ.model.entity.zombies.base.ZombieAnimation.trigger(this, "power", 1.0);
            for (Sun sun : nearbySuns) {
                double speed = 250.0 * 0.016;
                double dx = x - sun.getX();
                double dy = y - sun.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 30.0) {
                    stolenSunAmount += sun.getAmount();
                    sun.collect();
                    System.out.println("[ZombieRa] Absorbed sun! Amount: " +
                        sun.getAmount() + " | Total stolen sun: " + stolenSunAmount);
                } else {sun.setPosition(sun.getX() + (dx / dist) * speed, sun.getY() + (dy / dist) * speed);}}}
    }

    @Override
    public void onDestroy() { }

    @Override
    public void die(BattleController controller) {
        if (stolenSunAmount > 0) {
            com.PVZ.model.game.RegularGameEngine engine = com.PVZ.model.status.AppStatus.getGameEngine()
                instanceof com.PVZ.model.game.RegularGameEngine re ? re : null;
            if (engine != null && engine.getSunManager() != null) {
                int count = Math.max(1, stolenSunAmount / 25);
                int sunPerEntity = stolenSunAmount / count;
                for (int i = 0; i < count; i++) {
                    double offsetX = (Math.random() - 0.5) * 60.0;
                    double offsetY = (Math.random() - 0.5) * 40.0;
                    engine.getSunManager().spawn(x + offsetX, y + offsetY, sunPerEntity);
                }
                System.out.println("[ZombieRa] Ra zombie died! Dropped "
                    + count + " sun(s) (" + stolenSunAmount +
                    " sun total) at position (" + Math.round(x) + ", " + Math.round(y) + ").");
            } else if (controller != null) {
                controller.addSun(stolenSunAmount);
                System.out.println("[ZombieRa] Ra zombie died! Returned "
                    + stolenSunAmount + " stolen suns back to player.");
            }
        }
        super.die(controller);
    }

    public int getStolenSunAmount() {
        return stolenSunAmount;
    }
}
