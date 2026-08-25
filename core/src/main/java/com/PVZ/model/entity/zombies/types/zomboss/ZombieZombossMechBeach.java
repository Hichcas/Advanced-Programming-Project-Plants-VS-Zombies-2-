package com.PVZ.model.entity.zombies.types.zomboss;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.entity.zombies.factory.ZombieFactory;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.Map;

import java.util.ArrayList;
import java.util.List;

public class ZombieZombossMechBeach extends AbstractZomboss {

    // === Turbine Suction State ===
    private boolean turbineActive = false;
    private float turbineTimer = 0f;

    // === Small Shark Projectile State ===
    public static class SmallShark {
        public float x, y;
        public int row;
        public float speed;
        public float animTime;
        public boolean active;

        public SmallShark(float startX, float startY, int row) {
            this.x = startX;
            this.y = startY;
            this.row = row;
            this.speed = 460f;
            this.animTime = 0f;
            this.active = true;
        }
    }

    private final List<SmallShark> activeSharks = new ArrayList<>();

    public ZombieZombossMechBeach() {
        super("ZombieZombossMechBeach", 16000, 600, 0.1, 6000, 12000, defaultScaledProps(),
              0.33, 3, 7.0);
        this.hitbox.setSize(240f, 240f);
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        return list;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        this.hitbox.setSize(240f, 240f);
    }

    @Override
    public void onPhaseTransition(BattleController ctrl) {
        currentSpeed = speed * (1 + currentPhase * 0.25);
        abilityCooldown = Math.max(3.5, abilityCooldown - 1.0);
        triggerStun(4.0f);
        System.out.println("[ZombossBeach] Shark Submarine entered Phase " + currentPhase + "! Stunned for 4.0s");
    }

    @Override
    public void onUpdate(float delta, BattleController ctrl) {
        super.onUpdate(delta, ctrl);
        if (ctrl == null) return;

        // Update Stun State
        if (isStunned()) {
            turbineActive = false;
            activeSharks.clear();
            return;
        }

        // Update Turbine Suction
        if (turbineActive) {
            turbineTimer += delta;
            updateTurbineSuction(delta, ctrl);
            if (turbineTimer >= 3.5f) {
                turbineActive = false;
                ZombieAnimation.trigger(this, "suction_off", 0.6);
            }
        }

        // Update Small Shark Projectiles
        if (!activeSharks.isEmpty()) {
            updateSharks(delta, ctrl);
        }
    }

    @Override
    public void useSpecialAbility(BattleController ctrl) {
        if (isStunned() || turbineActive) return;

        double roll = Math.random();
        if (roll < 0.40) {
            triggerTurbineSuction(ctrl);
        } else if (roll < 0.75) {
            triggerSmallSharksAttack(ctrl);
        } else {
            triggerSummonWave(ctrl);
        }
    }

    // =========================================================================
    // 1. TURBINE SUCTION MECHANIC (30 pts + 15 pts animation)
    // =========================================================================

    public void triggerTurbineSuction(BattleController ctrl) {
        if (isStunned() || ctrl == null || ctrl.getMap() == null) return;
        turbineActive = true;
        turbineTimer = 0f;
        ZombieAnimation.trigger(this, "suction_loop", 3.5);
        com.PVZ.model.status.AppStatus.triggerCameraShake();

        System.out.println("[ZombossBeach] Shark Submarine activated Turbine Suction!");
    }

    private void updateTurbineSuction(float delta, BattleController ctrl) {
        Map map = ctrl.getMap();
        if (map == null) return;

        int r1 = Math.max(0, Math.min(3, (int) this.getRow()));
        int r2 = r1 + 1;

        // Sucking the rightmost plant in the 2 rows towards the shark grinder
        for (int r : new int[]{r1, r2}) {
            for (int c = map.getCols() - 1; c >= 0; c--) {
                Plant p = map.getPlantAt(r, c);
                if (p != null && !p.isDead()) {
                    if (turbineTimer > 1.2f) { // Churning and pulling into grinder
                        p.takeDamage(999999);
                        map.removePlant(r, c);
                        if (ctrl.getEngine() != null) {
                            Tile t = map.getTile(r, c);
                            float tx = t != null ? t.getX() + t.getWidth() / 2f : 1000f;
                            float ty = t != null ? t.getY() + t.getHeight() / 2f : 400f;
                            ctrl.getEngine().addTimedPamEffect(
                                "768/FULL/EFFECTS/WATER_SPLASH/WATER_SPLASH.PAM",
                                "animation", 1.0, 1.0f, tx, ty
                            );
                        }
                        System.out.println("[ZombossBeach] Turbine swallowed plant at (" + r + ", " + c + ")!");
                    }
                    break; // Pulled the frontmost plant
                }
            }
        }
    }

