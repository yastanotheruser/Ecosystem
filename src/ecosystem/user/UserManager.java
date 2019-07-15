package ecosystem.user;

import ecosystem.util.DataManager;

public class UserManager extends DataManager<User> {
    private User loggedInUser;

    public UserManager(String filename) {
        super(filename);
        loggedInUser = null;
    }

    public LoginResult login(String username, String password) {
        User u = this.get(username.toLowerCase());
        if (u == null)
            return LoginResult.NO_SUCH_USER;

        if (!u.testPassword(password))
            return LoginResult.WRONG_PASSWORD;

        loggedInUser = u;
        return LoginResult.SUCCESS;
    }

    public void logout() {
        loggedInUser = null;
    }

    public User getCurrentUser() {
        return loggedInUser;
    }

    @Override
    public boolean updateEntry(User u, String newUsername) {
        if (!super.updateEntry(u, newUsername.toLowerCase(), false))
            return false;

        u.setUsername(newUsername);
        return update();
    }

    public static void main(String[] args) {
        new UserManager("users.dat");
    }
}