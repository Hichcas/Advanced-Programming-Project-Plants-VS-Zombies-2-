package com.PVZ.model.game;

import com.PVZ.model.entity.LawnMower;
import com.PVZ.model.entity.Plant;
import com.PVZ.model.entity.Projectile;
import com.PVZ.model.entity.Sun;
import com.PVZ.model.entity.zombies.base.Zombie;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private Map map;
    private List<Plant> plants = new ArrayList<>();
    private List<Zombie> zombies = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private List<Sun> suns = new ArrayList<>();
    private List<LawnMower> lawnMowers = new ArrayList<>();
    // managers: SunManager, WaveManager, ...

    public void update(float delta) { /* sunManager, plants, zombies, projectiles */ }
    public void render(SpriteBatch batch) {
        map.render(batch);
        for (Plant p : plants) p.draw(batch);
        for (Zombie z : zombies) z.draw(batch);
        for (Projectile p : projectiles) p.draw(batch);
        for (Sun s : suns) s.draw(batch);
        for (LawnMower m : lawnMowers) m.draw(batch);
    }
}
