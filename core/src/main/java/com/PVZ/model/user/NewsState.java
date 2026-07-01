package com.PVZ.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewsState {
    private final List<NewsEntry> news;

    public NewsState() {
        this.news = new ArrayList<>();
    }

    public List<NewsEntry> getNews() {
        return news;
    }

    public void addNews(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        news.add(new NewsEntry(text));
    }

    @JsonIgnore
    public List<NewsEntry> getUnreadNews() {
        List<NewsEntry> unread = new ArrayList<>();
        for (NewsEntry entry : news) {
            if (!entry.isRead()) {
                unread.add(entry);
            }
        }
        return unread;
    }

    @JsonIgnore
    public List<NewsEntry> getAllNews() {
        return Collections.unmodifiableList(news);
    }

    public void markAllRead() {
        for (NewsEntry entry : news) {
            entry.setRead(true);
        }
    }
}
