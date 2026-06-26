package view.input.DTO;

import model.enums.LoginCommand;
import view.input.InputDTO;

public class LoginInputDTO implements InputDTO {

    private final LoginCommand command;
    private final String username;
    private final String password;
    private final boolean stayLoggedIn;
    private final String email;
    private final String answer;
    private final String newPassword;

    public LoginInputDTO(LoginCommand command,
                         String username,
                         String password,
                         boolean stayLoggedIn,
                         String email,
                         String answer,
                         String newPassword) {
        this.command = command;
        this.username = username;
        this.password = password;
        this.stayLoggedIn = stayLoggedIn;
        this.email = email;
        this.answer = answer;
        this.newPassword = newPassword;
    }

    public static LoginInputDTO invalid() {
        return new LoginInputDTO(null, null, null, false, null, null, null);
    }

    public LoginCommand getCommand() {
        return command;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isStayLoggedIn() {
        return stayLoggedIn;
    }

    public String getEmail() {
        return email;
    }

    public String getAnswer() {
        return answer;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
