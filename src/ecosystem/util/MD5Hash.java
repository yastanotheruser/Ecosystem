package ecosystem.util;

import ecosystem.user.UserManager;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MD5Hash {
    public static String getHash(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] digest = md.digest(str.getBytes());
            BigInteger bigInt = new BigInteger(1, digest);
            String hashText = bigInt.toString(16);

            while (hashText.length() < 32)
                hashText = "0" + hashText;

            return hashText;
        } catch (NullPointerException ex) {
            Logger.getLogger(UserManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(MD5Hash.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
