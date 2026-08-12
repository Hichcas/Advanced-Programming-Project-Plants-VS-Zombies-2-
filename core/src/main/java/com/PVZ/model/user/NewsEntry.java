package com.PVZ.model.user;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NewsEntry {
    private String text;
    private boolean read;
    private long timestamp;   // زمان ایجاد خبر (epoch millis)

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd  HH:mm");

    public NewsEntry() {
    }

    public NewsEntry(String text) {
        this(text, System.currentTimeMillis());
    }

    public NewsEntry(String text, long timestamp) {
        this.text = text;
        this.read = false;
        this.timestamp = timestamp;
    }

    /** برگرداندن متن خبر به‌همراه تاریخ در قالب زیبا */
    public String getFormattedText() {
        String dateStr = DATE_FORMAT.format(new Date(timestamp));
        return text + "\n" + dateStr;
    }

    // ---------- getter / setter ----------
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
