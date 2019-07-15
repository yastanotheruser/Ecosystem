package ecosystem.academic;

import ecosystem.util.Unique;
import java.io.Serializable;

public class Subject extends Unique implements Serializable {
    private String name;
    private String professorId;
    private int credits;

    public Subject(String id, String name, String professorId, int credits) {
        super(id);
        this.name = name;
        this.professorId = professorId;
        this.credits = credits;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfessorId() {
        return professorId;
    }

    public void setProfessorId(String professorId) {
        this.professorId = professorId;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String toString() {
        return name;
    }
}
