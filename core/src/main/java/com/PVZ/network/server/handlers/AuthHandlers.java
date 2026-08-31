package com.PVZ.network.server.handlers;

import com.PVZ.database.EncryptionEngine;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.network.common.JsonCodec;
import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;
import com.PVZ.network.server.ClientSession;
import com.PVZ.network.server.RequestDispatcher;
import com.PVZ.network.server.SessionRegistry;

/**
 * حساب کاربری روی سرور: ثبت‌نام، ورود، خروج، و همگام‌سازی کامل شیء
 * {@link User} بین دستگاه‌ها.
 *
 * منطق اعتبارسنجی و رمزنگاری همانی است که قبلا سمت Client در
 * RegisterMenuController/LoginMenuController بود - فقط این‌جا اجرا
 * می‌شود چون طبق سند فاز سوم، ذخیره‌سازی و احراز هویت باید کاملا سمت
 * سرور باشد. {@link UserRegistry} و {@link com.PVZ.database.UserDatabase}
 * بدون هیچ تغییری همان‌هایی هستند که قبلا کلاینت مستقیم صدا می‌زد -
 * الان فقط سرور آن‌ها را صدا می‌زند.
 *
 * درباره‌ی سطح Sync: طبق تصمیم تیم، سینک کامل User فقط در دو لحظه اتفاق
 * می‌افتد نه هر تغییر کوچک: (۱) لحظه‌ی LOGIN که کل User به کلاینت
 * فرستاده می‌شود، (۲) لحظه‌ای که کلاینت صریحا SYNC_USER می‌فرستد (پیشنهاد:
 * موقع خروج از حساب یا خروج از بازی). این خیلی ساده‌تر از sync
 * فیلد-به-فیلد است و برای بازه‌ی ۵ روزه کافی است.
 */
public final class AuthHandlers {

    private AuthHandlers() {
    }

    public static void registerAll(RequestDispatcher dispatcher) {
        dispatcher.register(MessageType.REGISTER, AuthHandlers::handleRegister);
        dispatcher.register(MessageType.LOGIN, AuthHandlers::handleLogin);
        dispatcher.register(MessageType.LOGOUT, AuthHandlers::handleLogout);
        dispatcher.register(MessageType.SYNC_USER, AuthHandlers::handleSyncUser);
        dispatcher.register(MessageType.FETCH_USER, AuthHandlers::handleFetchUser);
        dispatcher.register(MessageType.CHECK_USERNAME, AuthHandlers::handleCheckUsername);
        dispatcher.register(MessageType.SUBMIT_SCORE, AuthHandlers::handleSubmitScore);
        dispatcher.register(MessageType.PING, (session, request) ->
                NetworkMessage.reply(request.getRequestId(), MessageType.PONG));
    }

    // ===================== REGISTER =====================

    private static NetworkMessage handleRegister(ClientSession session, NetworkMessage request) {
        String username = request.getString("username");
        String password = request.getString("password");
        String nickname = request.getString("nickname");
        String email = request.getString("email");
        String gender = request.getString("gender");
        String securityQuestion = request.getString("securityQuestion");
        String securityAnswer = request.getString("securityAnswer");

        boolean prehashed = request.getBoolean("prehashed", false);
        String error = validateRegisterInput(username, prehashed ? "Aa1!aaaa" : password, nickname, email, gender);
        if (error != null) {
            return fail(request, MessageType.REGISTER_RESULT, error);
        }

        // یکتایی username سمت سرور - طبق سند این چک باید اینجا انجام شود، نه سمت کلاینت
        if (UserRegistry.containsUsername(username)) {
            return fail(request, MessageType.REGISTER_RESULT, "This username is already taken.");
        }

        try {
            String passwordHash = prehashed ? password : EncryptionEngine.hash(password);
            String answerHash = prehashed
                    ? (securityAnswer == null ? "" : securityAnswer)
                    : EncryptionEngine.hash(securityAnswer == null ? "" : securityAnswer);

            User createdUser = User.createNewUser(
                    username, passwordHash, nickname, email, gender,
                    securityQuestion, answerHash);

            boolean ok = UserRegistry.register(createdUser);
            if (!ok) {
                return fail(request, MessageType.REGISTER_RESULT, "Registration failed. Username may already exist.");
            }

            session.setUsername(username);
            SessionRegistry.markOnline(username, session);

            return NetworkMessage.reply(request.getRequestId(), MessageType.REGISTER_RESULT)
                    .with("success", true)
                    .with("message", "Registration successful.")
                    .with("user", createdUser);

        } catch (Exception e) {
            return fail(request, MessageType.REGISTER_RESULT, "Failed to process registration: " + e.getMessage());
        }
    }

