package ecosystem.user;

import ecosystem.util.MD5Hash;
import ecosystem.util.Unique;
import java.io.Serializable;
import java.util.Random;

public class User extends Unique implements Serializable {
    private static Random rnd = new Random();

    public static String generatePassword() {
        String pwd = Integer.toString(rnd.nextInt(10000));
        for (int i = 0; i < 4 - pwd.length(); i++)
            pwd = "0" + pwd;

        return pwd;
    }

    public UserType type;
    public String username;
    protected String pwdHash;

    public User(String username, String password, boolean isEncrypted, UserType type) {
        super(username.toLowerCase());
        this.username = username;
        this.type = type;

        if (!isEncrypted)
            this.pwdHash = MD5Hash.getHash(password);
        else
            this.pwdHash = password;
    }

    public User(String username, String password, UserType type) {
        this(username, password, false, type);
    }

    public User(String username, String password, boolean isEncrypted) {
        this(username, password, isEncrypted, UserType.GENERIC);
    }

    public User(String username, String password) {
        this(username, password, false);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.setId(username.toLowerCase());
    }

    public boolean testPassword(String pwd) {
        return MD5Hash.getHash(pwd).equals(pwdHash);
    }

    public boolean changePassword(String oldPwd, String newPwd) {
        if (testPassword(oldPwd)) {
            this.pwdHash = MD5Hash.getHash(newPwd);
            return true;
        }

        return false;
    }

    public void unsafeChangePassword(String newPwd) {
        this.pwdHash = MD5Hash.getHash(newPwd);
    }

    public String getPasswordHash() {
        return pwdHash;
    }

    public boolean validateUniqueness(String username) {
        return !this.username.equalsIgnoreCase(username);
    }
}