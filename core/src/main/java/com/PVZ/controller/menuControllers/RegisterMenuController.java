package com.PVZ.controller.menuControllers;

import com.PVZ.database.EncryptionEngine;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.network.client.NetworkSession;
import com.PVZ.view.input.DTO.RegisterInputDTO;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.output.OutputDTO;

public class RegisterMenuController {

    private static final String[] SECURITY_QUESTIONS = {
        "What is your favorite color?",
        "What is your first pet's name?",
        "What city were you born in?",
        "What is your mother's maiden name?",
        "What is your favorite food?"
    };

    public OutputDTO handle(InputDTO input) {
        if (!(input instanceof RegisterInputDTO registerInput)) {
            return new OutputDTO(false, "Invalid input.");
        }

        if (registerInput.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        return switch (registerInput.getCommand()) {
            case REGISTER -> register(registerInput);
            case ENTER_LOGIN -> enterLoginMenu();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> new OutputDTO(false, "You are already in Register Menu.");
            default -> new OutputDTO(false, "Invalid Command.");
        };
    }

    /**
     * ثبت‌نام یک‌مرحله‌ای.
     * در صورت موفقیت، کاربر را مستقیماً وارد بازی می‌کند (منوی اصلی).
     */
    private OutputDTO register(RegisterInputDTO input) {
        // ----- ۱. اعتبارسنجی فیلدهای اصلی -----
        String username = input.getUsername();
        String password = input.getPassword();
        String confirmPassword = input.getConfirmPassword();
        String nickname = input.getNickname();
        String email = input.getEmail();
        String gender = input.getGender();

        String error = validateRegisterInput(username, password, confirmPassword, nickname, email, gender);
        if (error != null) {
            return new OutputDTO(false, error);
        }

        // ----- ۲. اعتبارسنجی سؤال امنیتی -----
        Integer questionNumber = input.getQuestionNumber();
        String answer = input.getAnswer();
        String answerConfirm = input.getAnswerConfirm();

        if (questionNumber == null || questionNumber < 1 || questionNumber > SECURITY_QUESTIONS.length) {
            return new OutputDTO(false, "Invalid security question.");
        }
        if (answer == null || answerConfirm == null || !answer.equals(answerConfirm)) {
            return new OutputDTO(false, "Security answer confirmation doesn't match.");
        }
        String question = SECURITY_QUESTIONS[questionNumber - 1];

        // فاز شبکه: یکتایی username و ذخیره‌سازی نهایی باید سمت سرور
        // انجام شود (سند فاز سوم). اگر به سرور وصلیم از همان مسیر
        // می‌رویم؛ در غیر این صورت (تست/توسعه‌ی آفلاین) به منطق محلی
        // قبلی برمی‌گردیم.
        if (NetworkSession.isConnected()) {
            return registerOverNetwork(username, password, nickname, email, gender, question, answer);
        }
        return registerLocally(username, password, nickname, email, gender, question, answer);
    }

    private OutputDTO registerOverNetwork(String username, String password, String nickname,
                                           String email, String gender,
                                           String question, String answer) {
        NetworkSession.AuthResult result = NetworkSession.register(
                username, password, nickname, email, gender, question, answer);
        if (!result.success()) {
            return new OutputDTO(false, result.message());
        }
        AppStatus.currentUser = result.user();
        UserRegistry.cacheUser(AppStatus.currentUser);
        AppStatus.currentMenuType = MenuType.MAIN;
        return new OutputDTO(true, "Registration successful. Welcome, " + nickname + "!");
    }

    private OutputDTO registerLocally(String username, String password, String nickname,
                                       String email, String gender,
                                       String question, String answer) {
        if (UserRegistry.containsUsername(username)) {
            return new OutputDTO(false, "This username is already taken.");
        }
        try {
            String passwordHash = EncryptionEngine.hash(password);
            String answerHash = EncryptionEngine.hash(answer);

            User createdUser = User.createNewUser(
                username, passwordHash, nickname, email, gender,
                question, answerHash
            );

            boolean ok = UserRegistry.register(createdUser);
            if (!ok) {
                return new OutputDTO(false, "Registration failed. See logs.");
            }

            AppStatus.currentUser = createdUser;
            AppStatus.currentMenuType = MenuType.MAIN;

            return new OutputDTO(true, "Registration successful. Welcome, " + nickname + "!");

        } catch (Exception e) {
            return new OutputDTO(false, "Failed to process registration.");
        }
    }

    private OutputDTO enterLoginMenu() {
        AppStatus.currentMenuType = MenuType.LOGIN;
        return new OutputDTO(true, "Entered Login Menu.");
    }

    // ======================== توابع اعتبارسنجی (بدون تغییر) ========================

    private String validateRegisterInput(String username,
                                         String password,
                                         String confirmPassword,
                                         String nickname,
                                         String email,
                                         String gender) {

        if (username == null || !username.matches("^[A-Za-z0-9-]+$")) {
            return "Invalid username.";
        }

        if (password == null || !isStrongPassword(password)) {
            return "Weak password.";
        }

        if (!password.equals(confirmPassword)) {
            return "Password confirmation doesn't match.";
        }

        if (nickname == null || nickname.length() < 3 || nickname.length() > 30) {
            return "Invalid nickname length.";
        }

        if (email == null || !isValidEmail(email)) {
            return "Invalid email.";
        }

        if (gender == null || !(gender.equalsIgnoreCase("male") || gender.equalsIgnoreCase("female"))) {
            return "Invalid gender.";
        }

        return null;
    }

    private boolean isStrongPassword(String password) {
        if (password.length() < 8) return false;

        boolean hasLower = false, hasUpper = false, hasDigit = false, hasSpecial = false;
        String specialChars = "!#$%^&*()=+{}[]|/\\:;'\" ,><?";

        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);
            if (Character.isLowerCase(ch)) hasLower = true;
            else if (Character.isUpperCase(ch)) hasUpper = true;
            else if (Character.isDigit(ch)) hasDigit = true;
            else if (specialChars.indexOf(ch) >= 0) hasSpecial = true;
        }

        return hasLower && hasUpper && hasDigit && hasSpecial;
    }

    private boolean isValidEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex != email.lastIndexOf('@')) return false;

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex + 1);

        if (!localPart.matches("^[A-Za-z0-9](?:[A-Za-z0-9_.-]*[A-Za-z0-9])?$")) return false;
        if (localPart.contains("..")) return false;
        if (!domainPart.matches("^[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$")) return false;

        String[] labels = domainPart.split("\\.");
        String tld = labels[labels.length - 1];
        return tld.length() >= 2;
    }
}
