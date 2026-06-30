package model.user;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UserRegistry {

    private static final Map<String, User> USERS = new ConcurrentHashMap<>();

    private UserRegistry() {
    }

    public static boolean containsUsername(String username) {
        return username != null && USERS.containsKey(username);
    }

    public static User getUser(String username) {
        if (username == null) {
            return null;
        }
        return USERS.get(username);
    }

    public static boolean register(User user) {
        if (user == null || user.profile == null || user.profile.getUsername() == null) {
            return false;
        }
        String username = user.profile.getUsername();
        return USERS.putIfAbsent(username, user) == null;
    }

    public static boolean isUsernameAvailable(String username) {
        return username != null && !USERS.containsKey(username);
    }

    public static synchronized boolean changeUsername(User user, String newUsername) {
        if (user == null || user.profile == null || newUsername == null) {
            return false;
        }

        String oldUsername = user.profile.getUsername();
        if (oldUsername == null || oldUsername.equals(newUsername)) {
            return false;
        }

        if (USERS.containsKey(newUsername)) {
            return false;
        }

        if (!USERS.remove(oldUsername, user)) {
            return false;
        }

        user.profile.setUsername(newUsername);
        USERS.put(newUsername, user);
        return true;
    }

    public static boolean authenticate(String username, String passwordHash) {
        User user = getUser(username);
        if (user == null || user.profile == null) {
            return false;
        }
        return user.profile.getPasswordHash() != null && user.profile.getPasswordHash().equals(passwordHash);
    }

    public static Collection<User> allUsers() {
        return USERS.values();
    }

    public static void clear() {
        USERS.clear();
    }
}
