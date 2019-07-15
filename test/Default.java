/**
 * WARNING: This login system is just plain precarious.
 */

import static ecosystem.Ecosystem.*;
import ecosystem.academic.Professor;
import ecosystem.academic.Subject;
import ecosystem.user.*;
import javax.swing.ImageIcon;

public class Default {
    public static final Class[] userConstructorTypes = new Class[] { String.class, String.class, boolean.class, UserType.class };
    public static final Class[] studentConstructorTypes = new Class[] { String.class, String.class, String.class, String.class, String.class, String.class, String.class, ImageIcon.class };
    public static final Class[] applicantConstructorTypes = new Class[] { String.class, String.class, String.class, String.class, ImageIcon.class };
    public static final Class[] professorConstructorTypes = new Class[] { String.class, String.class, String.class, String[].class };
    public static final Class[] subjectConstructorTypes = new Class[] { String.class, String.class, String.class, int.class };

    public static final ImageIcon doggo = new ImageIcon(Default.class.getResource("resources/doggo.jpg"));
    public static final ImageIcon cedula = new ImageIcon(Default.class.getResource("resources/cedula.jpg"));

    public static final Object[][] admins = {
        { "admin", "202cb962ac59075b964b07152d234b70", true, UserType.ADMIN }
    };

    public static final Object[][] students = {
        { "1", "123", "Juan", "Ibáñez", "1234567890", "juanibanez@gmail.com", "Su casa", doggo }
    };

    private static final Object[][] applicants = {
        { "a@b.cd", "123", "Alexandra", "Bacca Cortez-Delgado", cedula }
    };

    private static final Object[][] personnel = {
        { "12345", "Fulana", "Gámez", new String[] { "1001" } },
        { "33111", "John", "David", new String[] { "1002", "1003" } },
        { "2511", "Euler", "Euler", new String[] { "1004", "1005", "1006" } },
        { "95547", "Diana", "Medina", new String[] { "1007", "1008" } },
        { "56130", "Martín", "Moro", new String[] { "1009", "1010" } },
        { "10101", "Paula", "Santander", new String[] { "1011" } },
    };

    private static final Object[][] curriculum = {
        { "1001", "Cultura y medio ambiente", "12345", 4 },
        { "1002", "Tratamiento de aguas", "33111", 4 },
        { "1003", "Cuidados de botánica y bosques", "33111", 3 },
        { "1004", "Hidrografía del Departamento de Nariño", "2511", 2 },
        { "1005", "Análisis histórico del impacto humano e industrial en Nariño", "2511", 5 },
        { "1006", "Reciclaje Inteligente como una forma de vida", "2511", 4 },
        { "1007", "Mejoramiento social y energías renovables", "95547", 3 },
        { "1008", "Ecoempresas: Emprendimiento y naturaleza", "95547", 3 },
        { "1009", "Diseño e implementación de proyectos ecológicos", "56130", 3 },
        { "1010", "Reglamentación ambiental en Colombia", "56130", 4 },
        { "1011", "Etica profesional, conciencia y biología", "10101",   4 }
    };

    public static void main(String[] args) {
        userManager.getFile().delete();
        subjectManager.getFile().delete();
        professorManager.getFile().delete();
        meta.getFile().delete();
        userManager.clear();
        initDataManagers();
        userManager.addFrom(User.class, userConstructorTypes, admins);
        userManager.addFrom(Student.class, studentConstructorTypes, students);
        userManager.addFrom(Applicant.class, applicantConstructorTypes, applicants);
        userManager.update();
        subjectManager.clear();
        subjectManager.addFrom(Subject.class, subjectConstructorTypes, curriculum);
        subjectManager.update();
        professorManager.clear();
        professorManager.addFrom(Professor.class, professorConstructorTypes, personnel);
        professorManager.update();
    }
}
    