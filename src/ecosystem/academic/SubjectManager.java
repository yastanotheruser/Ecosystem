package ecosystem.academic;

import ecosystem.util.DataManager;

public class SubjectManager extends DataManager<Subject> {
    public SubjectManager(String filename) {
        super(filename);
    }

    public void updateProfessor(String newId) {
        for (Subject s : list)
            s.setProfessorId(newId);

        update();
    }
}