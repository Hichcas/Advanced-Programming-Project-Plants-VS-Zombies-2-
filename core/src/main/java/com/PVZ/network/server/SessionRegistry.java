package com.PVZ.network.server;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * نگاشت username -> ClientSession برای کاربرانی که همین الان لاگین
 * هستند. این کلاس مرکزی است که تمام تیم (matchmaking، ری‌اکشن‌ها، سینک
 * بازی) برای فهمیدن «این کاربر آنلاین است یا نه» و «پیام را برای کدام
 * Session بفرستم» استفاده می‌کند - پس به‌جای این‌که هرکس خودش یک
 * Map جدا بسازد، همه از همین یکی استفاده می‌کنند.
 */
public final class SessionRegistry {

    private static final ConcurrentMap<String, ClientSession> ONLINE_USERS = new ConcurrentHashMap<>();

    private SessionRegistry() {
    }

    /** وقتی کاربری لاگین موفق انجام می‌دهد صدا زده می‌شود. */
    public static void markOnline(String username, ClientSession session) {
        if (username == null) return;
        ONLINE_USERS.put(username, session);
    }

    /** وقتی کاربر logout می‌کند یا اتصالش قطع می‌شود صدا زده می‌شود. */
    public static void markOffline(String username) {
        if (username == null) return;
        ONLINE_USERS.remove(username);
    }

    public static boolean isOnline(String username) {
        return username != null && ONLINE_USERS.containsKey(username);
    }

    /** Session کاربر آنلاین را برمی‌گرداند، یا null اگر آفلاین باشد. */
    public static ClientSession get(String username) {
        return username == null ? null : ONLINE_USERS.get(username);
    }

    public static Collection<ClientSession> allOnline() {
        return ONLINE_USERS.values();
    }

    public static int onlineCount() {
        return ONLINE_USERS.size();
    }
}
