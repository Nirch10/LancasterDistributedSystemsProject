package server.src.Data;

public class UserToken {
    public User User;
    public String Token;

    public UserToken(server.src.Data.User user, String token) {
        User = user;
        Token = token;
    }
}
