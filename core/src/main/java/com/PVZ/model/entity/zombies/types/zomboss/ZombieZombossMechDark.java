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

public class ZombieZombossMechDark extends AbstractZomboss {

    // === Active Fire Breath State ===
    private boolean fireBreathActive = false;
    private float fireBreathTimer = 0f;

    // === Active Fireball Attack State ===
    public static class DarkFireball {
        public float startX, startY;
        public float currentX, currentY;
        public float targetX, targetY;
        public int targetRow, targetCol;
        public float progress; // 0.0 to 1.0
        public float speed;
        public boolean exploded;

        public DarkFireball(float startX, float startY, float targetX, float targetY, int targetRow, int targetCol) {
            this.startX = startX;
            this.startY = startY;
            this.currentX = startX;
            this.currentY = startY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetRow = targetRow;
            this.targetCol = targetCol;
            this.progress = 0f;
            this.speed = 1.4f;
            this.exploded = false;
        }
    }

    private final List<DarkFireball> activeFireballs = new ArrayList<>();
    private float fireballAnimTimer = 0f;

    public ZombieZombossMechDark() {
        super("ZombieZombossMechDark", 18000, 600, 0.1, 6000, 12000, defaultScaledProps(),
              0.33, 3, 7.0);
        this.hitbox.setSize(230f, 240f);
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
        this.hitbox.setSize(230f, 240f);
    }

    @Override
    public void onPhaseTransition(BattleController ctrl) {
        currentSpeed = speed * (1 + currentPhase * 0.25);
        abilityCooldown = Math.max(3.5, abilityCooldown - 1.0);
        triggerStun(4.0f);
        System.out.println("[ZombossDark] Dragon entered Phase " + currentPhase + "! Stunned for 4.0s");
    }

    @Override
    public void onUpdate(float delta, BattleController ctrl) {
        super.onUpdate(delta, ctrl);
        if (ctrl == null) return;

        // Update Stun State
        if (isStunned()) {
            fireBreathActive = false;
            activeFireballs.clear();
            return;
        }

        // Update Fire Breath
        if (fireBreathActive) {
            fireBreathTimer += delta;
            applyContinuousFireDamage(ctrl, delta);
            if (fireBreathTimer >= 3.2f) {
                fireBreathActive = false;
                ZombieAnimation.trigger(this, "idle", 1.0);
            }
        }

        // Update Fireballs
        if (!activeFireballs.isEmpty()) {
            fireballAnimTimer += delta;
            updateFireballs(delta, ctrl);
        }
    }

    @Override
    public void useSpecialAbility(BattleController ctrl) {
        if (isStunned() || fireBreathActive) return;

        double roll = Math.random();
        if (roll < 0.40) {
            triggerFireBreath(ctrl);
        } else if (roll < 0.75) {
            triggerFireballAttack(ctrl);
        } else {
            triggerSummonWave(ctrl);
        }
    }

    // =========================================================================
    // 1. 2-ROW FIRE BREATH ATTACK & SCORCHED TILES
    // =========================================================================

    public void triggerFireBreath(BattleController ctrl) {
        if (isStunned() || ctrl == null || ctrl.getMap() == null) return;
        fireBreathActive = true;
        fireBreathTimer = 0f;
        ZombieAnimation.trigger(this, "fire_attack", 3.2);
        com.PVZ.model.status.AppStatus.triggerCameraShake();

        int r1 = Math.max(0, Math.min(3, (int) this.getRow()));
        int r2 = r1 + 1;

        Map map = ctrl.getMap();
        for (int r : new int[]{r1, r2}) {
            for (int c = 0; c < map.getCols(); c++) {
                Tile t = map.getTile(r, c);
                if (t != null) {
                    // Ignite tile as Burnt / Scorch Tile for 6 seconds
                    t.setScorchTimer(6.0f);

                    // Destroy any plant on this tile
                    Plant p = map.getPlantAt(r, c);
                    if (p != null && !p.isDead()) {
                        p.takeDamage(999999);
                        map.removePlant(r, c);
                    }
                }
            }
        }

        System.out.println("[ZombossDark] Dragon breathed fire across rows "
            + r1 + " & " + r2 + "! Scorch tiles created.");
    }

    private void applyContinuousFireDamage(BattleController ctrl, float delta) {
        int r1 = Math.max(0, Math.min(3, (int) this.getRow()));
        int r2 = r1 + 1;
        Map map = ctrl.getMap();
        if (map == null) return;

        for (int r : new int[]{r1, r2}) {
            for (int c = 0; c < map.getCols(); c++) {
                Plant p = map.getPlantAt(r, c);
                if (p != null && !p.isDead()) {
                    p.takeDamage(999999);
                    map.removePlant(r, c);
                }
            }
        }
    }

    // =========================================================================
    // 2. FIREBALL ATTACK
    // =========================================================================

