package ecosystem.user;

import java.io.Serializable;
import javax.swing.ImageIcon;

public class Applicant extends User implements Serializable {
    public String fname;
    public String lname;
    public String email;
    public ImageIcon attachment = null;
    public boolean enabled = false;

    public Applicant(String email, String password, String fname, String lname, ImageIcon attachment) {
        super(email, password);
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.attachment = attachment;
    }

    public Applicant(String email, String password, String fname, String lname) {
        this(email, password, fname, lname, null);
    }

    public void accept() {
        enabled = true;
    }

    public boolean validateUniqueness(String username, String email) {
        if (!super.validateUniqueness(username))
            return false;

        return !this.username.equalsIgnoreCase(email);
    }
}