package ecosystem.academic;

import ecosystem.util.DataManager;

public class SubjectManager extends DataManager<Subject> {
    public SubjectManager(String filename) {
        super(filename);
    }

    public void updateProfessor(String initialId, String newId) {
        for (Subject s : list) {
            if (s.getProfessorId().equals(initialId))
                s.setProfessorId(newId);
        }

        update();
    }
}