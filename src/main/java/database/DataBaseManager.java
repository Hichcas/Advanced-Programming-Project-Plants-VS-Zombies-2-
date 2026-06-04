/**
 * فاطمه جان اینو بخون و کلا درستش کن ببین چی به چیه
 */



package database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager implements DatabaseConnection {
    private static volatile DatabaseManager instance;
    private final Map<String, DatabaseRecord> records = new ConcurrentHashMap<>();
    private final Path mainDbFile = Paths.get("database.db");
    private final Path sessionFile = Paths.get("session.db");
    private final Gson gson = new Gson();
    private DatabaseRecord activeSession;   // می‌تواند null باشد

    private DatabaseManager() {
        loadMainDatabase();
        loadSession();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    // ==================== متدهای کمکی برای I/O ====================
    private void loadMainDatabase() {
        if (Files.exists(mainDbFile)) {
            try {
                String encryptedContent = Files.readString(mainDbFile);
                String json = EncryptionEngine.decrypt(encryptedContent);
                Type listType = new TypeToken<List<DatabaseRecord>>() {}.getType();
                List<DatabaseRecord> loadedRecords = gson.fromJson(json, listType);
                if (loadedRecords != null) {
                    for (DatabaseRecord rec : loadedRecords) {
                        records.put(rec.getUsername(), rec);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading main database: " + e.getMessage());
            }
        }
    }

    private void loadSession() {
        if (Files.exists(sessionFile)) {
            try {
                String encryptedContent = Files.readString(sessionFile);
                String json = EncryptionEngine.decrypt(encryptedContent);
                activeSession = gson.fromJson(json, DatabaseRecord.class);
            } catch (IOException e) {
                System.err.println("Error loading session: " + e.getMessage());
            }
        }
    }

    // ==================== CRUD عمومی ====================
    @Override
    public void addRecord(DatabaseRecord record) {
        records.put(record.getUsername(), record);
    }

    @Override
    public Optional<DatabaseRecord> getUser(String username) {
        return Optional.ofNullable(records.get(username));
    }

    @Override
    public void updateRecord(DatabaseRecord record) {
        // فرض می‌کنیم نام کاربری تغییر نمی‌کند؛ در غیر این صورت منطق خاص خود را دارد
        records.replace(record.getUsername(), record);
    }

    @Override
    public void removeRecord(DatabaseRecord record) {
        records.remove(record.getUsername());
    }

    @Override
    public boolean containsUser(String username) {
        return records.containsKey(username);
    }

    // ==================== ذخیره‌سازی کلی روی دیسک ====================
    @Override
    public synchronized void saveAllData() {
        try {
            String json = gson.toJson(records.values());
            String encrypted = EncryptionEngine.encrypt(json);
            Files.writeString(mainDbFile, encrypted);
        } catch (IOException e) {
            System.err.println("Error saving main database: " + e.getMessage());
        }
    }

    // ==================== مدیریت نشست فعال ====================
    @Override
    public void saveActiveSession(DatabaseRecord activeInfo) {
        this.activeSession = activeInfo;
        try {
            String json = gson.toJson(activeInfo);
            String encrypted = EncryptionEngine.encrypt(json);
            Files.writeString(sessionFile, encrypted);
        } catch (IOException e) {
            System.err.println("Error saving session: " + e.getMessage());
        }
    }

    @Override
    public Optional<DatabaseRecord> getActiveSession() {
        return Optional.ofNullable(activeSession);
    }

    @Override
    public void clearActiveSession() {
        this.activeSession = null;
        try {
            Files.deleteIfExists(sessionFile);
        } catch (IOException e) {
            System.err.println("Error clearing session file: " + e.getMessage());
        }
    }

    @Override
    public boolean isThereAnyCurrentSession() {
        return activeSession != null;
    }
}