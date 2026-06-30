package com.PVZ.model.entity.zombies.base;

import com.PVZ.model.entity.zombies.base.ScaledProperty;
import java.util.List;

public abstract class Zombie {
      protected String alias;
      protected double hitpoints;
      protected double eatDPS;
      protected double speed;
      protected int wavePointCost;
      protected int weight;
      protected List<ScaledProperty> scaledProps;
      protected ZombieArmor armor;
      
      public Zombie(String alias, double hitpoints, double eatDPS, double speed,
                    int wavePointCost, int weight, List<ScaledProperty> scaledProps) {
            this.alias = alias;
            this.hitpoints = hitpoints;
            this.eatDPS = eatDPS;
            this.speed = speed;
            this.wavePointCost = wavePointCost;
            this.weight = weight;
            this.scaledProps = scaledProps;
            this.armor = null;
      }
      
      public void setArmor(ZombieArmor armor) {
            this.armor = armor;
      }
      
      public ZombieArmor getArmor() {
            return armor;
      }
      
      public double getEffectiveHitpoints() {
            double base = hitpoints;
            if (armor != null) {
                  base += armor.getHealth();
            }
            return base;
      }
      
      public abstract void onSpawn();
      public abstract void onUpdate(double deltaTime);
      public abstract void onDestroy();
      
      public String getAlias() { return alias; }
      public double getHitpoints() { return hitpoints; }
      public double getEatDPS() { return eatDPS; }
      public double getSpeed() { return speed; }
      public int getWavePointCost() { return wavePointCost; }
      public int getWeight() { return weight; }
}
