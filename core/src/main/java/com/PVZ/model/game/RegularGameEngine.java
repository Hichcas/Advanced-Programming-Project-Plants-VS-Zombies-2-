package com.PVZ.model.game;

import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Sun;
import com.PVZ.model.entity.plants.behavior.impl.Projectile;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class RegularGameEngine extends GameEngine{

    private final RegularZombieEngine zombieEngine;
    private List<Plant> plants = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Sun> suns = new ArrayList<>();
    private WaveManager waveManager;
    private BattleController battleController;

    public RegularGameEngine(GameStatus gameStatus) {
        super(gameStatus, new RegularInputProcessor());
        ((RegularInputProcessor)inputProcessor).setRegularGameEngine(this);

        this.zombieEngine = new RegularZombieEngine();
        battleController = new BattleController(zombieEngine.getZombies(), plants, projectiles, gameStatus);

        List<Wave> waves = new ArrayList<>();
        List<Wave.WaveEntry> e = new ArrayList<>();
        e.add(new Wave.WaveEntry("ZombieTutorialDefault", 5, 1.5f));
        waves.add(new Wave(e, 5f));
        waveManager = new WaveManager(waves);
    }

    @Override
    public void setMap(Map map) {
        super.setMap(map);
        zombieEngine.bindMap(map);
        battleController.setMap(map);
    }

    @Override
    public void update(float delta) {
        waveManager.update(delta, zombieEngine);
        battleController.update(delta);
    }

    @Override
    public void draw(SpriteBatch batch) {
        zombieEngine.draw(batch);
    }

    @Override
    public void dispose() {
        zombieEngine.dispose();
    }

    public RegularZombieEngine getZombieEngine() {
        return zombieEngine;
    }

    public BattleController getBattleController() {
        return battleController;
    }

    public void startWaves() {
        waveManager.start();
    }

}