    public void triggerFireballAttack(BattleController ctrl) {
        if (isStunned() || ctrl == null || ctrl.getMap() == null) return;
        Map map = ctrl.getMap();

        ZombieAnimation.trigger(this, "fire_bomb", 2.5);
        fireballAnimTimer = 0f;

        // Choose 1 to 3 targets
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

        int count = Math.min(3, 1 + currentPhase);
        float startX = (float) this.x + 30f;
        float startY = (float) this.y + 110f;

        for (int i = 0; i < count; i++) {
            int[] target;
            if (!plantTiles.isEmpty()) {
                int idx = (int) (Math.random() * plantTiles.size());
                target = plantTiles.remove(idx);
            } else if (!allTiles.isEmpty()) {
                int idx = (int) (Math.random() * allTiles.size());
                target = allTiles.remove(idx);
            } else {
                break;
            }

            Tile t = map.getTile(target[0], target[1]);
            float tx = t != null ? t.getX() + t.getWidth() / 2f : 800f;
            float ty = t != null ? t.getY() + t.getHeight() / 2f : 400f;

            DarkFireball fb = new DarkFireball(startX, startY, tx, ty, target[0], target[1]);
            activeFireballs.add(fb);
        }

        System.out.println("[ZombossDark] Launched " + activeFireballs.size() + " Dark Fireballs!");
    }

    private void updateFireballs(float delta, BattleController ctrl) {
        List<DarkFireball> toRemove = new ArrayList<>();
        for (DarkFireball fb : activeFireballs) {
            fb.progress += fb.speed * delta;
            if (fb.progress >= 1.0f) {
                fb.progress = 1.0f;
                onFireballImpact(fb, ctrl);
                toRemove.add(fb);
            } else {
                fb.currentX = fb.startX + (fb.targetX - fb.startX) * fb.progress;
                // Parabolic arc:
                float heightOffset = 180f * (float) Math.sin(fb.progress * Math.PI);
                fb.currentY = fb.startY + (fb.targetY - fb.startY) * fb.progress + heightOffset;
            }
        }
        activeFireballs.removeAll(toRemove);
    }

    private void onFireballImpact(DarkFireball fb, BattleController ctrl) {
        if (fb.exploded) return;
        fb.exploded = true;
        com.PVZ.model.status.AppStatus.triggerCameraShake();

        if (ctrl.getEngine() != null) {
            ctrl.getEngine().addTimedPamEffect(
                "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM",
                "idle2", 1.2, 1.0f, fb.targetX, fb.targetY
            );
        }

        Map map = ctrl.getMap();
        if (map != null) {
            Tile t = map.getTile(fb.targetRow, fb.targetCol);
            if (t != null) {
                t.setScorchTimer(4.5f);
            }
            Plant p = map.getPlantAt(fb.targetRow, fb.targetCol);
            if (p != null) {
                p.takeDamage(999999);
                map.removePlant(fb.targetRow, fb.targetCol);
                System.out.println("[ZombossDark] Fireball destroyed plant at ("
                    + fb.targetRow + ", " + fb.targetCol + ")");
            }
        }
    }

    // =========================================================================
    // 3. SUMMON DARK AGES WAVE
    // =========================================================================

    @Override
    public void spawnZombieWave(BattleController ctrl) {
        triggerSummonWave(ctrl);
    }

    public void triggerSummonWave(BattleController ctrl) {
        if (ctrl == null) return;
        ZombieAnimation.trigger(this, "summoning", 2.5);

        String[] darkTypes = currentPhase >= 3 ?
            new String[]{"ZombieDarkArmor3Default", "ZombieWizardDefault",
                "ZombieDarkKingDefault", "ZombieDarkImpDragonDefault", "ZombieDarkJugglerDefault"} :
            new String[]{"ZombieDarkArmor1Default", "ZombieDarkArmor2Default",
                "ZombieDarkImpDragonDefault", "ZombieDarkJugglerDefault"};

        int count = 2 + currentPhase;
        float zombossX = (float) this.getX() - 80f;
        int zombossRow = (int) this.getRow();

        Tile tile = ctrl.getMap() != null ? ctrl.getMap().getTile(zombossRow, 7) : null;
        float spawnY = tile != null ? (tile.getY() + (tile.getHeight() - 70f) / 2f) : (float) this.getY();

        for (int i = 0; i < count; i++) {
            String type = darkTypes[(int) (Math.random() * darkTypes.length)];
            Zombie z = ZombieFactory.createZombie(type);
            if (z != null) {
                int spawnRow = Math.min(4, Math.max(0, zombossRow + (i % 2)));
                z.initPosition(zombossX - (i * 35f), spawnY, spawnRow);
                z.setRow(spawnRow);
                z.setCol(7);
                ctrl.addZombie(z);
            }
        }
        System.out.println("[ZombossDark] Summoned " + count + " Dark Ages zombies!");
    }

    // =========================================================================
    // Getters for Renderer
    // =========================================================================

    public boolean isFireBreathActive() { return fireBreathActive; }
    public float getFireBreathTimer() { return fireBreathTimer; }
    public List<DarkFireball> getActiveFireballs() { return activeFireballs; }
    public float getFireballAnimTimer() { return fireballAnimTimer; }
}
