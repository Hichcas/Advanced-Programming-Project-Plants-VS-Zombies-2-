package com.PVZ.model.entity.zombies.types.basic;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import com.PVZ.model.entity.zombies.base.ZombieArmor;
import com.PVZ.model.game.BattleController;

import java.util.ArrayList;
import java.util.List;

public class ZombiePharaoh extends AbstractBasicZombie {
    private boolean sarcophagusBroken;

    public ZombiePharaoh() {
        super("ZombiePharaohDefault", 400, 100, 0.12, 350, 3000, defaultScaledProps());
        this.sarcophagusBroken = false;
        this.armor = new ZombieArmor(ZombieArmor.ArmorType.SARCOPHAGUS, 600, false, false, false);
    }

    private static List<ScaledProperty> defaultScaledProps() {
        List<ScaledProperty> list = new ArrayList<>();
        list.add(new ScaledProperty("Hitpoints", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("EatDPS", ScaledProperty.Formula.STANDARD, 1.3, 0.05));
        list.add(new ScaledProperty("Speed", ScaledProperty.Formula.CONSTANT, 0, 0));
        list.add(new ScaledProperty("WavePointCost", ScaledProperty.Formula.CONSTANT, 0, 0));
        return list;
    }

//    @Override
//    public void onUpdate(double deltaTime) {
//        if (!sarcophagusBroken && armor != null && armor.isDestroyed()) {
//            sarcophagusBroken = true;
//            armor = null;
//            speed = 0.3;
//        }
//    }

    @Override
    public void onUpdate(float delta, BattleController controller) {
        if (!sarcophagusBroken && armor != null && armor.isDestroyed()) {
            sarcophagusBroken = true;
            armor = null;
            speed = 0.3;
            currentSpeed = 0.3;
            System.out.println(alias + " sarcophagus broken, speeding up");
        }
    }

    public boolean isSarcophagusBroken() { return sarcophagusBroken; }
}
