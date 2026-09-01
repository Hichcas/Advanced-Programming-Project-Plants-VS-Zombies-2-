package com.PVZ.model.entity.zombies.types.zomboss;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Tile;
import com.PVZ.model.enums.TileType;
import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieAnimation;
import com.PVZ.model.entity.zombies.factory.ZombieFactory;
import com.PVZ.model.game.BattleController;
import com.PVZ.model.game.Map;

import java.util.ArrayList;
import java.util.List;

public class ZombieZombossMechIceAge extends AbstractZomboss {

    // === Ice Wind Breath State ===
    private boolean iceWindActive = false;
    private float iceWindTimer = 0f;

    // === Ice Missile Attack State ===
    public static class IceMissile {
        public float startX, startY;
        public float currentX, currentY;
        public float targetX, targetY;
        public int targetRow, targetCol;
        public float progress; // 0.0 to 1.0
        public float speed;
        public boolean exploded;

        public IceMissile(float startX, float startY, float targetX, float targetY, int targetRow, int targetCol) {
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

    private final List<IceMissile> activeMissiles = new ArrayList<>();
    private float missileAnimTimer = 0f;

    public ZombieZombossMechIceAge() {
        super("ZombieZombossMechIceAge", 17000, 600, 0.1, 6000, 12000, defaultScaledProps(),
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
        System.out.println("[ZombossIceAge] Mammoth entered Phase " + currentPhase + "! Stunned for 4.0s");
    }

    @Override
    public void onUpdate(float delta, BattleController ctrl) {
        super.onUpdate(delta, ctrl);
        if (ctrl == null) return;

        // Update Stun State
        if (isStunned()) {
            iceWindActive = false;
            activeMissiles.clear();
            return;
        }

        // Update Ice Wind Breath
        if (iceWindActive) {
            iceWindTimer += delta;
            applyIceWindFreezing(ctrl, delta);
            if (iceWindTimer >= 3.2f) {
                iceWindActive = false;
                ZombieAnimation.trigger(this, "idle", 1.0);
            }
        }

        // Update Ice Missiles
        if (!activeMissiles.isEmpty()) {
            missileAnimTimer += delta;
            updateIceMissiles(delta, ctrl);
        }
    }

    @Override
    public void useSpecialAbility(BattleController ctrl) {
        if (isStunned() || iceWindActive) return;

        double roll = Math.random();
        if (roll < 0.40) {
            triggerIceWindBreath(ctrl);
        } else if (roll < 0.75) {
            triggerIceMissileAttack(ctrl);
        } else {
            triggerGlacierSummon(ctrl);
        }
    }

    // =========================================================================
    // 1. MAMMOTH ICE WIND BREATH (15 pts logic + 15 pts animation)
    // =========================================================================

    public void triggerIceWindBreath(BattleController ctrl) {
        if (isStunned() || ctrl == null || ctrl.getMap() == null) return;
        iceWindActive = true;
        iceWindTimer = 0f;
        ZombieAnimation.trigger(this, "wind_1", 3.2);
        com.PVZ.model.status.AppStatus.triggerCameraShake();

        int r1 = Math.max(0, Math.min(3, (int) this.getRow()));
        int r2 = r1 + 1;

        Map map = ctrl.getMap();
        for (int r : new int[]{r1, r2}) {
            for (int c = 0; c < map.getCols(); c++) {
                Tile t = map.getTile(r, c);
                if (t != null && t.getType() == TileType.NORMAL) {
                    t.setType(TileType.ICE);
                    t.setHp(600);
                }
                Plant p = map.getPlantAt(r, c);
                if (p != null && !p.isDead()) {
                    p.putRuntimeState("freezeLevel", 3);
                    p.takeDamage(400);
                }
            }
        }

        System.out.println("[ZombossIceAge] Mammoth unleashed Ice Wind Blizzard across rows " + r1 + " & " + r2 + "!");
    }

    private void applyIceWindFreezing(BattleController ctrl, float delta) {
        int r1 = Math.max(0, Math.min(3, (int) this.getRow()));
        int r2 = r1 + 1;
        Map map = ctrl.getMap();
        if (map == null) return;

        for (int r : new int[]{r1, r2}) {
            for (int c = 0; c < map.getCols(); c++) {
                Plant p = map.getPlantAt(r, c);
                if (p != null && !p.isDead()) {
                    p.putRuntimeState("freezeLevel", 3);
                }
            }
        }
    }

    // =========================================================================
    // 2. MAMMOTH ICE MISSILES (15 pts logic + 15 pts animation)
    // =========================================================================

    public void triggerIceMissileAttack(BattleController ctrl) {
        if (isStunned() || ctrl == null || ctrl.getMap() == null) return;
        Map map = ctrl.getMap();

        ZombieAnimation.trigger(this, "slingshot", 2.5);
        missileAnimTimer = 0f;

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
        float startX = (float) this.x + 20f;
        float startY = (float) this.y + 120f;

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

            IceMissile im = new IceMissile(startX, startY, tx, ty, target[0], target[1]);
            activeMissiles.add(im);
        }

        System.out.println("[ZombossIceAge] Launched " + activeMissiles.size() + " Mammoth Ice Missiles!");
    }

    private void updateIceMissiles(float delta, BattleController ctrl) {
        List<IceMissile> toRemove = new ArrayList<>();
        for (IceMissile im : activeMissiles) {
            im.progress += im.speed * delta;
            if (im.progress >= 1.0f) {
                im.progress = 1.0f;
                onIceMissileImpact(im, ctrl);
                toRemove.add(im);
            } else {
                im.currentX = im.startX + (im.targetX - im.startX) * im.progress;
                float heightOffset = 190f * (float) Math.sin(im.progress * Math.PI);
                im.currentY = im.startY + (im.targetY - im.startY) * im.progress + heightOffset;
            }
        }
        activeMissiles.removeAll(toRemove);
    }

    private void onIceMissileImpact(IceMissile im, BattleController ctrl) {
        if (im.exploded) return;
        im.exploded = true;
        com.PVZ.model.status.AppStatus.triggerCameraShake();

        if (ctrl.getEngine() != null) {
            ctrl.getEngine().addTimedPamEffect(
                "768/FULL/EFFECTS/ICESHROOM_FX/ICESHROOM_FX.PAM",
                "animation", 1.2, 1.0f, im.targetX, im.targetY
            );
        }

        Map map = ctrl.getMap();
        if (map != null) {
            Tile t = map.getTile(im.targetRow, im.targetCol);
            if (t != null && t.getType() == TileType.NORMAL) {
                t.setType(TileType.ICE);
                t.setHp(600);
            }
            Plant p = map.getPlantAt(im.targetRow, im.targetCol);
            if (p != null) {
                p.takeDamage(999999);
                map.removePlant(im.targetRow, im.targetCol);
                System.out.println("[ZombossIceAge] Ice Missile crushed plant at ("
                    + im.targetRow + ", " + im.targetCol + ")");
            }
        }
    }

    // =========================================================================
    // 3. GLACIER ENCASED ZOMBIES & ICE AGE WAVE (25 pts + 15 pts animation)
    // =========================================================================

    @Override
    public void spawnZombieWave(BattleController ctrl) {
        triggerGlacierSummon(ctrl);
    }

    public void triggerGlacierSummon(BattleController ctrl) {
        if (ctrl == null) return;
        ZombieAnimation.trigger(this, "glacier_column_intro", 2.5);
        String[] iceTypes = currentPhase >= 3 ?
            new String[]{"ZombieIceAgeHunter", "ZombieIceAgeDodo",
                "ZombieWeaselHoarderDefault", "ZombieIceAgeArmor2Default", "ZombieIceAgeImpDefault"} :
            new String[]{"ZombieIceAgeDefault", "ZombieIceAgeDodo",
                "ZombieIceAgeImpDefault", "ZombieIceAgeArmor1Default"};
        int count = 2 + currentPhase;
        float zombossX = (float) this.getX() - 80f;
        int zombossRow = (int) this.getRow();
        Tile tile = ctrl.getMap() != null ? ctrl.getMap().getTile(zombossRow, 7) : null;
        float spawnY = tile != null ? (tile.getY() + (tile.getHeight() - 70f) / 2f) : (float) this.getY();
        for (int i = 0; i < count; i++) {
            String type = iceTypes[(int) (Math.random() * iceTypes.length)];
            Zombie z = ZombieFactory.createZombie(type);
            if (z != null) {
                int spawnRow = Math.min(4, Math.max(0, zombossRow + (i % 2)));
                z.initPosition(zombossX - (i * 35f), spawnY, spawnRow);
                z.setRow(spawnRow);
                z.setCol(7);
                ctrl.addZombie(z);}}if (ctrl.getMap() != null) {
            int numBlocks = 1 + (currentPhase > 1 ? 1 : 0);
            for (int b = 0; b < numBlocks; b++) {
                int r = Math.min(4, Math.max(0, zombossRow + (b % 2)));
                int c = 3 + (int)(Math.random() * 4); // Forward columns 3..6
                Tile t = ctrl.getMap().getTile(r, c);
                if (t != null) {
                    Plant p = ctrl.getMap().getPlantAt(r, c);
                    if (p != null) {
                        p.takeDamage(999999);
                        ctrl.getMap().removePlant(r, c);}
                    t.setType(TileType.ICE);t.setHp(600);t.setMaxHp(600);
                    String[] encasedOptions = new String[]{"ZombieIceAgeDefault",
                        "ZombieIceAgeDodo", "ZombieIceAgeHunter"};
                    t.setEncasedZombieType(encasedOptions[(int)(Math.random() * encasedOptions.length)]);
                    if (ctrl.getEngine() != null) {
                        ctrl.getEngine().addTimedPamEffect(
                            "768/FULL/EFFECTS/ICESHROOM_FX/ICESHROOM_FX.PAM",
                            "animation", 1.2, 1.0f, t.getX() +
                                t.getWidth() / 2f, t.getY() + t.getHeight() / 2f);}}}}
        System.out.println("[ZombossIceAge] Summoned " + count + " Ice Age zombies and Encased Ice Zombies!");
    }

    // =========================================================================
    // Getters for Renderer
    // =========================================================================

    public boolean isIceWindActive() { return iceWindActive; }
    public float getIceWindTimer() { return iceWindTimer; }
    public List<IceMissile> getActiveMissiles() { return activeMissiles; }
    public float getMissileAnimTimer() { return missileAnimTimer; }
}
