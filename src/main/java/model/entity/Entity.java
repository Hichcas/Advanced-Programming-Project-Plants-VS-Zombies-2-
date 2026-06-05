package model.entity;

public abstract class Entity {
      public double health;
      public double maxHealth;
      public boolean alive;
      public int x;                      // grid X (column)
      public int y;                      // grid Y (row)
      
      public Entity(double maxHealth) {
            this.maxHealth = maxHealth;
            this.health = maxHealth;
            this.alive = true;
      }
      
      public void takeDamage(int amount) {
            health -= amount;
            if (health <= 0) { health = 0; alive = false; }
      }
      
      public boolean isAlive() { return alive; }
}
