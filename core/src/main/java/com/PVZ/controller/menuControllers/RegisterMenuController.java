package com.PVZ.controller.menuControllers;

import com.PVZ.database.EncryptionEngine;
import com.PVZ.model.enums.MenuType;
import com.PVZ.model.status.AppStatus;
import com.PVZ.model.user.User;
import com.PVZ.model.user.UserRegistry;
import com.PVZ.view.input.InputDTO;
import com.PVZ.view.input.DTO.RegisterInputDTO;
import com.PVZ.view.output.OutputDTO;

public class RegisterMenuController {

    private static final String[] SECURITY_QUESTIONS = {
            "What is your favorite color?",
            "What is your first pet's name?",
            "What city were you born in?",
            "What is your mother's maiden name?",
            "What is your favorite food?"
    };

    private static PendingRegistration pendingRegistration;

    public OutputDTO handle(InputDTO input) {

        if (!(input instanceof RegisterInputDTO registerInput)) {
            return new OutputDTO(false, "Invalid input.");
        }

        if (registerInput.getCommand() == null) {
            return new OutputDTO(false, "Invalid Command.");
        }

        return switch (registerInput.getCommand()) {
            case REGISTER -> register(registerInput);
            case PICK_QUESTION -> pickQuestion(registerInput);
            case ENTER_LOGIN -> enterLoginMenu();
            case SHOW_CURRENT_MENU -> new OutputDTO(true, AppStatus.currentMenuType.name());
            case EXIT -> new OutputDTO(false, "You are already in Register Menu.");
            default -> new OutputDTO(false, "Invalid Command.");
        };
    }

    private OutputDTO register(RegisterInputDTO input) {
        String username = input.getUsername();
        String password = input.getPassword();
        String confirmPassword = input.getConfirmPassword();
        String nickname = input.getNickname();
        String email = input.getEmail();
        String gender = input.getGender();

        String validationError = validateRegisterInput(username, password, confirmPassword, nickname, email, gender);
        if (validationError != null) {
            return new OutputDTO(false, validationError);
        }

        if (UserRegistry.containsUsername(username)) {
            return new OutputDTO(false, "This username is already taken.");
        }

        try {
            pendingRegistration = new PendingRegistration(
                    username,
                    EncryptionEngine.hash(password),
                    nickname,
                    email,
                    gender
            );
        } catch (Exception e) {
            return new OutputDTO(false, "Failed to process registration.");
        }

        return new OutputDTO(true, buildQuestionPrompt());
    }

    private OutputDTO pickQuestion(RegisterInputDTO input) {
        if (pendingRegistration == null) {
            return new OutputDTO(false, "Please register first.");
        }

        Integer questionNumber = input.getQuestionNumber();
        String answer = input.getAnswer();
        String answerConfirm = input.getAnswerConfirm();

        if (questionNumber == null || questionNumber < 1 || questionNumber > SECURITY_QUESTIONS.length) {
            return new OutputDTO(false, "Invalid security question.");
        }

        if (answer == null || answerConfirm == null || !answer.equals(answerConfirm)) {
            return new OutputDTO(false, "Security answer confirmation doesn't match.");
        }

        try {
            String question = SECURITY_QUESTIONS[questionNumber - 1];
            String answerHash = EncryptionEngine.hash(answer);

            User createdUser = User.createNewUser(
                    pendingRegistration.username,
                    pendingRegistration.passwordHash,
                    pendingRegistration.nickname,
                    pendingRegistration.email,
                    pendingRegistration.gender,
                    question,
                    answerHash
            );

            boolean ok = UserRegistry.register(createdUser);
            pendingRegistration = null;
            if (!ok) {
                return new OutputDTO(false, "Registration failed. See logs.");
            }
            AppStatus.currentMenuType = MenuType.LOGIN;

            return new OutputDTO(true, "Security question saved successfully.\nEntered Login Menu.");
        } catch (Exception e) {
            return new OutputDTO(false, "Failed to save security question.");
        }
    }

    private OutputDTO enterLoginMenu() {
        AppStatus.currentMenuType = MenuType.LOGIN;
        return new OutputDTO(true, "Entered Login Menu.");
    }

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
        if (password.length() < 8) {
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

        if (!localPart.matches("^[A-Za-z0-9](?:[A-Za-z0-9_.-]*[A-Za-z0-9])?$")) {
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

    private String buildQuestionPrompt() {
        StringBuilder builder = new StringBuilder("Choose your security question:\n");
        for (int i = 0; i < SECURITY_QUESTIONS.length; i++) {
            builder.append(i + 1).append(". ").append(SECURITY_QUESTIONS[i]);
            if (i + 1 < SECURITY_QUESTIONS.length) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    private static final class PendingRegistration {
        private final String username;
        private final String passwordHash;
        private final String nickname;
        private final String email;
        private final String gender;

        private PendingRegistration(String username,
                                    String passwordHash,
                                    String nickname,
                                    String email,
                                    String gender) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.nickname = nickname;
            this.email = email;
            this.gender = gender;
        }
    }
}
