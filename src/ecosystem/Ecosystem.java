package ecosystem;

import ecosystem.academic.*;
import ecosystem.mailing.SimpleMailApp;
import ecosystem.user.UserManager;
import ecosystem.util.FileChooser;
import java.io.IOException;

public class Ecosystem {
    public static final UserManager userManager = new UserManager("users");
    public static final SubjectManager subjectManager = new SubjectManager("subjects");
    public static final ProfessorManager professorManager = new ProfessorManager("professors");
    public static final MetaManager meta = new MetaManager("ecosystem");
    public static final FileChooser imageChooser = new FileChooser(null, "Imágenes", "jpg", "png", "gif");

    public static void initDataManagers() {
        userManager.init();
        subjectManager.init();
        professorManager.init();
        meta.init();
    }

    public static void main(String[] args) throws IOException {
        initDataManagers();
        new SimpleMailApp();
        new LoadFrame();
    }
}