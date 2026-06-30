package com.PVZ.database;
//this file is just a helper file

/**
 * ========================================================================
 *                         DATABASE ARCHITECTURE
 * ========================================================================
 *
 *   ┌────────────────────────────────────────────────────────────┐
 *   │                     FILES ON DISK
 *   ├────────────────────────────────────────────────────────────┤
 *   │  vaults.enc
 *   │     stores: vaultId -> hash(masterKey)
 *   │     encrypted with: AUTH_KEY (static 16 bytes)
 *   │
 *   │  database.enc
 *   │     stores: Map< key, DatabaseEntry>
 *   │     encrypted with: dataKey (derived from masterKey)
 *   │
 *   │  session.dat
 *   │     stores: openVaultId
 *   │     encrypted with: AUTH_KEY (static 16 bytes)
 *   └────────────────────────────────────────────────────────────┘
 *
 *   ┌────────────────────────────────────────────────────────────┐
 *   │                DatabaseEntry Structure
 *   ├────────────────────────────────────────────────────────────┤
 *   │  {
 *   │    "className": "database.User",    <- original class name
 *   │    "jsonData":  "{\"username\":...}" <- the object's JSON
 *   │  }
 *   └────────────────────────────────────────────────────────────┘
 *
 *   ┌────────────────────────────────────────────────────────────┐
 *   │                     ENCRYPTION KEYS
 *   ├────────────────────────────────────────────────────────────┤
 *   │  AUTH_KEY = "0123456789ABCDEF"  (static, 16 bytes)
 *   │    -> used for vaults.enc and session.dat
 *   │
 *   │  dataKey = SHA-256(masterKey).substring(0, 16)
 *   │    -> used for database.enc (each vault has its own key)
 *   └────────────────────────────────────────────────────────────┘
 *
 *   ┌────────────────────────────────────────────────────────────┐
 *   │                        FLOW
 *   ├────────────────────────────────────────────────────────────┤
 *   │
 *   │  createVault("ali", "ali123")
 *   │    +-> vaults.enc   <- hash("ali123")
 *   │    +-> database.enc <- {} (empty)
 *   │
 *   │  save("user_ali", user)
 *   │    +-> database.enc <- {"user_ali": DatabaseEntry{...}}
 *   │
 *   │  closeVault()
 *   │    +-> session.dat deleted
 *   │
 *   │  openVault("ali", "ali123")
 *   │    +-> vaults.enc:  hash("ali123") == stored
 *   │    +-> database.enc: decrypt -> load
 *   │
 *   │  load("user_ali", User.class)
 *   │    +-> entry.jsonData -> readValue -> User
 *   │
 *   └────────────────────────────────────────────────────────────┘
 */
public class DatabaseUsageGuide {

      /**
       * ========================================================================
       *  1. Get the singleton instance (call once at startup)
       * ========================================================================
       */
      public void step1_getInstance() {
            DatabaseManager db = DatabaseManager.getInstance();
      }

      /**
       * ========================================================================
       *  2. Create a vault and save a User object
       * ========================================================================
       */
      public void step2_saveUser() throws Exception {
            DatabaseManager db = DatabaseManager.getInstance();

            db.createVault("ali", "ali123");

            String hashedPw = EncryptionEngine.hash("ali123");
            db.save("ali_profile", new User("ali", hashedPw, 8500, true));
      }

      /**
       * ========================================================================
       *  3. Save a GameProgress object (different fields)
       * ========================================================================
       */
      public void step3_saveGameProgress() throws Exception {
            DatabaseManager db = DatabaseManager.getInstance();
            db.createVault("ali", "ali123");

            GameProgress progress = new GameProgress(
                  12, 45.5, new String[]{"peashooter", "sunflower", "walnut"}
            );
            db.save("progress_ali", progress);
      }

      /**
       * ========================================================================
       *  4. Load with explicit class (automatic cast)
       * ========================================================================
       */
      public void step4_loadWithClass() throws Exception {
            DatabaseManager db = DatabaseManager.getInstance();
            db.openVault("ali", "ali123");

            User user = db.load("ali_profile", User.class);
            GameProgress progress = db.load("progress_ali", GameProgress.class);
      }

      /**
       * ========================================================================
       *  5. Load without class (the database detects the type)
       * ========================================================================
       */
      public void step5_loadWithoutClass() throws Exception {
            DatabaseManager db = DatabaseManager.getInstance();
            db.openVault("ali", "ali123");

            Object obj = db.load("ali_profile");
            User user = (User) obj;
      }

      /**
       * ========================================================================
       *  6. Check if key exists + delete
       * ========================================================================
       */
      public void step6_checkAndDelete() throws Exception {
            DatabaseManager db = DatabaseManager.getInstance();
            db.openVault("ali", "ali123");

            if (db.contains("ali_profile")) {
                  System.out.println("ali_profile exists");
            }

            db.delete("ali_profile");
      }

      /**
       * ========================================================================
       *  7. Close and reopen a vault
       * ========================================================================
       */
      public void step7_closeAndReopen() throws Exception {
            DatabaseManager db = DatabaseManager.getInstance();

            db.createVault("ali", "ali123");
            db.save("ali_profile", new User("ali", "", 100, false));
            db.closeVault();

            db.openVault("ali", "ali123");
            User user = db.load("ali_profile", User.class);
            db.closeVault();
      }

      /**
       * ========================================================================
       *  8. Hash a password with SHA-256
       * ========================================================================
       */
      public void step8_hashPassword() throws Exception {
            String hash = EncryptionEngine.hash("ali123");
            System.out.println(hash);

            String input = "ali123";
            boolean match = hash.equals(EncryptionEngine.hash(input));
            System.out.println(match);
      }

      /**
       * ========================================================================
       *  9. Clear all data
       * ========================================================================
       */
      public void step9_clearAll() {
            DatabaseManager db = DatabaseManager.getInstance();
            db.clearAll();
      }
}




class User {
      private String username;
      private String passwordHash;
      private int highScore;
      private boolean isVip;
      
      public User() {}
      
      public User(String username, String passwordHash, int highScore, boolean isVip) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.highScore = highScore;
            this.isVip = isVip;
      }
      
      // getters & setters
      public String getUsername() { return username; }
      public void setUsername(String username) { this.username = username; }
      public String getPasswordHash() { return passwordHash; }
      public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
      public int getHighScore() { return highScore; }
      public void setHighScore(int highScore) { this.highScore = highScore; }
      public boolean isVip() { return isVip; }
      public void setVip(boolean vip) { isVip = vip; }
      
      @Override
      public String toString() {
            return "User{username='" + username + "', highScore=" + highScore + ", vip=" + isVip + "}";
      }
}
class GameProgress {
      private int level;
      private double playTimeHours;
      private String[] unlockedPlants;
      
      public GameProgress() {}
      
      public GameProgress(int level, double playTimeHours, String[] unlockedPlants) {
            this.level = level;
            this.playTimeHours = playTimeHours;
            this.unlockedPlants = unlockedPlants;
      }
      
      // getters & setters
      public int getLevel() { return level; }
      public void setLevel(int level) { this.level = level; }
      public double getPlayTimeHours() { return playTimeHours; }
      public void setPlayTimeHours(double playTimeHours) { this.playTimeHours = playTimeHours; }
      public String[] getUnlockedPlants() { return unlockedPlants; }
      public void setUnlockedPlants(String[] unlockedPlants) { this.unlockedPlants = unlockedPlants; }
      
      @Override
      public String toString() {
            return "GameProgress{level=" + level + ", hours=" + playTimeHours + "}";
      }
}
