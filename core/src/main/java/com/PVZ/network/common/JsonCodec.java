package com.PVZ.network.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * تبدیل {@link NetworkMessage} به/از یک خط JSON (بدون کاراکتر newline
 * داخلش، چون پروتکل ما newline-delimited است - هر پیام دقیقا یک خط روی
 * سوکت).
 *
 * از همان تنظیمات Jackson که {@code UserDatabase} استفاده می‌کند پیروی
 * می‌کنیم (فیلدها مستقیم serialize می‌شوند، نیازی به getter/setter کامل
 * روی هر مدل نیست) تا آبجکت‌های User/Profile/... که همین الان با آن
 * ObjectMapper سازگارند، بدون تغییر داخل payload پیام‌ها هم جا بگیرند.
 */
public final class JsonCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    private JsonCodec() {
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /** یک NetworkMessage را به یک خط فشرده‌ی JSON تبدیل می‌کند (بدون newline). */
    public static String encode(NetworkMessage message) {
        try {
            String json = MAPPER.writeValueAsString(message);
            // اطمینان از این‌که هیچ newline داخل خروجی نیست (protocol را نمی‌شکند)
            return json.replace('\n', ' ').replace('\r', ' ');
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode NetworkMessage: " + e.getMessage(), e);
        }
    }

    public static NetworkMessage decode(String line) {
        try {
            return MAPPER.readValue(line, NetworkMessage.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decode NetworkMessage: " + e.getMessage(), e);
        }
    }

    /** برای هر Handler که می‌خواهد payload را به یک کلاس مشخص تبدیل کند (مثلا User.class). */
    public static <T> T convert(Object rawValue, Class<T> targetType) {
        return MAPPER.convertValue(rawValue, targetType);
    }
}
