package com.PVZ.model.user;

public class NewsEntry {
    private String text;
    private boolean read;

    public NewsEntry() {
    }

    public NewsEntry(String text) {
        this.text = text;
        this.read = false;
    }

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
}
