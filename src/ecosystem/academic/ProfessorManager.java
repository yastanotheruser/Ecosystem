package ecosystem.academic;

import ecosystem.util.DataManager;

public class ProfessorManager extends DataManager<Professor> {
    public ProfessorManager(String filename) {
        super(filename);
    }

    public void deleteSubject(String subjectId) {
        for (Professor p : list)
            p.dropSubject(subjectId);

        update();
    }

    public void updateSubject(String initialId, String newId) {
        if (newId.equals(initialId))
            return;

        for (Professor p : list)
            p.updateSubject(initialId, newId);

        update();
    }
}
