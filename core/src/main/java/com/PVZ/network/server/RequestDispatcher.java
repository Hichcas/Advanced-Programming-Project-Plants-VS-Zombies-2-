package com.PVZ.network.server;

import com.PVZ.network.common.MessageType;
import com.PVZ.network.common.NetworkMessage;

import java.util.EnumMap;
import java.util.Map;

/**
 * مسیریابی پیام‌های ورودی به Handler مناسب.
 *
 * چرا این‌طوری طراحی شده: با یک تیم سه‌نفره که هرکس بخش خودش را
 * می‌زند (این بخش: حساب کاربری، نفر بعدی: matchmaking، نفر سوم:
 * بازی/ری‌اکشن)، اگر همه مجبور بودند داخل یک متد بزرگ switch(type)
 * کد اضافه کنند، مدام Merge Conflict می‌خوردید. به‌جایش هرکس یک کلاس
 * *Handlers جدا می‌سازد (مثل {@link com.PVZ.network.server.handlers.AuthHandlers})
 * و در {@code main} سرور فقط یک خط {@code XyzHandlers.registerAll(dispatcher)}
 * اضافه می‌کند.
 *
 * یک Handler می‌تواند null برگرداند، یعنی «خودم مستقیم روی session.send(...)
 * جواب دادم یا این پیام اصلا نیازی به پاسخ فوری ندارد» (مثلا وقتی سرور
 * می‌خواهد پاسخ را به کاربر دیگری push کند، نه به فرستنده).
 */
public class RequestDispatcher {

    @FunctionalInterface
    public interface RequestHandler {
        /**
         * @param session کاربری که این پیام را فرستاده.
         * @param request خود پیام.
         * @return پیام پاسخی که باید برای همین فرستنده برگردد، یا null اگر
         *         Handler خودش پاسخ‌دهی (یا push به دیگری) را انجام داده باشد.
         */
        NetworkMessage handle(ClientSession session, NetworkMessage request);
    }

    private final Map<MessageType, RequestHandler> handlers = new EnumMap<>(MessageType.class);

    public void register(MessageType type, RequestHandler handler) {
        handlers.put(type, handler);
    }

    /** توسط ClientSession برای هر پیام دریافتی صدا زده می‌شود. */
    public void dispatch(ClientSession session, NetworkMessage request) {
        session.updateLastActivity(); // ← این خط را اضافه کن
        RequestHandler handler = handlers.get(request.getType());
        if (handler == null) {
            session.send(NetworkMessage.reply(request.getRequestId(), MessageType.SERVER_ERROR)
                    .with("message", "Unknown or unhandled message type: " + request.getType()));
            return;
        }
        try {
            NetworkMessage response = handler.handle(session, request);
            if (response != null) {
                session.send(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.send(NetworkMessage.reply(request.getRequestId(), MessageType.SERVER_ERROR)
                    .with("message", "Internal server error: " + e.getMessage()));
        }
    }
}
