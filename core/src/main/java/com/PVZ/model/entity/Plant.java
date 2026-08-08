package com.PVZ.model.entity;

import com.PVZ.model.entity.plants.PlantDefinition;
import com.PVZ.model.entity.plants.PlantFactory;
import com.PVZ.model.entity.plants.PlantInstance;
import com.PVZ.model.entity.plants.PlantStats;
import com.PVZ.model.entity.plants.behavior.BehaviorContext;
import com.PVZ.model.entity.plants.behavior.BehaviorFactory;
import com.PVZ.model.entity.plants.behavior.PlantBehavior;
import com.PVZ.model.entity.plants.behavior.PlantFoodBehavior;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.enums.PlantType;
import com.PVZ.model.game.BattleController;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.Map;

public class Plant {
    private final PlantInstance instance;
    private final PlantBehavior mainBehavior;
    private final PlantFoodBehavior plantFoodBehavior;

    public Plant(PlantInstance instance) {
        if (instance == null) {
            throw new IllegalArgumentException("Plant instance cannot be null");
        }

        this.instance = instance;
        this.maxHp = instance.getCurrentHp();
        this.mainBehavior = BehaviorFactory.createMainBehavior(instance.getDefinition());
        this.plantFoodBehavior = BehaviorFactory.createPlantFoodBehavior(instance.getDefinition());
    }

    public static Plant of(PlantDefinition definition, int level) {
        return new Plant(PlantFactory.create(definition, level));
    }

    public PlantInstance getInstance() {
        return instance;
    }

    public PlantDefinition getDefinition() {
        return instance.getDefinition();
    }

    public PlantType getType() {
        return instance.getType();
    }

    public int getLevel() {
        return instance.getLevel();
    }

    public PlantStats getStats() {
        return instance.getStats();
    }

