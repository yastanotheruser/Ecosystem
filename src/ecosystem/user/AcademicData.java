package ecosystem.user;

import static ecosystem.Ecosystem.subjectManager;
import ecosystem.academic.*;
import java.io.Serializable;
import java.util.ArrayList;

public class AcademicData implements Serializable {
    private int credits;
    private final String studentId;
    public final ArrayList<String> subjects;
    public final ArrayList<Transaction> transactions;

    public AcademicData(String studentId) {
        this.studentId = studentId;
        this.credits = 0;
        this.subjects = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

    public int getCredits() {
        return credits;
    }

    public void addSubject(String subjectId) {
        if (subjects.contains(subjectId))
            return;

        credits += subjectManager.get(subjectId).getCredits();
        subjects.add(subjectId);
    }

    public void deleteSubject(String subjectId) {
        if (!subjects.remove(subjectId))
            return;

        credits -= subjectManager.get(subjectId).getCredits();
    }

    public boolean execTransaction(Transaction tr) {
        if (tr.type == TransactionType.ENROLLMENT) {
            for (String sid : tr.subjects)
                enrollInSubject(sid);
        } else if (tr.type == TransactionType.CANCELLATION) {
            for (String sid : tr.subjects)
                cancelSubject(sid);
        }

        return this.transactions.add(tr);
    }

    public String getStudentId() {
        return studentId;
    }

    public void enrollInSubject(String subjectId) {
        Subject s = subjectManager.get(subjectId);
        subjects.add(subjectId);
        s.enrollStudent(studentId);
        credits += s.getCredits();
    }

    public void cancelSubject(String subjectId) {
        subjects.remove(subjectId);
        Subject s = subjectManager.get(subjectId);
        subjects.remove(subjectId);
        s.dropStudent(studentId);
        credits -= s.getCredits();
    }

    public void updateSubject(String initialId, String newId) {
        int index = subjects.indexOf(initialId);
        if (index == -1)
            return;

        subjects.remove(index);
        credits = 0;
        subjects.add(index, newId);

        for (String sid : subjects) {
            Subject s = subjectManager.get(sid);
            credits += s.getCredits();
        }

        for (Transaction tr : transactions) {
            int i = tr.subjects.indexOf(initialId);
            if (i == -1)
                continue;

            tr.subjects.remove(i);
            tr.subjects.add(i, newId);
        }
    }
}
