package com.PVZ.controller.menuControllers;

import com.PVZ.database.EncryptionEngine;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.network.client.NetworkSession;
import com.PVZ.view.input.DTO.LoginInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class LoginMenuController {

    private enum ResetState {
        NONE,
        WAITING_FOR_ANSWER,
        WAITING_FOR_NEW_PASSWORD
    }

    private static ResetState resetState = ResetState.NONE;
    private static User pendingResetUser;

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof LoginInputDTO loginInput)) {
            return new OutputDTO(false, "Invalid input.");
        }

        if (loginInput.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        return switch (loginInput.getCommand()) {
            case LOGIN -> login(loginInput);
            case FORGET_PASSWORD -> startForgetPassword(loginInput);
            case ANSWER -> handleAnswer(loginInput);
            case NEW_PASSWORD -> handleNewPassword(loginInput);
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> exitToRegister();
        };
    }

    private OutputDTO login(LoginInputDTO input) {
        String username = input.getUsername();
        String password = input.getPassword();

        if (username == null || password == null) {
            return new OutputDTO(false, "Username and password are required.");
        }

        // فاز شبکه: احراز هویت باید سمت سرور انجام شود (سند فاز سوم -
        // "سیستم ورود، ثبت‌نام و پروفایل باید کامل با سرور هماهنگ باشد").
        // اگر اتصال به سرور برقرار است از همان مسیر استفاده می‌کنیم؛ اگر
        // نه (مثلا هنگام توسعه/تست بدون سرور در حال اجرا)، به همان منطق
        // محلی قبلی برمی‌گردیم تا بقیه‌ی بخش‌های آفلاین بازی خراب نشوند.
        if (NetworkSession.isConnected()) {
            return loginOverNetwork(username, password, input.isStayLoggedIn());
        }
        return loginLocally(username, password, input.isStayLoggedIn());
    }

    private OutputDTO loginOverNetwork(String username, String password, boolean stayLoggedIn) {
        NetworkSession.AuthResult result = NetworkSession.login(username, password);
        if (!result.success()) {
            return new OutputDTO(false, result.message());
        }

        User user = result.user();
        user.setStayLoggedIn(stayLoggedIn);
        AppStatus.currentUser = user;
        AppStatus.currentMenuType = MenuType.MAIN;
        resetState = ResetState.NONE;
        pendingResetUser = null;

        return new OutputDTO(true, "Logged in successfully.\nEntered Main Menu.");
    }

    private OutputDTO loginLocally(String username, String password, boolean stayLoggedIn) {
        User user = UserRegistry.loginUser(username);

        if (user == null) {
            return new OutputDTO(false, "Invalid username or password.");
        }

        try {
            if (!user.profile.getPasswordHash().equals(EncryptionEngine.hash(password))) {
                return new OutputDTO(false, "Invalid username or password.");
            }
        } catch (Exception e) {
            return new OutputDTO(false, "Failed to verify password.");
        }

        user.setStayLoggedIn(stayLoggedIn);
        UserRegistry.touch(username);
        AppStatus.currentUser = user;
        AppStatus.currentMenuType = MenuType.MAIN;
        resetState = ResetState.NONE;
        pendingResetUser = null;

        return new OutputDTO(true, "Logged in successfully.\nEntered Main Menu.");
    }

    private OutputDTO startForgetPassword(LoginInputDTO input) {
        String username = input.getUsername();
        String email = input.getEmail();

        User user = UserRegistry.getUser(username);
        if (user == null || user.profile == null || email == null || !email.equals(user.profile.getEmail())) {
            return new OutputDTO(false, "Invalid username or email.");
        }

        pendingResetUser = user;
        resetState = ResetState.WAITING_FOR_ANSWER;
        return new OutputDTO(true, user.profile.getSecurityQuestion());
    }

    private OutputDTO handleAnswer(LoginInputDTO input) {
        // اگر فرایند فراموشی رمز آغاز نشده باشد
        if (resetState != ResetState.WAITING_FOR_ANSWER || pendingResetUser == null) {
            return new OutputDTO(false, "Please start the password reset process first.");
        }

        String answer = input.getAnswer();
        if (answer == null || answer.trim().isEmpty()) {
            return new OutputDTO(false, "Answer cannot be empty.");
        }

        String answerHash;
        try {
            answerHash = EncryptionEngine.hash(answer);
        } catch (Exception e) {
            return new OutputDTO(false, "Failed to verify answer.");
        }

        // بررسی پاسخ
        if (!answerHash.equals(pendingResetUser.profile.getSecurityAnswerHash())) {
            // پاسخ اشتباه – وضعیت را نگه می‌داریم تا کاربر بتواند دوباره تلاش کند
            return new OutputDTO(false, "Incorrect answer. Please try again.");
        }

        // پاسخ درست – رفتن به مرحله‌ی رمز جدید
        resetState = ResetState.WAITING_FOR_NEW_PASSWORD;
        return new OutputDTO(true, "Correct answer. Please enter your new password.");
    }

    private OutputDTO handleNewPassword(LoginInputDTO input) {
        if (resetState != ResetState.WAITING_FOR_NEW_PASSWORD || pendingResetUser == null) {
            return new OutputDTO(false, "Please answer the security question first.");
        }

        String newPassword = input.getNewPassword();
        if (!isStrongPassword(newPassword)) {
            return new OutputDTO(false, "Weak password. Use at least 8 characters with a mix of upper, lower, digit, and special character.");
        }

        String oldPasswordHash = pendingResetUser.profile.getPasswordHash();
        String newPasswordHash;
        try {
            newPasswordHash = EncryptionEngine.hash(newPassword);
        } catch (Exception e) {
            return new OutputDTO(false, "Failed to update password.");
        }

        if (newPasswordHash.equals(oldPasswordHash)) {
            return new OutputDTO(false, "New password must be different from the old password.");
        }

        pendingResetUser.profile.setPasswordHash(newPasswordHash);
        UserRegistry.saveUserToDatabase(pendingResetUser.profile.getUsername());
        pendingResetUser = null;
        resetState = ResetState.NONE;

        return new OutputDTO(true, "Password changed successfully.");
    }

    private OutputDTO exitToRegister() {
        resetState = ResetState.NONE;
        pendingResetUser = null;
        AppStatus.currentMenuType = MenuType.REGISTER;
        return new OutputDTO(true, "Entered Register Menu.");
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

    public static void resetForgotPasswordState() {
        resetState = ResetState.NONE;
        pendingResetUser = null;
    }

    public static boolean isWaitingForNewPassword() {
        return resetState == ResetState.WAITING_FOR_NEW_PASSWORD;
    }
}