    public int getCurrentHp() {
        return instance.getCurrentHp();
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setCurrentHp(int currentHp) {
        instance.setCurrentHp(currentHp);
    }

    public boolean isDead() {
        return instance.isDead();
    }

    public void takeDamage(int amount) {
        instance.takeDamage(amount);
        if (amount > 0) {
            com.PVZ.model.entity.PlantAnimation.trigger(instance, "damage", 0.3);
        }
    }

    public void takeDamage(int amount, Zombie attacker, BattleController controller) {
        if (amount <= 0) {
            return;
        }
        instance.takeDamage(amount);
        com.PVZ.model.entity.PlantAnimation.trigger(instance, "damage", 0.3);
        mainBehavior.onDamaged(instance, controller, attacker, amount, instance.isDead());
    }

    public void heal(int amount) {
        instance.heal(amount);
    }

    public boolean isPlanted() {
        return instance.isPlanted();
    }

    public void setPlanted(boolean planted) {
        instance.setPlanted(planted);
    }

    public boolean isPlantFoodActive() {
        return instance.isPlantFoodActive();
    }

    public void setPlantFoodActive(boolean plantFoodActive) {
        instance.setPlantFoodActive(plantFoodActive);
    }

    public int getPlantFoodTicksRemaining() {
        return instance.getPlantFoodTicksRemaining();
    }

    public void setPlantFoodTicksRemaining(int ticks) {
        instance.setPlantFoodTicksRemaining(ticks);
    }

    public Map<String, Object> getRuntimeState() {
        return instance.getRuntimeState();
    }

    public void putRuntimeState(String key, Object value) {
        instance.putRuntimeState(key, value);
    }

    public Object getRuntimeState(String key) {
        return instance.getRuntimeState(key);
    }

    private com.badlogic.gdx.graphics.Texture bodyTexture;
    private boolean triedRealTexture = false;
    private final int maxHp;
    private float animStateTime = 0f;
    private String idleVariant = null;
    private double idleVariantTimer = 0.0;

    public void draw(SpriteBatch batch) {
        if (isDead()) {
            return;
        }
        Rectangle box = getHitbox();

        String key = getType() != null ? getType().name() : null;
        if (key != null) {
            com.PVZ.view.renderer.EntityRenderer renderer = com.PVZ.view.renderer.EntityRenderer.getInstance();
            float[] anchor = getVisualAnchor();
            float ax = anchor[0];
            float ay = anchor[1];
            boolean drewAnimated;
            if (isPlantFoodActive()) {
                drewAnimated = renderer.renderPlant(batch, key, "plantfood", animStateTime, ax, ay);
            } else if (com.PVZ.model.entity.PlantAnimation.isActive(instance)) {
                String state = com.PVZ.model.entity.PlantAnimation.getState(instance);
                drewAnimated = renderer.renderPlant(batch, key, state, animStateTime, ax, ay);
            } else if (idleVariant != null) {
                drewAnimated = renderer.renderPlantExact(batch, key, idleVariant, animStateTime, ax, ay);
                if (!drewAnimated) {
                    drewAnimated = renderer.renderPlant(batch, key, "idle", animStateTime, ax, ay);
                }
            } else {
                drewAnimated = renderer.renderPlant(batch, key, "idle", animStateTime, ax, ay);
            }
            if (drewAnimated) {
                return;
            }
        }

        if (bodyTexture == null) {
            bodyTexture = loadTexture();
        }
        batch.draw(bodyTexture, box.x, box.y, box.width, box.height);
    }

    private float[] getVisualAnchor() {
        Object wx = getRuntimeState("worldX");
        Object wy = getRuntimeState("worldY");
        Object tw = getRuntimeState("tileWidth");
        Object th = getRuntimeState("tileHeight");
        if (wx instanceof Number && wy instanceof Number) {
            float worldX = ((Number) wx).floatValue();
            float worldY = ((Number) wy).floatValue();
            float tileWidth = tw instanceof Number ? ((Number) tw).floatValue() : 100f;
            float tileHeight = th instanceof Number ? ((Number) th).floatValue() : 100f;
            float ax = worldX + tileWidth / 2f;
            float ay = worldY + (tileHeight - 70f) / 2f;
            return new float[]{ax, ay};
        }
        int row = asInt(getRuntimeState("row"), 0);
        int col = asInt(getRuntimeState("col"), 0);
        float tileSize = 100f;
        return new float[]{col * tileSize + tileSize / 2f, row * tileSize + (tileSize - 70f) / 2f};
    }

    private com.badlogic.gdx.graphics.Texture loadTexture() {
        if (!triedRealTexture) {
            triedRealTexture = true;
            String key = getType() != null ? getType().name() : null;
            if (key != null) {
                String path = PlantTexturePaths.getPath(key);
                try {
                    if (com.badlogic.gdx.Gdx.files.internal(path).exists()) {
                        com.badlogic.gdx.graphics.Texture tex =
                            new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal(path));
                        tex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
                            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
                        return tex;
                    }
                    System.out.println("[Plant] no icon found for " + key + " at assets/" + path
                        + " -> falling back to placeholder circle");
                } catch (RuntimeException ex) {
                    System.out.println("[Plant] failed loading texture for " + key + " at assets/" + path
                        + " -> " + ex.getMessage());
                }
            }
        }
        return buildBodyTexture();
    }

