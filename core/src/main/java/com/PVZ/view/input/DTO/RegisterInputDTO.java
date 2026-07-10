package com.PVZ.view.input.DTO;

import com.PVZ.model.enums.commands.RegisterCommand;
import com.PVZ.view.input.InputDTO;

public class RegisterInputDTO implements InputDTO {

    private final RegisterCommand command;

    private final String username;
    private final String password;
    private final String confirmPassword;
    private final String nickname;
    private final String email;
    private final String gender;
    private final Integer questionNumber;
    private final String answer;
    private final String answerConfirm;

    public RegisterInputDTO(RegisterCommand command,
                            String username,
                            String password,
                            String confirmPassword,
                            String nickname,
                            String email,
                            String gender,
                            Integer questionNumber,
                            String answer,
                            String answerConfirm) {

        this.command = command;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.questionNumber = questionNumber;
        this.answer = answer;
        this.answerConfirm = answerConfirm;
    }

    public static RegisterInputDTO invalid() {
        return new RegisterInputDTO(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public RegisterCommand getCommand() {
        return command;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public String getAnswer() {
        return answer;
    }

    public String getAnswerConfirm() {
        return answerConfirm;
    }
}
