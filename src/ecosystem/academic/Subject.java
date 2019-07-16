package ecosystem.academic;

import ecosystem.user.Student;
import ecosystem.util.Unique;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Subject extends Unique implements Serializable {
    private String name;
    private String professorId;
    private final ArrayList<String> students;
    private int credits;

    public Subject(String id, String name, String professorId, int credits) {
        super(id);
        this.name = name;
        this.professorId = professorId;
        this.credits = credits;
        this.students = new ArrayList<>();
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

    public void enrollStudent(String studentId) {
        students.add(studentId);
    }

    public void dropStudent(String studentId) {
        students.remove(studentId);
    }

    public String[] getStudents() {
        return students.toArray(new String[students.size()]);
    }

    public int getStudentCount() {
        return students.size();
    }

    @Override
    public String toString() {
        return name;
    }
}
