package com.PVZ.network.client;

import com.PVZ.model.user.User;
import com.PVZ.network.common.JsonCodec;
import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * نقطه‌ی ورودی ساده برای بقیه‌ی اپلیکیشن (Controllerها) برای حرف زدن با
 * سرور. یک {@link NetworkClient} به‌صورت static نگه می‌دارد - دقیقا مثل
 * الگویی که {@code AppStatus} برای نگه‌داشتن state سراسری استفاده کرده،
 * چون این اپ در کل روی static singleton ساخته شده و هر Client یک JVM
 * جداست (پس تداخلی با چند کاربر روی یک پروسه وجود ندارد).
 *
 * این کلاس عمدا نتیجه را به‌صورت بلاک‌کننده برمی‌گرداند (Login/Register
 * چیزی نیستند که در همون فریم باید async باشند - کاربر منتظر جواب
 * می‌ماند)، ولی برای پیام‌های push (دعوت‌نامه، snapshot بازی و ...)
 * تیم باید مستقیم از {@link NetworkClient#on} استفاده کند، نه این کلاس.
 */
public final class NetworkSession {

    private static final NetworkClient CLIENT = new NetworkClient();
    private static final int DEFAULT_PORT = 5050;

    private NetworkSession() {
    }

    public static NetworkClient client() {
        return CLIENT;
    }

    public static boolean isConnected() {
        return CLIENT.isConnected();
    }

    public static void connect(String host) throws IOException {
        connect(host, DEFAULT_PORT);
    }

    public static void connect(String host, int port) throws IOException {
        if (CLIENT.isConnected()) return;
        CLIENT.connect(host, port);
    }

    /** نتیجه‌ی یک عملیات احراز هویت - یا موفق با کاربر برگشتی، یا ناموفق با پیام خطا. */
    public record AuthResult(boolean success, String message, User user) {
        static AuthResult failure(String message) {
            return new AuthResult(false, message, null);
        }
    }

    public static AuthResult login(String username, String password) {
        try {
            NetworkMessage response = CLIENT.sendRequestBlocking(
                    NetworkMessage.request(MessageType.LOGIN)
                            .with("username", username)
                            .with("password", password));
            return toAuthResult(response);
        } catch (TimeoutException e) {
            return AuthResult.failure("Server did not respond in time.");
        } catch (IOException e) {
            return AuthResult.failure("Connection error: " + e.getMessage());
        }
    }

    public static AuthResult register(String username, String password, String nickname,
                                       String email, String gender,
                                       String securityQuestion, String securityAnswer) {
        try {
            NetworkMessage response = CLIENT.sendRequestBlocking(
                    NetworkMessage.request(MessageType.REGISTER)
                            .with("username", username)
                            .with("password", password)
                            .with("nickname", nickname)
                            .with("email", email)
                            .with("gender", gender)
                            .with("securityQuestion", securityQuestion)
                            .with("securityAnswer", securityAnswer));
            return toAuthResult(response);
        } catch (TimeoutException e) {
            return AuthResult.failure("Server did not respond in time.");
        } catch (IOException e) {
            return AuthResult.failure("Connection error: " + e.getMessage());
        }
    }

    private static AuthResult toAuthResult(NetworkMessage response) {
        boolean success = response.getBoolean("success", false);
        String message = response.getString("message");
        if (!success) {
            return AuthResult.failure(message != null ? message : "Request failed.");
        }
        User user = JsonCodec.convert(response.get("user"), User.class);
        return new AuthResult(true, message, user);
    }

    public static boolean checkUsernameAvailable(String username) {
        try {
            NetworkMessage response = CLIENT.sendRequestBlocking(
                    NetworkMessage.request(MessageType.CHECK_USERNAME).with("username", username));
            return response.getBoolean("available", false);
        } catch (Exception e) {
            return false;
        }
    }

    /** کل User فعلی را به سرور می‌فرستد تا ذخیره شود (مثلا موقع خروج یا بعد از تغییر مهم). */
    public static boolean syncUser(User user) {
        try {
            NetworkMessage response = CLIENT.sendRequestBlocking(
                    NetworkMessage.request(MessageType.SYNC_USER).with("user", user));
            return response.getBoolean("success", false);
        } catch (Exception e) {
            return false;
        }
    }

    public static void logout(User user) {
        try {
            if (isConnected() && user != null) {
                syncUser(user);
            }
            CLIENT.sendRequestBlocking(NetworkMessage.request(MessageType.LOGOUT));
        } catch (Exception ignored) {
        }
    }
}
