package view.input.DTO;

import model.enums.ProfileCommand;
import view.input.InputDTO;

public class ProfileInputDTO implements InputDTO {

    private final ProfileCommand command;
    private final String username;
    private final String nickname;
    private final String email;
    private final String newPassword;
    private final String oldPassword;

    public ProfileInputDTO(ProfileCommand command,
                           String username,
                           String nickname,
                           String email,
                           String newPassword,
                           String oldPassword) {
        this.command = command;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
    }

    public static ProfileInputDTO invalid() {
        return new ProfileInputDTO(null, null, null, null, null, null);
    }

    public ProfileCommand getCommand() {
        return command;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getOldPassword() {
        return oldPassword;
    }
}
