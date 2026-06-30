package com.PVZ.database;

import java.util.Optional;

public interface Database {
      //CRUD
      <T> void save(String key, T obj) throws Exception;
      <T> T load(String key, Class<T> type) throws Exception;
      void delete(String key) throws Exception;
      boolean contains(String key);
      
      // Vault management
      void createVault(String vaultId, String masterKey) throws Exception;
      void openVault(String vaultId, String masterKey) throws Exception;
      void closeVault() throws Exception;
      Optional<String> getOpenVaultId();
      
      // Disk
      void saveAllToDisk() throws Exception;
      void loadAllFromDisk() throws Exception;
      void clearAll();
}
