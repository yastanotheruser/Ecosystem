package ecosystem.user;

import static ecosystem.Ecosystem.subjectManager;
import ecosystem.academic.*;
import java.io.Serializable;
import java.util.ArrayList;

public class AcademicData implements Serializable {
    private int credits;
    public ArrayList<String> subjects;
    public ArrayList<Transaction> transactions;

    public AcademicData() {
        credits = 0;
        subjects = new ArrayList<>();
        transactions = new ArrayList<>();
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
            for (String sid : tr.subjects) {
                subjects.add(sid);
                credits += subjectManager.get(sid).getCredits();
            }
        } else if (tr.type == TransactionType.CANCELLATION) {
            for (String sid : tr.subjects) {
                subjects.remove(sid);
                credits -= subjectManager.get(sid).getCredits();
            }
        }

        return this.transactions.add(tr);
    }

    public boolean enrollInSubject(String subjectId) {
        if (!subjects.add(subjectId))
            return false;

        credits += subjectManager.get(subjectId).getCredits();
        return true;
    }
}