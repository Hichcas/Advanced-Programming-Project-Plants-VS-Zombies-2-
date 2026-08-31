package com.PVZ.view.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import pvz.libpvz.textures.TextureBank;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.pam.ClipRef;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.PVZ.model.entity.zombies.base.ZombieArmor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class EntityRenderer {

    private static EntityRenderer instance;

    private final TextureBank textures;
    private final PamPlayer pamPlayer;
    private final Map<String, ClipRef> zombieClips = new HashMap<>();
    private final Map<String, ClipRef> plantClips = new HashMap<>();
    private final Map<String, ClipRef> projectileClips = new HashMap<>();
    private final Map<String, ClipRef> genericPamClips = new HashMap<>();
    private final Map<String, Boolean> plantPamFailed = new HashMap<>();
    private final Map<String, Boolean> projectilePamFailed = new HashMap<>();
    private final Map<String, Boolean> genericPamFailed = new HashMap<>();
    private final java.util.Set<String> loggedPams = new java.util.HashSet<>();
    private final java.util.List<FallingArmorHelper.FallingArmorPiece> fallingArmors =
        new CopyOnWriteArrayList<>();

    private EntityRenderer() {
        com.badlogic.gdx.files.FileHandle assetsFolder =
            com.badlogic.gdx.Gdx.files.internal("assets/pvz-assets");
        if (!assetsFolder.exists()) {
            assetsFolder = com.badlogic.gdx.Gdx.files.internal("pvz-assets");
        }
        textures = new TextureBank("768", assetsFolder);
        pamPlayer = new PamPlayer(textures, assetsFolder);
    }

    public static EntityRenderer getInstance() {
        if (instance == null) {
            instance = new EntityRenderer();
        }
        return instance;
    }

    public void update() {
        textures.update();
    }

    // ---------- Zombie ----------
    public ClipRef getZombieClip(String alias, String state) {
        return ZombieRendering.getClip(this, alias, state);
    }

    public ClipRef getWalkClip(String alias) {
        return getZombieClip(alias, "walk");
    }

    public void renderZombie(SpriteBatch batch, Zombie zombie, float stateTime) {
        ZombieRendering.render(this, batch, zombie, stateTime);
    }

    public void renderZombieAlias(SpriteBatch batch, String alias, String state,
                                  float stateTime, float x, float y) {
        ZombieRendering.renderAlias(this, batch, alias, state, stateTime, x, y);
    }

    public void renderZombieAlias(SpriteBatch batch, String alias, String state,
                                  float stateTime, float x, float y, float scale) {
        ZombieRendering.renderAlias(this, batch, alias, state, stateTime, x, y, scale);
    }

    // ---------- Plant ----------
    public ClipRef getPlantClip(String plantTypeName, String state) {
        return PlantRendering.getClip(this, plantTypeName, state);
    }

    public ClipRef getPlantClip(String plantTypeName) {
        return getPlantClip(plantTypeName, "idle");
    }

    public ClipRef getPlantClipExact(String plantTypeName, String exactClipName) {
        return PlantRendering.getClipExact(this, plantTypeName, exactClipName);
    }

    public boolean renderPlantExact(SpriteBatch batch, String plantTypeName,
                                    String exactClipName, float stateTime,
                                    float x, float y) {
        return PlantRendering.renderExact(this, batch, plantTypeName,
            exactClipName, stateTime, x, y);
    }

    public boolean renderPlant(SpriteBatch batch, String plantTypeName,
                               String state, float stateTime, float x, float y) {
        return PlantRendering.render(this, batch, plantTypeName, state, stateTime, x, y);
    }

    public boolean renderPlant(SpriteBatch batch, String plantTypeName,
                               float stateTime, float x, float y) {
        return renderPlant(batch, plantTypeName, "idle", stateTime, x, y);
    }

    // ---------- Projectile ----------
    public boolean renderProjectile(SpriteBatch batch, String visualKey,
                                    float stateTime, float x, float y) {
        return ProjectileRendering.render(this, batch, visualKey, stateTime, x, y);
    }

    // ---------- Generic PAM ----------
    public boolean renderPam(SpriteBatch batch, String pamPath,
                             float stateTime, float x, float y) {
        return renderPam(batch, pamPath, null, stateTime, x, y);
    }

    public boolean renderPam(SpriteBatch batch, String pamPath, String clipName,
                             float stateTime, float x, float y) {
        return GenericPamRendering.render(this, batch, pamPath, clipName, stateTime, x, y);
    }

    public boolean renderPam(SpriteBatch batch, String pamPath, String clipName,
                             float stateTime, float x, float y, float scale) {
        return GenericPamRendering.renderScaled(this, batch, pamPath, clipName,
            stateTime, x, y, scale);
    }

    // ---------- Sun / Loot ----------
    public boolean renderSun(SpriteBatch batch, com.PVZ.model.entity.Sun.SunType type,
                             float animationTime, boolean falling,
                             boolean reachedGround, float x, float y) {
        return SunLootRendering.renderSun(this, batch, type, animationTime,
            falling, reachedGround, x, y);
    }

    public boolean renderLoot(SpriteBatch batch, com.PVZ.model.entity.LootDrop.LootType type,
                              float animationTime, float x, float y) {
        return SunLootRendering.renderLoot(this, batch, type, animationTime, x, y);
    }

    // ---------- Falling Armor ----------
    public void spawnFallingArmor(float x, float y, ZombieArmor.ArmorType type,
                                  String alias) {
        FallingArmorHelper.spawn(this, x, y, type, alias);
    }

    public void renderFallingArmors(SpriteBatch batch) {
        FallingArmorHelper.render(this, batch);
    }

    // ---------- Accessors ----------
    public TextureBank getTextures() { return textures; }
    public PamPlayer getPamPlayer() { return pamPlayer; }

    // Getters for caches (package-private)
    Map<String, ClipRef> zombieClips() { return zombieClips; }
    Map<String, ClipRef> plantClips() { return plantClips; }
    Map<String, ClipRef> projectileClips() { return projectileClips; }
    Map<String, ClipRef> genericPamClips() { return genericPamClips; }
    Map<String, Boolean> plantPamFailed() { return plantPamFailed; }
    Map<String, Boolean> projectilePamFailed() { return projectilePamFailed; }
    Map<String, Boolean> genericPamFailed() { return genericPamFailed; }
    java.util.Set<String> loggedPams() { return loggedPams; }
    java.util.List<FallingArmorHelper.FallingArmorPiece> fallingArmors() { return fallingArmors; }
}