    private static String validateRegisterInput(String username, String password, String nickname,
                                                  String email, String gender) {
        if (username == null || !username.matches("^[A-Za-z0-9-]+$")) {
            return "Invalid username.";
        }
        if (password == null || !isStrongPassword(password)) {
            return "Weak password. Use at least 8 characters with upper, lower, digit, and special character.";
        }
        if (nickname == null || nickname.length() < 3 || nickname.length() > 30) {
            return "Invalid nickname length.";
        }
        if (email == null || !isValidEmail(email)) {
            return "Invalid email.";
        }
        if (gender == null || !(gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female"))) {
            return "Invalid gender.";
        }
        return null;
    }

    // ===================== LOGIN =====================

    private static NetworkMessage handleLogin(ClientSession session, NetworkMessage request) {
        String username = request.getString("username");
        String password = request.getString("password");

        if (username == null || password == null) {
            return fail(request, MessageType.LOGIN_RESULT, "Username and password are required.");
        }

        // اگر همین کاربر از یک دستگاه دیگر آنلاین است، اجازه‌ی دو اتصال هم‌زمان
        // نمی‌دهیم - جلوی خراب شدن فایل سرور به‌خاطر دو نوشتن هم‌زمان را می‌گیرد.
        if (SessionRegistry.isOnline(username)) {
            return fail(request, MessageType.LOGIN_RESULT, "This account is already logged in from another device.");
        }

        User user = UserRegistry.loginUser(username);
        if (user == null) {
            return fail(request, MessageType.LOGIN_RESULT, "Invalid username or password.");
        }

        try {
            if (!user.profile.getPasswordHash().equals(EncryptionEngine.hash(password))) {
                return fail(request, MessageType.LOGIN_RESULT, "Invalid username or password.");
            }
        } catch (Exception e) {
            return fail(request, MessageType.LOGIN_RESULT, "Failed to verify password.");
        }

        UserRegistry.touch(username);
        session.setUsername(username);
        SessionRegistry.markOnline(username, session);

        return NetworkMessage.reply(request.getRequestId(), MessageType.LOGIN_RESULT)
                .with("success", true)
                .with("message", "Logged in successfully.")
                .with("user", user);
    }

    // ===================== LOGOUT =====================

    private static NetworkMessage handleLogout(ClientSession session, NetworkMessage request) {
        String username = session.getUsername();
        if (username != null) {
            UserRegistry.clearUserCache(username); // ذخیره‌ی آخرین نسخه + پاک کردن از کش سرور
            SessionRegistry.markOffline(username);
            session.setUsername(null);
        }
        return NetworkMessage.reply(request.getRequestId(), MessageType.LOGOUT_RESULT)
                .with("success", true);
    }

    // ===================== SYNC_USER (کلاینت -> سرور) =====================

    private static NetworkMessage handleSyncUser(ClientSession session, NetworkMessage request) {
        if (!session.isAuthenticated()) {
            return fail(request, MessageType.SYNC_USER_RESULT, "Not logged in.");
        }
        Object rawUser = request.get("user");
        if (rawUser == null) {
            return fail(request, MessageType.SYNC_USER_RESULT, "Missing 'user' field.");
        }

        User incoming = JsonCodec.convert(rawUser, User.class);
        // امنیت ساده: کاربر فقط می‌تواند رکورد خودش را بازنویسی کند، نه کاربر دیگری را
        if (incoming.profile == null || !session.getUsername().equals(incoming.profile.getUsername())) {
            return fail(request, MessageType.SYNC_USER_RESULT, "Cannot sync a different user's data.");
        }

        try {
            com.PVZ.database.UserDatabase.save(session.getUsername(), incoming);
        } catch (Exception e) {
            return fail(request, MessageType.SYNC_USER_RESULT, "Failed to save: " + e.getMessage());
        }

        return NetworkMessage.reply(request.getRequestId(), MessageType.SYNC_USER_RESULT)
                .with("success", true);
    }

    // ===================== FETCH_USER (سرور -> کلاینت) =====================

    private static NetworkMessage handleFetchUser(ClientSession session, NetworkMessage request) {
        if (!session.isAuthenticated()) {
            return fail(request, MessageType.FETCH_USER_RESULT, "Not logged in.");
        }
        User user = UserRegistry.getUser(session.getUsername());
        if (user == null) {
            return fail(request, MessageType.FETCH_USER_RESULT, "User not found.");
        }
        return NetworkMessage.reply(request.getRequestId(), MessageType.FETCH_USER_RESULT)
                .with("success", true)
                .with("user", user);
    }

    // ===================== CHECK_USERNAME =====================

    private static NetworkMessage handleCheckUsername(ClientSession session, NetworkMessage request) {
        String username = request.getString("username");
        boolean available = username != null && UserRegistry.isUsernameAvailable(username);
        return NetworkMessage.reply(request.getRequestId(), MessageType.CHECK_USERNAME_RESULT)
                .with("available", available);
    }

    // ===================== SUBMIT_SCORE («بازی امتیازی» تحت شبکه) =====================
    // طبق سند فاز شبکه: پس از پایان هر دور، امتیاز به سرور ارسال می‌شود و فقط اگر از
    // رکورد فعلی کاربر روی سرور بیشتر باشد، رکورد به‌روزرسانی می‌شود (نه بازنویسی مطلق).
    // این مقدار همان چیزی است که ستون «My Point» در لیدربورد از userStats.highestScore
    // می‌خواند - پس کاربری که هنوز این پیام را نفرستاده، مقداری غیر از صفر پیش‌فرض ندارد.

    private static NetworkMessage handleSubmitScore(ClientSession session, NetworkMessage request) {
        if (!session.isAuthenticated()) {
            return fail(request, MessageType.SUBMIT_SCORE_RESULT, "Not logged in.");
        }
        int score = request.getInt("score", -1);
        if (score < 0) {
            return fail(request, MessageType.SUBMIT_SCORE_RESULT, "Invalid score.");
        }

        User user = UserRegistry.getUser(session.getUsername());
        if (user == null || user.userStats == null) {
            return fail(request, MessageType.SUBMIT_SCORE_RESULT, "User not found.");
        }

        boolean isNewRecord = score > user.userStats.getHighestScore();
        if (isNewRecord) {
            user.userStats.updateHighestScore(score);
            try {
                com.PVZ.database.UserDatabase.save(session.getUsername(), user);
            } catch (Exception e) {
                return fail(request, MessageType.SUBMIT_SCORE_RESULT, "Failed to save: " + e.getMessage());
            }
        }

        return NetworkMessage.reply(request.getRequestId(), MessageType.SUBMIT_SCORE_RESULT)
                .with("success", true)
                .with("newRecord", isNewRecord)
                .with("highestScore", user.userStats.getHighestScore());
    }

    // ===================== Helpers (کپی‌شده از RegisterMenuController/LoginMenuController) =====================

    private static boolean isStrongPassword(String password) {
        if (password.length() < 8) return false;
        boolean hasLower = false, hasUpper = false, hasDigit = false, hasSpecial = false;
        String specialChars = "!#$%^&*()=+{}[]|/\\:;'\" ,><?";
        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (Character.isLowerCase(ch)) hasLower = true;
            else if (Character.isUpperCase(ch)) hasUpper = true;
            else if (Character.isDigit(ch)) hasDigit = true;
            else if (specialChars.indexOf(ch) >= 0) hasSpecial = true;
        }
        return hasLower && hasUpper && hasDigit && hasSpecial;
    }

    private static boolean isValidEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex != email.lastIndexOf('@')) return false;
        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex + 1);
        if (!localPart.matches("^[A-Za-z0-9](?:[A-Za-z0-9_.-]*[A-Za-z0-9])?$")) return false;
        if (localPart.contains("..")) return false;
        if (!domainPart.matches("^[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$")) return false;
        String[] labels = domainPart.split("\\.");
        String tld = labels[labels.length - 1];
        return tld.length() >= 2;
    }

    private static NetworkMessage fail(NetworkMessage request, MessageType type, String message) {
        return NetworkMessage.reply(request.getRequestId(), type)
                .with("success", false)
                .with("message", message);
    }
}
