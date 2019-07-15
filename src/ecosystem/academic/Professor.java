package ecosystem.academic;

import ecosystem.util.Unique;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Professor extends Unique implements Serializable {
    private String fname;
    private String lname;
    private ArrayList<String> subjects;

    public Professor(String id, String fname, String lname, ArrayList<String> subjects) {
        super(id);
        this.fname = fname;
        this.lname = lname;
        this.subjects = subjects;
    }

    public Professor(String id, String fname, String lname, String[] subjects) {
        this(id, fname, lname, new ArrayList<>(Arrays.asList(subjects)));
    }

    public Professor(String id, String fname, String lname) {
        this(id, fname, lname, new ArrayList<>());
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String[] getSubjects() {
        return subjects.toArray(new String[subjects.size()]);
    }

    public boolean assignSubject(String id) {
        return subjects.add(id);
    }

    public boolean dropSubject(String id) {
        return subjects.remove(id);
    }

    public String toString() {
        return lname + " " + fname;
    }

    public boolean updateSubject(String oldId, String newId) {
        if (dropSubject(oldId))
            return assignSubject(newId);

        return false;
    }
}
