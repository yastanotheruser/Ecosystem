package ecosystem.user;

import java.io.Serializable;
import javax.swing.ImageIcon;

public class Student extends User implements Serializable {
    private String fname, lname, phone, email, address;
    private ImageIcon picture;
    private boolean requiresPasswordChange = true;
    private AcademicData academic;
    private AcademicState state;
    private double payment;

    private static String toNameCase(String str) {
        String[] arr = str.split(" ");
        StringBuilder sb = new StringBuilder();

        for (String arr1 : arr)
            sb.append(Character.toUpperCase(arr1.charAt(0))).append(arr1.substring(1)).append(" ");

        return sb.toString().trim();
    }

    public Student(String id, String password, boolean isEncrypted, String fname, String lname, String phone,
                   String email, String address, ImageIcon picture) {
        super(id, password, isEncrypted);
        this.fname = toNameCase(fname);
        this.lname = toNameCase(lname);
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.picture = picture;
        this.academic = new AcademicData();
        this.state = AcademicState.BEFORE_ENROLL;
        this.payment = 0;
    }

    public Student(String id, String password, String fname, String lname, String phone, String email, String address, ImageIcon picture) {
        this(id, password, false, fname, lname, phone, email, address, picture);
    }

    public Student(String id, String password, String fname, String lname, String phone, String email, String address) {
        this(id, password, fname, lname, phone, email, address, null);
    }

    public Student(String id, String fname, String lname, String phone, String email, String address) {
        this(id, User.generatePassword(), fname, lname, phone, email, address, null);
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFname() {
        return fname;
    }

    public String getLname() {
        return lname;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public ImageIcon getPicture() {
        return picture;
    }

    public void setPicture(ImageIcon picture) {
        this.picture = picture;
    }

    public AcademicData getAcademicData() {
        return academic;
    }

    public AcademicState getState() {
        return state;
    }

    public void setState(AcademicState state) {
        this.state = state;
    }

    public boolean validateUniqueness(String username, String email) {
        if (!super.validateUniqueness(username))
            return false;

        return !this.email.equalsIgnoreCase(email);
    }

    public double getPayment() {
        return payment;
    }

    public void setPayment(double payment) {
        this.payment = payment;
    }
}