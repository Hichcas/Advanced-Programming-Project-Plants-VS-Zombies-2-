package com.PVZ.controller.menuControllers;

import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.NewsEntry;
import com.PVZ.model.user.NewsState;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.input.DTO.NewsInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

import java.util.List;

public class NewsMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof NewsInputDTO newsInput)) {
            return new OutputDTO(false, "Invalid input.");
        }

        if (newsInput.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        return switch (newsInput.getCommand()) {
            case SHOW_UNREAD -> showUnread();
            case SHOW_ALL -> showAll();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToMain();
        };
    }

    private NewsState getNewsState() {
        User currentUser = AppStatus.currentUser;
        if (currentUser == null) {
            return null;
        }
        return currentUser.newsState;
    }

    /** بررسی وجود اخبار نخوانده – برای نشان‌گر قرمز استفاده می‌شود */
    public static boolean hasUnreadNews() {
        User user = AppStatus.currentUser;
        if (user == null || user.newsState == null) {
            return false;
        }
        return !user.newsState.getUnreadNews().isEmpty();
    }

    private OutputDTO showUnread() {
        NewsState newsState = getNewsState();
        if (newsState == null) {
            return new OutputDTO(false, "No active user.");
        }

        List<NewsEntry> unread = newsState.getUnreadNews();
        if (unread.isEmpty()) {
            return new OutputDTO(true, "No unread news.");
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < unread.size(); i++) {
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(i + 1).append(". ").append(unread.get(i).getText());
            unread.get(i).setRead(true);
        }
        UserRegistry.touch(AppStatus.currentUser.profile.getUsername());
        return new OutputDTO(true, builder.toString());
    }

    private OutputDTO showAll() {
        NewsState newsState = getNewsState();
        if (newsState == null) {
            return new OutputDTO(false, "No active user.");
        }

        List<NewsEntry> allNews = newsState.getAllNews();
        if (allNews.isEmpty()) {
            return new OutputDTO(true, "No news available.");
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < allNews.size(); i++) {
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(i + 1).append(". ").append(allNews.get(i).getText());
            if (!allNews.get(i).isRead()) {
                builder.append(" [unread]");
            }
        }
        return new OutputDTO(true, builder.toString());
    }

    private OutputDTO exitToMain() {
        AppStatus.currentMenuType = MenuType.MAIN;
        return new OutputDTO(true, "Entered Main Menu.");
    }
}
