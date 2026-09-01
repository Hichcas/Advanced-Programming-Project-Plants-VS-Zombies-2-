package com.PVZ.model.entity.zombies.types.zomboss;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.entity.zombies.factory.ZombieFactory;
import com.PVZ.model.enums.GraveVariant;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.Map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZombieZombossMechEgypt extends AbstractZomboss {

    // === Missile Attack State ===
    private boolean missileActive = false;
    private float missileAnimTime = 0f;
    private float missileTargetX = 0f;
    private float missileTargetY = 0f;
    private int missileTargetRow = 0;
    private int missileTargetCol = 0;
    private float missileCurrentY = 1250f;
    private float missileDropSpeed = 850f;
    private boolean missileExploded = false;

    // === Charge / Dash Attack State ===
    public enum ChargeState { IDLE, WINDUP, CHARGING_FORWARD, RETURNING }
    private ChargeState chargeState = ChargeState.IDLE;
    private float chargeTimer = 0f;
    private float homeX = 1800f;
    private float targetChargeX = 520f;
    private float chargeSpeed = 700f;

    public ZombieZombossMechEgypt() {
        super("ZombieZombossMechEgypt", 15000, 500, 0.12, 5000, 10000, defaultScaledProps(),
              0.33, 3, 7.0);
        this.hitbox.setSize(220f, 230f);
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
        this.homeX = (float) this.x;
        if (this.homeX < 1500f) {
            this.homeX = 1750f;
        }
    }

    @Override
    public void onPhaseTransition(BattleController ctrl) {
        currentSpeed = speed * (1 + currentPhase * 0.2);
        abilityCooldown = Math.max(3.5, abilityCooldown - 1.0);
        triggerStun(4.0f);
        System.out.println("[ZombossEgypt] Phase " + currentPhase + " activated! Stunned for 4.0s");
    }

    @Override
    public void onUpdate(float delta, BattleController ctrl) {
        super.onUpdate(delta, ctrl);
        if (ctrl == null) return;

        // Update Stun State
        if (isStunned()) {
            stunTimer -= delta;
            if (stunTimer <= 0f) {
                stunTimer = 0f;
                ZombieAnimation.trigger(this, "stun_end", 1.0);
            }
            return;
        }

        // Update Missile Attack
        if (missileActive) {
            updateMissile(delta, ctrl);
        }

        // Update Charge / Dash Attack
        if (chargeState != ChargeState.IDLE) {
            updateCharge(delta, ctrl);
        }
    }

    @Override
    public void useSpecialAbility(BattleController ctrl) {
        if (isStunned() || chargeState != ChargeState.IDLE || missileActive) return;

        double roll = Math.random();
        if (roll < 0.45) {
            triggerMissileAttack(ctrl);
        } else if (roll < 0.80) {
            triggerChargeAttack(ctrl);
        } else {
            triggerPortalSpawn(ctrl);
        }
    }

    // =========================================================================
    // 1. MISSILE ATTACK
    // =========================================================================

    public void triggerMissileAttack(BattleController ctrl) {
        if (ctrl == null || ctrl.getMap() == null) return;
        Map map = ctrl.getMap();

        // 1. Pick a target tile (prioritizing tiles with living plants)
        List<int[]> plantTiles = new ArrayList<>();
        List<int[]> allTiles = new ArrayList<>();
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 8; c++) {
                allTiles.add(new int[]{r, c});
                if (map.getPlantAt(r, c) != null && !map.getPlantAt(r, c).isDead()) {
                    plantTiles.add(new int[]{r, c});
                }
            }
        }

        int[] chosen;
        if (!plantTiles.isEmpty()) {
            chosen = plantTiles.get((int) (Math.random() * plantTiles.size()));
        } else {
            chosen = allTiles.get((int) (Math.random() * allTiles.size()));
        }

        missileTargetRow = chosen[0];
        missileTargetCol = chosen[1];
        Tile t = map.getTile(missileTargetRow, missileTargetCol);
        if (t != null) {
            missileTargetX = t.getX() + t.getWidth() / 2f;
            missileTargetY = t.getY() + t.getHeight() / 2f;
        } else {
            missileTargetX = 800f;
            missileTargetY = 400f;
        }

        missileActive = true;
        missileAnimTime = 0f;
        missileCurrentY = 1250f;
        missileExploded = false;

        // Play boss missile animation
        ZombieAnimation.trigger(this, "missile_start", 1.5);
        System.out.println("[ZombossEgypt] Fired vertical missile targeting ("
            + missileTargetRow + ", " + missileTargetCol + ")!");
    }

    private void updateMissile(float delta, BattleController ctrl) {
        missileAnimTime += delta;

        if (missileCurrentY > missileTargetY) {
            missileCurrentY -= missileDropSpeed * delta;
            if (missileCurrentY <= missileTargetY) {
                missileCurrentY = missileTargetY;
                onMissileImpact(ctrl);
            }
        } else {
            if (missileAnimTime > 2.5f) {
                missileActive = false;
            }
        }
    }

    private void onMissileImpact(BattleController ctrl) {
        if (missileExploded) return;
        missileExploded = true;
        com.PVZ.model.status.AppStatus.triggerCameraShake();

        if (ctrl.getEngine() != null) {
            ctrl.getEngine().addTimedPamEffect(
                "768/INITIAL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_EGYPT/ZOMBOSS_MISSILE_EXPLOSION_EGYPT.PAM",
                "animation", 1.2, 1.0f, missileTargetX, missileTargetY
            );
        }

        Map map = ctrl.getMap();
        if (map != null) {
            // Destroy the plant at target tile instantly
            Plant p = map.getPlantAt(missileTargetRow, missileTargetCol);
            if (p != null) {
                p.takeDamage(999999);
                map.removePlant(missileTargetRow, missileTargetCol);
                System.out.println("[ZombossEgypt] Missile destroyed plant at ("
                    + missileTargetRow + ", " + missileTargetCol + ")");
            }

            // Spawn 2 tombstones in 2 random available empty tiles on the lawn
            List<Tile> emptyTiles = new ArrayList<>();
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 8; c++) {
                    Tile tile = map.getTile(r, c);
                    if (tile != null && !tile.isGrave() && map.getPlantAt(r, c) == null) {
                        emptyTiles.add(tile);
                    }
                }
            }
            Collections.shuffle(emptyTiles);
            int tombstonesToSpawn = Math.min(2, emptyTiles.size());
            for (int i = 0; i < tombstonesToSpawn; i++) {
                Tile empty = emptyTiles.get(i);
                empty.setType(com.PVZ.model.enums.TileType.TOMBSTONE);
                empty.setGraveVariant(GraveVariant.EGYPT);
                empty.setHp(700);
            }
            System.out.println("[ZombossEgypt] Missile spawned " + tombstonesToSpawn + " tombstones!");
        }
    }

    // =========================================================================
    // 2. CHARGE / DASH FORWARD ATTACK
    // =========================================================================

    public void triggerChargeAttack(BattleController ctrl) {
        if (isStunned() || chargeState != ChargeState.IDLE) return;
        chargeState = ChargeState.WINDUP;
        chargeTimer = 0f;
        homeX = (float) this.x;
        ZombieAnimation.trigger(this, "jump_start", 1.0);
        com.PVZ.model.status.AppStatus.triggerCameraShake();
        System.out.println("[ZombossEgypt] Starting Charge / Dash Forward attack in lane " + this.getRow() + "!");
    }

    private void updateCharge(float delta, BattleController ctrl) {
        chargeTimer += delta;

        if (chargeState == ChargeState.WINDUP) {
            if (chargeTimer >= 0.8f) {
                chargeState = ChargeState.CHARGING_FORWARD;
                ZombieAnimation.trigger(this, "walk_forward", 4.0);
            }
        } else if (chargeState == ChargeState.CHARGING_FORWARD) {
            this.x -= chargeSpeed * delta;
            this.hitbox.setPosition((float) this.x, (float) this.y);

            // Crush all plants & enemy zombies in this row and adjacent occupied row
            crushEntitiesInPath(ctrl);

            if (this.x <= targetChargeX) {
                this.x = targetChargeX;
                chargeState = ChargeState.RETURNING;
                ZombieAnimation.trigger(this, "walk_backwards", 3.0);
            }
        } else if (chargeState == ChargeState.RETURNING) {
            this.x += chargeSpeed * 1.2f * delta;
            this.hitbox.setPosition((float) this.x, (float) this.y);

            if (this.x >= homeX) {
                this.x = homeX;
                this.hitbox.setPosition((float) this.x, (float) this.y);
                chargeState = ChargeState.IDLE;
                ZombieAnimation.trigger(this, "idle", 1.0);
                System.out.println("[ZombossEgypt] Charge complete. Returned to position.");
            }
        }
    }

    private void crushEntitiesInPath(BattleController ctrl) {
        int r1 = (int) this.getRow();
        int r2 = Math.min(4, r1 + 1);

        // 1. Crush plants in rows r1 and r2
        if (ctrl.getMap() != null) {
            for (int r : new int[]{r1, r2}) {
                for (int c = 0; c < 9; c++) {
                    Tile t = ctrl.getMap().getTile(r, c);
                    if (t != null && t.getX() >= this.x - 80f && t.getX() <= this.x + 180f) {
                        Plant p = ctrl.getMap().getPlantAt(r, c);
                        if (p != null && !p.isDead()) {
                            p.takeDamage(999999);
                            ctrl.getMap().removePlant(r, c);
                        }
                    }
                }
            }
        }

        // 2. Crush other regular zombies in front of Zomboss
        List<Zombie> zombies = ctrl.getAllZombies();
        if (zombies != null) {
            for (Zombie z : zombies) {
                if (z != this && !z.isDead() && (z.getRow() == r1 || z.getRow() == r2)) {
                    if (z.getX() >= this.x - 80f && z.getX() <= this.x + 100f) {
                        z.takeDamage(999999);
                    }
                }
            }
        }
    }

    // =========================================================================
    // 3. PORTAL WAVE SPAWN
    // =========================================================================

    @Override
    public void spawnZombieWave(BattleController ctrl) {
        triggerPortalSpawn(ctrl);
    }

    public void triggerPortalSpawn(BattleController ctrl) {
        if (ctrl == null) return;
        ZombieAnimation.trigger(this, "zombie_portal_start", 2.2667);

        String[] egyptTypes = currentPhase >= 3 ?
            new String[]{"ZombiePharaohDefault", "ZombieTombRaiserDefault",
                "ZombieMummyArmor2Default", "ZombieMummyArmor1Default"} :
            new String[]{"ZombieMummyDefault", "ZombieMummyArmor1Default", "ZombieMummyArmor2Default"};

        int count = 2 + currentPhase;
        float zombossX = (float) this.getX() - 80f;
        int zombossRow = (int) this.getRow();

        Tile tile = ctrl.getMap() != null ? ctrl.getMap().getTile(zombossRow, 7) : null;
        float spawnY = tile != null ? (tile.getY() + (tile.getHeight() - 70f) / 2f) : (float) this.getY();

        for (int i = 0; i < count; i++) {
            String type = egyptTypes[(int) (Math.random() * egyptTypes.length)];
            Zombie z = ZombieFactory.createZombie(type);
            if (z != null) {
                int spawnRow = Math.min(4, Math.max(0, zombossRow + (i % 2)));
                z.initPosition(zombossX - (i * 30f), spawnY, spawnRow);
                z.setRow(spawnRow);
                z.setCol(7);
                ctrl.addZombie(z);
            }
        }
        System.out.println("[ZombossEgypt] Portal Wave spawned " + count + " Egypt zombies!");
    }

    // =========================================================================
    // Getters for Renderer
    // =========================================================================

    public boolean isMissileActive() { return missileActive; }
    public float getMissileAnimTime() { return missileAnimTime; }
    public float getMissileTargetX() { return missileTargetX; }
    public float getMissileTargetY() { return missileTargetY; }
    public float getMissileCurrentY() { return missileCurrentY; }
    public ChargeState getChargeState() { return chargeState; }
}
