package com.PVZ.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;


public class DatabaseManager implements Database {
      private static volatile DatabaseManager instance;
      private final Map<String, DatabaseEntry> storage = new ConcurrentHashMap<>();
      private final Map<String, String> vaultIndex = new HashMap<>(); // vaultId → masterKeyHash
      private final ObjectMapper mapper;
      private final File dbFile = new File("database.enc");
      private final File vaultFile = new File("vaults.enc");
      private final File sessionFile = new File("session.dat");
      private String openVaultId;
      private boolean isUnlocked = false;
      
      private DatabaseManager() {
            this.mapper = new ObjectMapper();
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            loadVaultIndex();
      }
      
      public static DatabaseManager getInstance() {
            if (instance == null) {
                  synchronized (DatabaseManager.class) {
                        if (instance == null) instance = new DatabaseManager();
                  }
            }
            return instance;
      }
      
      // ============ Vault ============
      @Override
      public void createVault(String vaultId, String masterKey) throws Exception {
            if (vaultIndex.containsKey(vaultId)) {
                  System.err.println("Error: Vault '" + vaultId + "' already exists");
                  return;                              // ← throw → print + return
            }
            
            String hashedKey = EncryptionEngine.hash(masterKey);
            vaultIndex.put(vaultId, hashedKey);
            saveVaultIndex();
            
            EncryptionEngine.initDataKey(masterKey);
            isUnlocked = true;
            openVaultId = vaultId;
            storage.clear();
            saveSessionToDisk();
            saveAllToDisk();
      }
      
      @Override
      public void openVault(String vaultId, String masterKey) throws Exception {
            String storedHash = vaultIndex.get(vaultId);
            if (storedHash == null) {
                  System.err.println("Error: Vault '" + vaultId + "' not found");
                  return;                              // ← throw → print + return
            }
            if (!storedHash.equals(EncryptionEngine.hash(masterKey))) {
                  System.err.println("Error: Wrong master key");
                  return;                              // ← throw → print + return
            }
            EncryptionEngine.initDataKey(masterKey);
            isUnlocked = true;
            storage.clear();
            loadAllFromDisk();
            openVaultId = vaultId;
            saveSessionToDisk();
      }
      
      @Override
      public void closeVault() throws Exception {
            openVaultId = null;
            isUnlocked = false;
            storage.clear();
            // Reset dataKey to default
            EncryptionEngine.initDataKey("DefaultDataKey16");
            Files.deleteIfExists(sessionFile.toPath());
      }
      
      @Override
      public Optional<String> getOpenVaultId() {
            return Optional.ofNullable(openVaultId);
      }
      
      // ============ CRUD ============
      @Override
      public <T> void save(String key, T obj) throws Exception {
            if (!isUnlocked) {
                  System.err.println("Error: Open a vault first");
                  return;                              // ← throw → print + return
            }
            String className = obj.getClass().getName();
            String jsonData = mapper.writeValueAsString(obj);
            storage.put(key, new DatabaseEntry(className, jsonData));
            saveAllToDisk();
      }
      
      @Override
      public <T> T load(String key, Class<T> type) throws Exception {
            if (!isUnlocked) {
                  System.err.println("Error: Open a vault first");
                  return null;                         // ← throw → print + return null
            }
            DatabaseEntry entry = storage.get(key);
            if (entry == null) return null;
            return mapper.readValue(entry.getJsonData(), type);
      }
      
      public Object load(String key) throws Exception {
            DatabaseEntry entry = storage.get(key);
            if (entry == null) return null;
            Class<?> clazz = Class.forName(entry.getClassName());
            return mapper.readValue(entry.getJsonData(), clazz);
      }
      
      @Override
      public void delete(String key) throws Exception {
            if (!isUnlocked) {
                  System.err.println("Error: Open a vault first");
                  return;
            }
            storage.remove(key);
            saveAllToDisk();
      }
      
      @Override
      public boolean contains(String key) {
            return storage.containsKey(key);
      }
      
      // ============ Disk I/O ============
      @Override
      public synchronized void saveAllToDisk() throws Exception {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(storage);
            String encrypted = EncryptionEngine.encrypt(json);
            if (!json.equals(EncryptionEngine.decrypt(encrypted))) {
                  System.err.println("Error: Encryption verification failed — data not saved");
                  return;                              // ← throw → print + return
            }
            
            Files.writeString(dbFile.toPath(), encrypted);
      }
      
      @Override
      public void loadAllFromDisk() throws Exception {
            if (!dbFile.exists()) return;
            String encrypted = Files.readString(dbFile.toPath());
            String json = EncryptionEngine.decrypt(encrypted);
            Map<String, DatabaseEntry> loaded = mapper.readValue(json,
                  new TypeReference<HashMap<String, DatabaseEntry>>() {});
            storage.putAll(loaded);
      }
      
      private void saveVaultIndex() throws Exception {
            String json = mapper.writeValueAsString(vaultIndex);
            String encrypted = EncryptionEngine.encryptAuth(json);
            if (!json.equals(EncryptionEngine.decryptAuth(encrypted))) {
                  System.err.println("Error: Vault index verification failed — not saved");
                  return;                              // ← throw → print + return
            }
            
            Files.writeString(vaultFile.toPath(), encrypted);
      }
      
      private void loadVaultIndex() {
            if (!vaultFile.exists()) return;
            try {
                  String encrypted = Files.readString(vaultFile.toPath());
                  String json = EncryptionEngine.decryptAuth(encrypted);
                  Map<String, String> loaded = mapper.readValue(json,
                        new TypeReference<HashMap<String, String>>() {});
                  vaultIndex.putAll(loaded);
            } catch (Exception e) {
                  System.err.println("Corrupted vault index: " + e.getMessage());
            }
      }
      
      private void saveSessionToDisk() throws Exception {
            if (openVaultId == null) return;
            String encrypted = EncryptionEngine.encryptAuth(openVaultId);
            Files.writeString(sessionFile.toPath(), encrypted);
      }
      
      
      @Override
      public void clearAll() {
            storage.clear();
            vaultIndex.clear();
            openVaultId = null;
            isUnlocked = false;
            try {
                  Files.deleteIfExists(dbFile.toPath());
                  Files.deleteIfExists(vaultFile.toPath());
                  Files.deleteIfExists(sessionFile.toPath());
            } catch (IOException ignored) {}
      }
}
