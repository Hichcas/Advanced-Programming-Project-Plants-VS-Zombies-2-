package com.PVZ.controller.menuControllers;

import com.PVZ.database.EncryptionEngine;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.input.DTO.ProfileInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class ProfileMenuController {

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof ProfileInputDTO profileInput)) {
            return new OutputDTO(false, "Invalid input.");
        }

        if (profileInput.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        return switch (profileInput.getCommand()) {
            case CHANGE_USERNAME -> changeUsername(profileInput.getUsername());
            case CHANGE_NICKNAME -> changeNickname(profileInput.getNickname());
            case CHANGE_EMAIL -> changeEmail(profileInput.getEmail());
            case CHANGE_PASSWORD -> changePassword(profileInput.getNewPassword(), profileInput.getOldPassword());
            case SHOW_INFO -> showInfo();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToMain();
        };
    }

    private OutputDTO changeUsername(String newUsername) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new OutputDTO(false, "You must be logged in first.");
        }
        if (newUsername == null || !newUsername.matches("^[A-Za-z0-9-]+$")) {
            return new OutputDTO(false, "Invalid username.");
        }

        String oldUsername = currentUser.profile.getUsername();
        if (oldUsername != null && oldUsername.equals(newUsername)) {
            return new OutputDTO(false, "This username is already your username.");
        }

        if (!UserRegistry.changeUsername(currentUser, newUsername)) {
            return new OutputDTO(false, "This username is already taken.");
        }

        return new OutputDTO(true, "Username changed successfully.");
    }

    private OutputDTO changeNickname(String newNickname) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new OutputDTO(false, "You must be logged in first.");
        }
        if (newNickname == null || newNickname.length() < 3 || newNickname.length() > 30) {
            return new OutputDTO(false, "Invalid nickname length.");
        }
        if (newNickname.equals(currentUser.profile.getNickname())) {
            return new OutputDTO(false, "This nickname is already your nickname.");
        }

        currentUser.profile.setNickname(newNickname);
        UserRegistry.markDirty(currentUser.profile.getUsername());
        return new OutputDTO(true, "Nickname changed successfully.");
    }

    private OutputDTO changeEmail(String newEmail) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new OutputDTO(false, "You must be logged in first.");
        }
        if (newEmail == null || !isValidEmail(newEmail)) {
            return new OutputDTO(false, "Invalid email.");
        }
        if (newEmail.equals(currentUser.profile.getEmail())) {
            return new OutputDTO(false, "This email is already your email.");
        }

        currentUser.profile.setEmail(newEmail);
        UserRegistry.markDirty(currentUser.profile.getUsername());
        return new OutputDTO(true, "Email changed successfully.");
    }

    private OutputDTO changePassword(String newPassword, String oldPassword) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new OutputDTO(false, "You must be logged in first.");
        }
        if (newPassword == null || oldPassword == null) {
            return new OutputDTO(false, "Invalid password.");
        }

        String currentHash = currentUser.profile.getPasswordHash();
        String oldHash;
        String newHash;
        try {
            oldHash = EncryptionEngine.hash(oldPassword);
            newHash = EncryptionEngine.hash(newPassword);
        } catch (Exception e) {
            return new OutputDTO(false, "Failed to process password.");
        }

        if (!oldHash.equals(currentHash)) {
            return new OutputDTO(false, "Old password is incorrect.");
        }

        if (newHash.equals(currentHash)) {
            return new OutputDTO(false, "New password must be different from current password.");
        }

        if (!isStrongPassword(newPassword)) {
            return new OutputDTO(false, "Weak password.");
        }

        currentUser.profile.setPasswordHash(newHash);
        UserRegistry.markDirty(currentUser.profile.getUsername());
        return new OutputDTO(true, "Password changed successfully.");
    }

    private OutputDTO showInfo() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return new OutputDTO(false, "You must be logged in first.");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Username: ").append(currentUser.profile.getUsername()).append('\n');
        builder.append("Nickname: ").append(currentUser.profile.getNickname()).append('\n');
        builder.append("Games played: ").append(currentUser.userStats.getGamesPlayed()).append('\n');
        builder.append("Coins: ").append(currentUser.userStats.getCoins()).append('\n');
        builder.append("Diamonds: ").append(currentUser.userStats.getDiamonds()).append('\n');
        builder.append("Stages completed: ").append(currentUser.userStats.getStagesCompleted()).append('\n');
        builder.append("Highest score: ").append(currentUser.userStats.getHighestScore());

        return new OutputDTO(true, builder.toString());
    }

    private OutputDTO exitToMain() {
        AppStatus.currentMenuType = MenuType.MAIN;
        return new OutputDTO(true, "Entered Main Menu.");
    }

    private User getCurrentUser() {
        return AppStatus.currentUser;
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        String specialChars = "!#$%^&*()=+{}[]|/\\:;'\" ,><?";

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (Character.isLowerCase(ch)) {
                hasLower = true;
            } else if (Character.isUpperCase(ch)) {
                hasUpper = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else if (specialChars.indexOf(ch) >= 0) {
                hasSpecial = true;
            }
        }

        return hasLower && hasUpper && hasDigit && hasSpecial;
    }

    private boolean isValidEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex != email.lastIndexOf('@')) {
            return false;
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex + 1);

        if (!localPart.matches("^[A-Za-z0-9](?:[A-Za-z0-9_.-]*[A-Za-z0-9])?$") ) {
            return false;
        }
        if (localPart.contains("..")) {
            return false;
        }
        if (!domainPart.matches("^[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$")) {
            return false;
        }

        String[] labels = domainPart.split("\\.");
        String tld = labels[labels.length - 1];
        return tld.length() >= 2;
    }
}
