package ecosystem.academic;

import java.util.HashMap;

public class Static {
    public static final int CREDITS = 4;

    private static final Object[][] PERSONNEL = {
        { "12345", "Fulana", "Gámez", new String[] { "1001" } },
        { "33111", "John", "David", new String[] { "1002", "1003" } },
        { "2511", "Euler", "Euler", new String[] { "1004", "1005", "1006" } },
        { "95547", "Diana", "Medina", new String[] { "1007", "1008" } },
        { "56130", "Martín", "Moro", new String[] { "1009", "1010" } },
        { "10101", "Paula", "Santander", new String[] { "1011" } },
    };

    private static final Object[][] CURRICULUM = {
        { "1001", "Cultura y medio ambiente", "12345" },
        { "1002", "Tratamiento de aguas", "33111" },
        { "1003", "Cuidados de botánica y bosques", "33111" },
        { "1004", "Hidrografía del Departamento de Nariño", "2511" },
        { "1005", "Análisis histórico del impacto humano e industrial en Nariño", "2511" },
        { "1006", "Reciclaje Inteligente como una forma de vida", "2511" },
        { "1007", "Mejoramiento social y energías renovables", "95547" },
        { "1008", "Ecoempresas: Emprendimiento y naturaleza", "95547" },
        { "1009", "Diseño e implementación de proyectos ecológicos", "56130" },
        { "1010", "Reglamentación ambiental en Colombia", "56130" },
        { "1011", "Etica profesional, conciencia y biología", "10101" }
    };

    public static Professor[] professors;
    public static Subject[] subjects;
    public static HashMap<String, Professor> professorsMap;
    public static HashMap<String, Subject> subjectsMap;

    public static void init() {
        professors = new Professor[PERSONNEL.length];
        professorsMap = new HashMap<>();

        for (int i = 0; i < PERSONNEL.length; i++) {
            Object[] row = PERSONNEL[i];
            professors[i] = new Professor((String) row[0], (String) row[1], (String) row[2], (String[]) row[3]);
            professorsMap.put((String) row[0], professors[i]);
        }

        subjects = new Subject[CURRICULUM.length];
        subjectsMap = new HashMap<>();

        for (int i = 0; i < CURRICULUM.length; i++) {
            Object[] row = CURRICULUM[i];
            subjects[i] = new Subject((String) row[0], (String) row[1], (String) row[2], 4);
            subjectsMap.put((String) row[0], subjects[i]);
        }
    }

    public static Professor getProfessorById(String pid) {
        return professorsMap.get(pid);
    }

    public static Subject getSubjectById(String sid) {
        return subjectsMap.get(sid);
    }
}