    private com.badlogic.gdx.graphics.Texture buildBodyTexture() {
        int w = 64;
        int h = 64;
        com.badlogic.gdx.graphics.Pixmap pixmap =
            new com.badlogic.gdx.graphics.Pixmap(w, h, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        int hash = getType() != null ? getType().name().hashCode() : 0;
        float r = 0.3f + ((hash & 0xFF) / 255f) * 0.6f;
        float g = 0.5f + (((hash >> 8) & 0xFF) / 255f) * 0.5f;
        float b = 0.2f + (((hash >> 16) & 0xFF) / 255f) * 0.4f;
        pixmap.setColor(r, g, b, 1f);
        pixmap.fillCircle(w / 2, h / 2, w / 2 - 2);
        pixmap.setColor(0f, 0f, 0f, 0.6f);
        pixmap.drawCircle(w / 2, h / 2, w / 2 - 2);
        com.badlogic.gdx.graphics.Texture tex = new com.badlogic.gdx.graphics.Texture(pixmap);
        tex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        pixmap.dispose();
        return tex;
    }

    public void disposeTexture() {
        if (bodyTexture != null) {
            bodyTexture.dispose();
            bodyTexture = null;
        }
        triedRealTexture = false;
    }

    public void update(BehaviorContext context, double deltaTimeSeconds) {
        animStateTime += (float) deltaTimeSeconds;
        com.PVZ.model.entity.PlantAnimation.tick(instance, deltaTimeSeconds);
        tickIdleVariant(deltaTimeSeconds);
        Object disabled = getRuntimeState("disabledTicks");
        int ticks = disabled instanceof Number ? ((Number) disabled).intValue() : 0;
        if (ticks > 0) {
            putRuntimeState("disabledTicks", Math.max(0, ticks - 1));
            instance.tickPlantFood();
            return;
        }
        mainBehavior.onUpdate(instance, context, deltaTimeSeconds);
        instance.tickPlantFood();
    }

    public void disableForTicks(int ticks) {
        Object disabled = getRuntimeState("disabledTicks");
        int cur = disabled instanceof Number ? ((Number) disabled).intValue() : 0;
        putRuntimeState("disabledTicks", Math.max(cur, ticks));
    }

    private void tickIdleVariant(double deltaTimeSeconds) {
        idleVariantTimer -= deltaTimeSeconds;
        if (idleVariant != null && idleVariantTimer > 0) {
            return;
        }
        idleVariant = pickRandomIdleVariant();
        idleVariantTimer = 3.0 + Math.random() * 4.0;
    }

    private String pickRandomIdleVariant() {
        String key = getType() != null ? getType().name() : null;
        if (key == null) {
            return null;
        }
        String pamPath = PlantTexturePaths.getPamPath(key);
        if (pamPath == null) {
            return null;
        }
        java.util.Set<String> names = com.PVZ.model.entity.PamAnimationCatalog.clipNames(pamPath);
        if (names == null) {
            return null;
        }
        java.util.List<String> idles = new java.util.ArrayList<>();
        for (String n : names) {
            String lower = n.toLowerCase();
            if (lower.startsWith("idle") && !lower.contains("damage")) {
                idles.add(n);
            }
        }
        if (idles.isEmpty()) {
            return null;
        }
        return idles.get((int) (Math.random() * idles.size()));
    }

    public void applyPlantFood(BehaviorContext context) {
        plantFoodBehavior.onPlantFood(instance, context);
    }

    public String getMainBehaviorId() {
        return instance.getMainBehaviorId();
    }

    public String getPlantFoodBehaviorId() {
        return instance.getPlantFoodBehaviorId();
    }

    public Rectangle getHitbox() {
        Object wx = getRuntimeState("worldX");
        Object wy = getRuntimeState("worldY");
        Object tw = getRuntimeState("tileWidth");
        Object th = getRuntimeState("tileHeight");
        if (wx instanceof Number && wy instanceof Number) {
            float width = tw instanceof Number ? ((Number) tw).floatValue() * 0.7f : 80f;
            float height = th instanceof Number ? ((Number) th).floatValue() * 0.7f : 80f;
            float x = ((Number) wx).floatValue() + (tw instanceof Number ? ((Number) tw).floatValue() * 0.15f : 0f);
            float y = ((Number) wy).floatValue() + (th instanceof Number ? ((Number) th).floatValue() * 0.15f : 0f);
            return new Rectangle(x, y, width, height);
        }
        int row = asInt(getRuntimeState("row"), 0);
        int col = asInt(getRuntimeState("col"), 0);
        return new Rectangle(col * 100f, row * 100f, 80, 80);
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
