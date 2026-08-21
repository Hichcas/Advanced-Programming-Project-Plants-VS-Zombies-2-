package com.PVZ.network.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * پاکت استاندارد ارتباطی بین Client و Server.
 *
 * هر پیام یک {@code type} دارد (نوعش را مشخص می‌کند)، یک {@code requestId}
 * که برای Request/Response matching سمت Client استفاده می‌شود (چون
 * NetworkClient می‌تواند هم‌زمان چند درخواست async داشته باشد)، و یک
 * {@code payload} آزاد به‌صورت Map که هرکس هرچی لازم دارد داخلش می‌گذارد -
 * نیازی به تعریف کلاس DTO جدا برای هر نوع پیام نیست، چون در بازه‌ی زمانی
 * ۵ روزه سرعت توسعه مهم‌تر از type-safety کامل است.
 *
 * پیام‌هایی که سمت Server به‌صورت push (بدون این‌که Client درخواستی زده
 * باشد) فرستاده می‌شوند - مثل دعوت‌نامه یا snapshot بازی - باید
 * requestId را خالی (null) بگذارند؛ NetworkClient این‌ها را به‌جای
 * matching با یک درخواست معلق، به Listenerهای ثبت‌شده روی type می‌فرستد.
 */
public class NetworkMessage {

    private String requestId;
    private MessageType type;
    private Map<String, Object> payload = new HashMap<>();

    /** برای Jackson لازم است. */
    public NetworkMessage() {
    }

    public NetworkMessage(MessageType type) {
        this.type = type;
    }

    public static NetworkMessage request(MessageType type) {
        NetworkMessage m = new NetworkMessage(type);
        m.requestId = UUID.randomUUID().toString();
        return m;
    }

    /** پاسخ به یک requestId مشخص می‌سازد (سمت سرور استفاده می‌شود). */
    public static NetworkMessage reply(String requestId, MessageType type) {
        NetworkMessage m = new NetworkMessage(type);
        m.requestId = requestId;
        return m;
    }

    /** پیام push بدون requestId (سمت سرور، وقتی خودش شروع‌کننده‌ی مکالمه است). */
    public static NetworkMessage push(MessageType type) {
        return new NetworkMessage(type);
    }

    public NetworkMessage with(String key, Object value) {
        payload.put(key, value);
        return this;
    }

    public String getString(String key) {
        Object v = payload.get(key);
        return v == null ? null : String.valueOf(v);
    }

    public int getInt(String key, int defaultValue) {
        Object v = payload.get(key);
        if (v instanceof Number n) return n.intValue();
        if (v instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object v = payload.get(key);
        if (v instanceof Boolean b) return b;
        if (v instanceof String s) return Boolean.parseBoolean(s);
        return defaultValue;
    }

    public Object get(String key) {
        return payload.get(key);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload == null ? new HashMap<>() : payload;
    }
}