    // =========================================================================
    // 2. SMALL SHARK PROJECTILES ATTACK (15 pts + 15 pts animation)
    // =========================================================================

    public void triggerSmallSharksAttack(BattleController ctrl) {
        if (isStunned() || ctrl == null || ctrl.getMap() == null) return;
        ZombieAnimation.trigger(this, "spawn", 2.2);

        int r1 = Math.max(0, Math.min(3, (int) this.getRow()));
        int r2 = r1 + 1;

        float startX = (float) this.x - 30f;
        Map map = ctrl.getMap();

        for (int r : new int[]{r1, r2}) {
            Tile t = map.getTile(r, 7);
            float startY = t != null ? t.getY() + (t.getHeight() - 40f) / 2f : (float) this.getY();
            activeSharks.add(new SmallShark(startX, startY, r));
        }

        System.out.println("[ZombossBeach] Launched Small Sharks across rows " + r1 + " & " + r2 + "!");
    }

    private void updateSharks(float delta, BattleController ctrl) {
        List<SmallShark> toRemove = new ArrayList<>();
        Map map = ctrl.getMap();

        for (SmallShark shark : activeSharks) {
            shark.animTime += delta;
            shark.x -= shark.speed * delta;

            if (shark.x < 200f) {
                toRemove.add(shark);
                continue;
            }

            if (map != null) {
                for (int c = 0; c < map.getCols(); c++) {
                    Tile t = map.getTile(shark.row, c);
                    if (t != null) {
                        float tx = t.getX();
                        float tw = t.getWidth();
                        if (shark.x >= tx && shark.x <= tx + tw) {
                            Plant p = map.getPlantAt(shark.row, c);
                            if (p != null && !p.isDead()) {
                                p.takeDamage(999999);
                                map.removePlant(shark.row, c);
                                System.out.println("[ZombossBeach] Small Shark devoured plant at (" + shark.row + ", " + c + ")!");
                                toRemove.add(shark);
                                break;
                            }
                        }
                    }
                }
            }
        }
        activeSharks.removeAll(toRemove);
    }

    // =========================================================================
    // 3. SUMMON BEACH ZOMBIE WAVE
    // =========================================================================

    @Override
    public void spawnZombieWave(BattleController ctrl) {
        triggerSummonWave(ctrl);
    }

    public void triggerSummonWave(BattleController ctrl) {
        if (ctrl == null) return;
        ZombieAnimation.trigger(this, "spawn", 2.2);

        String[] beachTypes = currentPhase >= 3 ?
            new String[]{"ZombieBeachOctopusDefault", "ZombieBeachFishermanDefault", "ZombieBeachSurferDefault", "ZombieBeachArmor2Default", "ZombieBeachImpDefault"} :
            new String[]{"ZombieBeachSurferDefault", "ZombieBeachDefault", "ZombieBeachImpDefault", "ZombieBeachArmor1Default"};

        int count = 2 + currentPhase;
        float zombossX = (float) this.getX() - 80f;
        int zombossRow = (int) this.getRow();

        Tile tile = ctrl.getMap() != null ? ctrl.getMap().getTile(zombossRow, 7) : null;
        float spawnY = tile != null ? (tile.getY() + (tile.getHeight() - 70f) / 2f) : (float) this.getY();

        for (int i = 0; i < count; i++) {
            String type = beachTypes[(int) (Math.random() * beachTypes.length)];
            Zombie z = ZombieFactory.createZombie(type);
            if (z != null) {
                int spawnRow = Math.min(4, Math.max(0, zombossRow + (i % 2)));
                z.initPosition(zombossX - (i * 35f), spawnY, spawnRow);
                z.setRow(spawnRow);
                z.setCol(7);
                ctrl.addZombie(z);
            }
        }
        System.out.println("[ZombossBeach] Summoned " + count + " Beach zombies!");
    }

    // =========================================================================
    // Getters for Renderer
    // =========================================================================

    public boolean isTurbineActive() { return turbineActive; }
    public float getTurbineTimer() { return turbineTimer; }
    public List<SmallShark> getActiveSharks() { return activeSharks; }
}
