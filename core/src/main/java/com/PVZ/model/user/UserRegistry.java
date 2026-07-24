package com.PVZ.model.user;

import com.PVZ.database.UserDatabase;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class UserRegistry {

    private static final Map<String, User> USERS = new ConcurrentHashMap<>();
    private static final Set<String> DIRTY_USERS = ConcurrentHashMap.newKeySet();

    private UserRegistry() {
    }

    public static boolean register(User user) {
        if (user == null || user.profile == null || user.profile.getUsername() == null)
            return false;

        String username = user.profile.getUsername();
        boolean added = USERS.putIfAbsent(username, user) == null;
        if (!added) return false;

        try {
            UserDatabase.save(username, user);
            UserDatabase.addToIndex(username);
            return true;
        } catch (Exception e) {
            USERS.remove(username);
            System.err.println("Registration save failed: " + e);
            e.printStackTrace();
            return false;
        }
    }

    public static User loginUser(String username) {
        try {
            if (!UserDatabase.exists(username)) return null;
            User user = UserDatabase.load(username);
            if (user == null) return null;
            USERS.put(username, user);
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean containsUsername(String username) {
        return username != null && (USERS.containsKey(username) || checkIndex(username));
    }

    private static boolean checkIndex(String username) {
        try {
            return UserDatabase.exists(username);
        } catch (Exception e) {
            return false;
        }
    }

    public static User getUser(String username) {
        if (username == null) return null;
        User user = USERS.get(username);
        if (user == null) {
            try {
                user = UserDatabase.load(username);
                if (user != null) USERS.put(username, user);
            } catch (Exception ignored) {
            }
        }
        return user;
    }

    public static boolean isUsernameAvailable(String username) {
        return username != null && !USERS.containsKey(username) && !checkIndex(username);
    }

    public static Collection<User> allUsers() {
        return USERS.values();
    }

    public static synchronized boolean changeUsername(User user, String newUsername) {
        if (user == null || user.profile == null || newUsername == null)
            return false;
        String oldUsername = user.profile.getUsername();
        if (oldUsername == null || oldUsername.equals(newUsername))
            return false;
        if (USERS.containsKey(newUsername) || checkIndex(newUsername))
            return false;
        if (!USERS.remove(oldUsername, user))
            return false;

        try {
            saveUserToDatabase(oldUsername);
            UserDatabase.delete(oldUsername);
            user.profile.setUsername(newUsername);
            USERS.put(newUsername, user);
            UserDatabase.save(newUsername, user);
            UserDatabase.addToIndex(newUsername);
            return true;
        } catch (Exception e) {
            user.profile.setUsername(oldUsername);
            USERS.put(oldUsername, user);
            return false;
        }
    }

    public static void markDirty(String username) {
        if (username != null) DIRTY_USERS.add(username);
    }

    public static void touch(String username) {
        if (username == null) {
            return;
        }
        markDirty(username);
        saveUserToDatabase(username);
    }

    public static void saveAllDirtyUsers() {
        if (DIRTY_USERS.isEmpty()) return;
        for (String username : DIRTY_USERS) {
            saveUserToDatabase(username);
        }
        DIRTY_USERS.clear();
    }

    public static void saveUserToDatabase(String username) {
        User user = USERS.get(username);
        if (user == null) return;
        try {
            UserDatabase.save(username, user);
        } catch (Exception ignored) {
        }
    }

    public static void clearUserCache(String username) {
        saveUserToDatabase(username);
        DIRTY_USERS.remove(username);
        USERS.remove(username);
    }

    public static void clear() {
        saveAllDirtyUsers();
        USERS.clear();
        DIRTY_USERS.clear();
    }

    public static void loadAllFromDatabase() {
        try {
            for (String username : UserDatabase.loadIndex()) {
                if (!USERS.containsKey(username)) {
                    User user = UserDatabase.load(username);
                    if (user != null) USERS.put(username, user);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
