package ecosystem;

import ecosystem.dialogs.AddSubjectDialog;
import static ecosystem.Ecosystem.*;
import ecosystem.academic.Professor;
import ecosystem.academic.Subject;
import ecosystem.util.*;
import ecosystem.user.*;
import ecosystem.components.*;
import ecosystem.dialogs.AddProfessorDialog;
import ecosystem.dialogs.PictureDialog;
import ecosystem.event.MouseClickedListener;
import static ecosystem.mailing.SimpleMailApp.emailManager;
import ecosystem.pdf.ReportBuilder;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

public class AdminFrame extends EcosystemFrame {
    private final ImageIcon USER_ICON = new ImageIcon(getClass().getResource("/images/user.png"));

    private JTextComponent[] fields;
    private ToggleMenu menu;
    private NotificationBadge badge;
    private FormValidator validator;
    private File choosenFile;
    private final ArrayList<Student> students;
    private final ArrayList<Applicant> applicants;

    private DefaultTableModel listModel;
    private DefaultTableModel requestsModel;
    private ListSelectionModel requestsSelModel;
    private DefaultTableModel subjectsModel;
    private ListSelectionModel subjectsSelModel;
    private DefaultTableModel professorsModel;
    private ListSelectionModel professorsSelModel;
    private DefaultTableModel financialModel;

    public AdminFrame() {
        super(977, 600);
        students = new ArrayList<>();
        applicants = new ArrayList<>();
        initComponents();
        jPanelMenuBarContainer.add(jMenuBar);
        initTableModels();
        initMenu();
        initValidationData();
        loadTableData();
        loadRequests();
        initRequestsTable();
        loadProgramme();
        updateFinancialData();
        setVisible(true);
    }

    private void initTableModels() {
        listModel = (DefaultTableModel) jTableList.getModel();
        requestsModel = (DefaultTableModel) jTableRequests.getModel();
        requestsSelModel = jTableRequests.getSelectionModel();
        requestsSelModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subjectsModel = (DefaultTableModel) jTableSubjects.getModel();
        subjectsSelModel = jTableSubjects.getSelectionModel();
        professorsModel = (DefaultTableModel) jTableProfessors.getModel();
        professorsSelModel = jTableProfessors.getSelectionModel();
        financialModel = (DefaultTableModel) jTableFinancial.getModel();
    }

    private void initValidationData() {
        jTextFieldId.putClientProperty("validator", RegexValidator.NUMBER_VALIDATOR);
        jTextFieldFname.putClientProperty("validator", RegexValidator.ALPHA_VALIDATOR);
        jTextFieldLname.putClientProperty("validator", RegexValidator.ALPHA_VALIDATOR);
        jTextFieldPhone.putClientProperty("validator", RegexValidator.PHONE_VALIDATOR);
        jTextFieldEmail.putClientProperty("validator", RegexValidator.EMAIL_VALIDATOR);

        jTextFieldId.putClientProperty("required", true);
        jTextFieldFname.putClientProperty("required", true);
        jTextFieldLname.putClientProperty("required", true);
        jTextFieldEmail.putClientProperty("required", true);
        jTextAddress.putClientProperty("required", true);

        fields = new JTextComponent[] {
            jTextFieldId,
            jTextFieldFname,
            jTextFieldLname,
            jTextFieldPhone,
            jTextFieldEmail,
            jTextAddress
        };

        validator = new FormValidator(fields, jButtonRegister);
    }

    private void loadTableData() {
        for (User u : userManager.list) {
            if (!(u instanceof Student))
                continue;

            Student stud = (Student) u;
            listModel.addRow(new Object[] {
                stud.getUsername(),
                stud.getLname(),
                stud.getFname(),
                stud.getEmail()
            });

            students.add(stud);
        }
    }

    private void loadRequests() {
        for (User u : userManager.list) {
            if (!(u instanceof Applicant))
                continue;

            Applicant a = (Applicant) u;
            if (a.enabled)
                continue;

            requestsModel.addRow(new Object[] {
                a.lname,
                a.fname,
                a.getUsername()
            });

            applicants.add(a);
        }
    }

    private void updateBadge() {
        Object requestCountObj = meta.getItem("requestsCount");
        int requestCount = (requestCountObj != null) ? (int) requestCountObj : 0;
        badge.setText(String.valueOf(requestCount));
        badge.setVisible(requestCount > 0);
    }

    private void initMenu() {
        int count = 0;
        TogglePanel[] options = new TogglePanel[5];

        for (JPanel option : new JPanel[] { jPanelRegisterOpt, jPanelFindOpt, jPanelRequestsOpt, jPanelProgrammeOpt, jPanelFinancialOpt })
            options[count++] = (TogglePanel) option;

        JPanel[] views = new JPanel[] { jPanelRegister, jPanelLists, jPanelRequests, jPanelProgramme, jPanelFinancial };
        menu = new ToggleMenu(options, views);
        menu.toggleMenuItem(0);

        Object requestCountObj = meta.getItem("requestCount");
        if (requestCountObj == null)
            meta.setItem("requestCount", 0);

        int requestCount = (requestCountObj != null) ? (int) requestCountObj : 0;
        badge = new NotificationBadge(String.valueOf(requestCount), jPanelBadge, 25, 25);
        badge.setFont(new Font("Trebuchet MS", Font.BOLD, 12));
        badge.setVisible(requestCount > 0);
        jPanelBadge.add(badge);

        menu.addMenuListener(2, (TogglePanel tp, int index) -> {
            meta.setItem("requestCount", 0);
            updateBadge();
        });
    }

    private void initRequestsTable() {
        requestsSelModel.addListSelectionListener((ListSelectionEvent lse) -> {
            if (!lse.getValueIsAdjusting()) {
                int index = requestsSelModel.getMinSelectionIndex();
                if (index == -1) {
                    jLabelAttachment.setIcon(USER_ICON);
                    return;
                }

                ImageIcon attachment = applicants.get(index).attachment;
                if (attachment == null)
                    attachment = USER_ICON;

                jLabelAttachment.setIcon(ImageUtil.resizeImage(attachment, jLabelAttachment));
            }
        });

        jTableRequests.addMouseListener(new MouseClickedListener() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getButton() != MouseEvent.BUTTON1 || me.getClickCount() != 2)
                    return;

                int index = requestsSelModel.getMinSelectionIndex();
                if (index == -1)
                    return;

                Applicant they = applicants.get(index);
                if (they.attachment == null) {
                    if (JOptionPane.showConfirmDialog(null, "El usuario aún no ha confirmado su identidad ¿Desea continuar?",
                        "Alerta", JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION)
                    return;
                }

                String id = JOptionPane.showInputDialog(null, "Ingrese el número de identificación del estudiante:", null);
                if (id == null || !id.matches("^\\d+$")) {
                    if (id != null)
                        JOptionPane.showMessageDialog(null, "Identificación inválida", "Error", JOptionPane.ERROR_MESSAGE);

                    return;
                }

                if (!userManager.updateEntry(they, id)) {
                    JOptionPane.showMessageDialog(null, "El usuario ya existe", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                they.accept();
                userManager.update();
                requestsModel.removeRow(index);
            }
        });

        jTableList.addMouseListener(new MouseClickedListener() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getButton() != MouseEvent.BUTTON1 || me.getClickCount() != 2)
                    return;

                int index = jTableList.getSelectedRow();
                if (index == -1)
                    return;

                Student stud = (Student) students.get(index);
                ImageIcon picture = stud.getPicture();

                if (picture == null)
                    JOptionPane.showMessageDialog(null, "No existe foto de perfil para este usuario", "Ecosystem", JOptionPane.INFORMATION_MESSAGE);
                else
                    new PictureDialog(self, stud.getPicture());
            }
        });
    }

    private void loadProgramme() {
        loadCurriculum();
        loadProfessors();
    }

    private void loadCurriculum() {
        DefaultTableModel model = (DefaultTableModel) jTableSubjects.getModel();
        model.setRowCount(0);

        for (Object o : subjectManager.list) {
            Subject s = (Subject) o;
            model.addRow(new Object[] {
                s.getId(),
                s.getName(),
                s.getProfessorId(),
                s.getCredits()
            });
        }

    }

    private void loadProfessors() {
        DefaultTableModel model = (DefaultTableModel) jTableProfessors.getModel();
        model.setRowCount(0);

        for (Object o : professorManager.list) {
            Professor p = (Professor) o;
            model.addRow(new Object[] {
                p.getId(),
                p.getLname(),
                p.getFname()
            });
        }

    }

    private void updateFinancialData() {
        int count = 0;
        for (User u : userManager.list) {
            if (u instanceof Student)
                count++;
        }

        int credits = 0;
        double total = 0;
        financialModel.setRowCount(0);

        for (Subject s : subjectManager.list) {
            String[] they = s.getStudents();
            double localTotal = Financial.COST_PER_CREDIT * s.getCredits() * they.length;
            credits += they.length * s.getCredits();
            total += localTotal;
            financialModel.addRow(new Object[] { s.getId(), s.getName(), s.getCredits(), s.getStudentCount(), localTotal });
        }

        total += count * Financial.ENROLLMENT_COST;
        jLabelTotalEnrolledStudents.setText(Integer.toString(count));
        jLabelTotalCredits.setText(Integer.toString(credits));
        jLabelTotalProfit.setText("$" + Double.toString(total));
    }

    private void removeSelectedSubjects() {
        if (jTableSubjects.getSelectedRowCount() == 0)
            return;

        boolean askBeforeDeletion = jCheckBoxVerbose.getState();
        if (askBeforeDeletion && JOptionPane.showConfirmDialog(null, "¿Desea eliminar las materias seleccionadas?") != JOptionPane.OK_OPTION)
            return;

        while (jTableSubjects.getSelectedRowCount() > 0) {
            int i = subjectsSelModel.getMinSelectionIndex();
            String subjectId = (String) jTableSubjects.getValueAt(i, 0);
            subjectManager.delete(subjectId, false);
            subjectsModel.removeRow(i);
            professorManager.deleteSubject(subjectId);
        }

        jTableSubjects.clearSelection();
        subjectManager.update();
    }

    private void removeSelectedProfessors() {
        if (jTableProfessors.getSelectedRowCount() == 0)
            return;

        boolean askBeforeDeletion = jCheckBoxVerbose.getState();
        if (askBeforeDeletion && JOptionPane.showConfirmDialog(null, "¿Desea eliminar los docentes seleccionados?") != JOptionPane.OK_OPTION)
            return;

        for (int i : jTableProfessors.getSelectedRows()) {
            String professorId = (String) jTableProfessors.getValueAt(i, 0);
            Professor p = (Professor) professorManager.get(professorId);
            String[] subjects = p.getSubjects();

            if (subjects.length > 0) {
                JOptionPane.showMessageDialog(null, "El docente identificado con cédula " + p.getId() + " aún tiene asignado materias, no es posible eliminarlo", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        while (jTableProfessors.getSelectedRowCount() > 0) {
            int i = professorsSelModel.getMinSelectionIndex();
            String professorId = (String) jTableProfessors.getValueAt(i, 0);
            professorManager.delete(professorId, false);
            professorsModel.removeRow(i);
        }

        jTableProfessors.clearSelection();
        professorManager.update();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar = new javax.swing.JMenuBar();
        jMenuAdd = new javax.swing.JMenu();
        jMenuItemAddSubject = new javax.swing.JMenuItem();
        jMenuItemAddProfessor = new javax.swing.JMenuItem();
        jMenuDelete = new javax.swing.JMenu();
        jMenuItemDeleteSubjects = new javax.swing.JMenuItem();
        jMenuItemDeleteProfessors = new javax.swing.JMenuItem();
        jMenuOptions = new javax.swing.JMenu();
        jCheckBoxVerbose = new javax.swing.JCheckBoxMenuItem();
        jMenuReport = new javax.swing.JMenu();
        jMenuItemSubjectReport = new javax.swing.JMenuItem();
        jPanelMain = new javax.swing.JPanel();
        jLabelState = new javax.swing.JLabel();
        jPanelLeft = new javax.swing.JPanel();
        jPanelMenu = new javax.swing.JPanel();
        jPanelRegisterOpt = new TogglePanel(new Color(44, 93, 59));
        jLabelOpt1 = new javax.swing.JLabel();
        jPanelFindOpt = new TogglePanel(new Color(44, 93, 59));
        jLabelOpt2 = new javax.swing.JLabel();
        jPanelRequestsOpt = new TogglePanel(new Color(44, 93, 59));
        jLabelOpt3 = new javax.swing.JLabel();
        jPanelBadge = new javax.swing.JPanel();
        jPanelProgrammeOpt = new TogglePanel(new Color(44, 93, 59));
        jLabelOpt4 = new javax.swing.JLabel();
        jPanelFinancialOpt = new TogglePanel(new Color(44, 93, 59));
        jLabelOpt5 = new javax.swing.JLabel();
        jPanelLogoutOpt = new TogglePanel(new Color(44, 93, 59));
        jLabelOpt6 = new javax.swing.JLabel();
        jPanelRegister = new javax.swing.JPanel();
        jLabelRequired = new javax.swing.JLabel();
        jLabelId = new javax.swing.JLabel();
        jTextFieldId = new javax.swing.JTextField();
        jLabelFname = new javax.swing.JLabel();
        jTextFieldFname = new javax.swing.JTextField();
        jLabelLname = new javax.swing.JLabel();
        jTextFieldLname = new javax.swing.JTextField();
        jLabelPhone = new javax.swing.JLabel();
        jTextFieldPhone = new javax.swing.JTextField();
        jLabelEmail = new javax.swing.JLabel();
        jTextFieldEmail = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jLabePicture = new javax.swing.JLabel();
        jLabelProfile = new javax.swing.JLabel();
        jButtonBrowse = new javax.swing.JButton();
        jLabelAddress = new javax.swing.JLabel();
        jScrollPaneAddress = new javax.swing.JScrollPane();
        jTextAddress = new javax.swing.JTextArea();
        jButtonRegister = new javax.swing.JButton();
        jPanelLists = new javax.swing.JPanel();
        jLabelSubjectIdStatic = new javax.swing.JLabel();
        jTextFieldSubjectId = new javax.swing.JTextField();
        jButtonFind = new javax.swing.JButton();
        jPanelResult = new javax.swing.JPanel();
        jPanelSubjectInfo = new javax.swing.JPanel();
        jPanelCol1 = new javax.swing.JPanel();
        jPanelName = new javax.swing.JPanel();
        jLabelNameStatic = new javax.swing.JLabel();
        jLabelSubjectName = new javax.swing.JLabel();
        jPanelProfessor = new javax.swing.JPanel();
        jLabelProfessorStatic = new javax.swing.JLabel();
        jLabelSubjectProfessor = new javax.swing.JLabel();
        jPanelCredits = new javax.swing.JPanel();
        jLabelCreditsStatic = new javax.swing.JLabel();
        jLabelCredits = new javax.swing.JLabel();
        jPanelCol2 = new javax.swing.JPanel();
        jPanelCost = new javax.swing.JPanel();
        jLabelCostStatic = new javax.swing.JLabel();
        jLabelCost = new javax.swing.JLabel();
        jPanelEnrollsCount = new javax.swing.JPanel();
        jLabelEnrollsStatic = new javax.swing.JLabel();
        jLabelEnrolls = new javax.swing.JLabel();
        jPanelProfit = new javax.swing.JPanel();
        jLabelProfitStatic = new javax.swing.JLabel();
        jLabelProfit = new javax.swing.JLabel();
        jScrollPaneResults = new javax.swing.JScrollPane();
        jTableSubjectList = new javax.swing.JTable();
        jPanelRequests = new javax.swing.JPanel();
        jLabelAttachment = new javax.swing.JLabel();
        jScrollPaneRequets = new javax.swing.JScrollPane();
        jTableRequests = new javax.swing.JTable();
        jPanelProgramme = new javax.swing.JPanel();
        jPanelMenuBarContainer = new javax.swing.JPanel();
        jPanelTables = new javax.swing.JPanel();
        jScrollPaneSubjects = new javax.swing.JScrollPane();
        jTableSubjects = new javax.swing.JTable();
        jScrollPaneProfessors = new javax.swing.JScrollPane();
        jTableProfessors = new javax.swing.JTable();
        jPanelFinancial = new javax.swing.JPanel();
        jPanelFinancialData = new javax.swing.JPanel();
        jPanelTotalEnrolledStudents = new javax.swing.JPanel();
        jLabelTotalEnrolledStudentsStatic = new javax.swing.JLabel();
        jLabelTotalEnrolledStudents = new javax.swing.JLabel();
        jPanelTotalCredits = new javax.swing.JPanel();
        jLabelTotalCreditsStatic = new javax.swing.JLabel();
        jLabelTotalCredits = new javax.swing.JLabel();
        jPanelTotalProfit = new javax.swing.JPanel();
        jLabelTotalProfilStatic = new javax.swing.JLabel();
        jLabelTotalProfit = new javax.swing.JLabel();
        jScrollPaneFinancial = new javax.swing.JScrollPane();
        jTableFinancial = new javax.swing.JTable();
        jButtonSaveReport = new javax.swing.JButton();
        jScrollPaneList = new javax.swing.JScrollPane();
        jTableList = new javax.swing.JTable();

        jMenuBar.setBackground(new java.awt.Color(58, 155, 83));
        jMenuBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 1));

        jMenuAdd.setLayout(new BoxLayout(jMenuAdd, BoxLayout.PAGE_AXIS));
        jMenuAdd.setForeground(new java.awt.Color(240, 240, 240));
        jMenuAdd.setText("Añadir");

        jMenuItemAddSubject.setText("Asignatura");
        jMenuItemAddSubject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddSubjectActionPerformed(evt);
            }
        });
        jMenuAdd.add(jMenuItemAddSubject);

        jMenuItemAddProfessor.setText("Docente");
        jMenuItemAddProfessor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAddProfessorActionPerformed(evt);
            }
        });
        jMenuAdd.add(jMenuItemAddProfessor);

        jMenuBar.add(jMenuAdd);

        jMenuDelete.setForeground(new java.awt.Color(240, 240, 240));
        jMenuDelete.setText("Eliminar selección");

        jMenuItemDeleteSubjects.setText("Asignaturas");
        jMenuItemDeleteSubjects.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteSubjectsActionPerformed(evt);
            }
        });
        jMenuDelete.add(jMenuItemDeleteSubjects);

        jMenuItemDeleteProfessors.setText("Docentes");
        jMenuItemDeleteProfessors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemDeleteProfessorsActionPerformed(evt);
            }
        });
        jMenuDelete.add(jMenuItemDeleteProfessors);

        jMenuBar.add(jMenuDelete);

        jMenuOptions.setForeground(new java.awt.Color(240, 240, 240));
        jMenuOptions.setText("Opciones");

        jCheckBoxVerbose.setSelected(true);
        jCheckBoxVerbose.setText("Solicitar confirmación antes de eliminar");
        jMenuOptions.add(jCheckBoxVerbose);

        jMenuBar.add(jMenuOptions);

        jMenuReport.setForeground(new java.awt.Color(240, 240, 240));
        jMenuReport.setText("Reportes");

        jMenuItemSubjectReport.setText("Reporte por asignatura");
        jMenuItemSubjectReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSubjectReportActionPerformed(evt);
            }
        });
        jMenuReport.add(jMenuItemSubjectReport);

        jMenuBar.add(jMenuReport);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanelMain.setBackground(new java.awt.Color(34, 63, 49));
        jPanelMain.setInheritsPopupMenu(true);
        jPanelMain.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelState.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jLabelState.setForeground(new java.awt.Color(255, 255, 255));
        jLabelState.setToolTipText("");
        jPanelMain.add(jLabelState, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 20, -1, -1));

        jPanelLeft.setBackground(new java.awt.Color(34, 63, 49));
        jPanelLeft.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanelMenu.setBackground(new java.awt.Color(34, 63, 49));
        jPanelMenu.setLayout(new java.awt.GridLayout(6, 0));

        jPanelRegisterOpt.setBackground(new java.awt.Color(44, 73, 59));
        jPanelRegisterOpt.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        jPanelRegisterOpt.setLayout(new java.awt.GridLayout(1, 0));

        jLabelOpt1.setBackground(new java.awt.Color(44, 73, 59));
        jLabelOpt1.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelOpt1.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOpt1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/dossier.png"))); // NOI18N
        jLabelOpt1.setText("Matricular estudiante");
        jLabelOpt1.setIconTextGap(6);
        jPanelRegisterOpt.add(jLabelOpt1);
        jLabelOpt1.getAccessibleContext().setAccessibleName("");

        jPanelMenu.add(jPanelRegisterOpt);

        jPanelFindOpt.setBackground(new java.awt.Color(44, 73, 59));
        jPanelFindOpt.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        jPanelFindOpt.setLayout(new java.awt.GridLayout(1, 0));

        jLabelOpt2.setBackground(new java.awt.Color(44, 73, 59));
        jLabelOpt2.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelOpt2.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOpt2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/user-icon.png"))); // NOI18N
        jLabelOpt2.setText("Listados por materia");
        jLabelOpt2.setIconTextGap(6);
        jPanelFindOpt.add(jLabelOpt2);
        jLabelOpt2.getAccessibleContext().setAccessibleName("");

        jPanelMenu.add(jPanelFindOpt);

        jPanelRequestsOpt.setBackground(new java.awt.Color(44, 73, 59));
        jPanelRequestsOpt.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        jPanelRequestsOpt.setLayout(new java.awt.BorderLayout());

        jLabelOpt3.setBackground(new java.awt.Color(44, 73, 59));
        jLabelOpt3.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelOpt3.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOpt3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/report-card.png"))); // NOI18N
        jLabelOpt3.setText("Solicitudes");
        jLabelOpt3.setIconTextGap(6);
        jPanelRequestsOpt.add(jLabelOpt3, java.awt.BorderLayout.CENTER);
        jLabelOpt3.getAccessibleContext().setAccessibleName("");

        jPanelBadge.setBackground(new java.awt.Color(44, 73, 59));
        jPanelBadge.setOpaque(false);
        jPanelBadge.setPreferredSize(new java.awt.Dimension(72, 50));

        javax.swing.GroupLayout jPanelBadgeLayout = new javax.swing.GroupLayout(jPanelBadge);
        jPanelBadge.setLayout(jPanelBadgeLayout);
        jPanelBadgeLayout.setHorizontalGroup(
            jPanelBadgeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 72, Short.MAX_VALUE)
        );
        jPanelBadgeLayout.setVerticalGroup(
            jPanelBadgeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        jPanelRequestsOpt.add(jPanelBadge, java.awt.BorderLayout.EAST);

        jPanelMenu.add(jPanelRequestsOpt);

        jPanelProgrammeOpt.setBackground(new java.awt.Color(44, 73, 59));
        jPanelProgrammeOpt.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        jPanelProgrammeOpt.setLayout(new java.awt.GridLayout(1, 0));

        jLabelOpt4.setBackground(new java.awt.Color(44, 73, 59));
        jLabelOpt4.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelOpt4.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOpt4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/manage.png"))); // NOI18N
        jLabelOpt4.setText("Programa académico");
        jLabelOpt4.setIconTextGap(6);
        jPanelProgrammeOpt.add(jLabelOpt4);
        jLabelOpt4.getAccessibleContext().setAccessibleName("");

        jPanelMenu.add(jPanelProgrammeOpt);

        jPanelFinancialOpt.setBackground(new java.awt.Color(44, 73, 59));
        jPanelFinancialOpt.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        jPanelFinancialOpt.setLayout(new java.awt.GridLayout(1, 0));

        jLabelOpt5.setBackground(new java.awt.Color(44, 73, 59));
        jLabelOpt5.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelOpt5.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOpt5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/financial.png"))); // NOI18N
        jLabelOpt5.setText("Reporte financiero");
        jLabelOpt5.setIconTextGap(6);
        jPanelFinancialOpt.add(jLabelOpt5);
        jLabelOpt5.getAccessibleContext().setAccessibleName("");

        jPanelMenu.add(jPanelFinancialOpt);

        jPanelLogoutOpt.setBackground(new java.awt.Color(44, 73, 59));
        jPanelLogoutOpt.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        jPanelLogoutOpt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanelLogoutOptMouseClicked(evt);
            }
        });
        jPanelLogoutOpt.setLayout(new java.awt.GridLayout(1, 0));

        jLabelOpt6.setBackground(new java.awt.Color(44, 73, 59));
        jLabelOpt6.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelOpt6.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOpt6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/back-white.png"))); // NOI18N
        jLabelOpt6.setText("Cerrar sesión");
        jLabelOpt6.setIconTextGap(6);
        jPanelLogoutOpt.add(jLabelOpt6);
        jLabelOpt6.getAccessibleContext().setAccessibleName("");

        jPanelMenu.add(jPanelLogoutOpt);

        jPanelLeft.add(jPanelMenu, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 60, 220, 300));

        jPanelMain.add(jPanelLeft, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 220, 600));

        jPanelRegister.setBackground(new java.awt.Color(58, 155, 83));
        jPanelRegister.setNextFocusableComponent(jTextFieldId);
        jPanelRegister.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelRequired.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelRequired.setForeground(new java.awt.Color(255, 255, 255));
        jLabelRequired.setText("*: Campos requeridos");
        jPanelRegister.add(jLabelRequired, new org.netbeans.lib.awtextra.AbsoluteConstraints(88, 15, -1, -1));

        jLabelId.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelId.setForeground(new java.awt.Color(255, 255, 255));
        jLabelId.setText("Cédula*");
        jPanelRegister.add(jLabelId, new org.netbeans.lib.awtextra.AbsoluteConstraints(37, 55, -1, -1));

        jTextFieldId.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jTextFieldId.setForeground(new java.awt.Color(34, 49, 63));
        jTextFieldId.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jTextFieldId.setNextFocusableComponent(jTextFieldFname);
        jPanelRegister.add(jTextFieldId, new org.netbeans.lib.awtextra.AbsoluteConstraints(132, 50, 160, -1));

        jLabelFname.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelFname.setForeground(new java.awt.Color(255, 255, 255));
        jLabelFname.setText("Nombres*");
        jPanelRegister.add(jLabelFname, new org.netbeans.lib.awtextra.AbsoluteConstraints(37, 98, -1, -1));

        jTextFieldFname.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jTextFieldFname.setForeground(new java.awt.Color(34, 49, 63));
        jTextFieldFname.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jTextFieldFname.setNextFocusableComponent(jTextFieldLname);
        jPanelRegister.add(jTextFieldFname, new org.netbeans.lib.awtextra.AbsoluteConstraints(132, 93, 160, -1));

        jLabelLname.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelLname.setForeground(new java.awt.Color(255, 255, 255));
        jLabelLname.setText("Apellidos*");
        jPanelRegister.add(jLabelLname, new org.netbeans.lib.awtextra.AbsoluteConstraints(37, 141, -1, -1));

        jTextFieldLname.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jTextFieldLname.setForeground(new java.awt.Color(34, 49, 63));
        jTextFieldLname.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jTextFieldLname.setNextFocusableComponent(jTextFieldPhone);
        jPanelRegister.add(jTextFieldLname, new org.netbeans.lib.awtextra.AbsoluteConstraints(132, 136, 160, -1));

        jLabelPhone.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelPhone.setForeground(new java.awt.Color(255, 255, 255));
        jLabelPhone.setText("Teléfono");
        jPanelRegister.add(jLabelPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(37, 184, -1, -1));

        jTextFieldPhone.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jTextFieldPhone.setForeground(new java.awt.Color(34, 49, 63));
        jTextFieldPhone.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jTextFieldPhone.setNextFocusableComponent(jTextFieldEmail);
        jPanelRegister.add(jTextFieldPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(132, 179, 160, -1));

        jLabelEmail.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelEmail.setForeground(new java.awt.Color(255, 255, 255));
        jLabelEmail.setText("E-mail*");
        jPanelRegister.add(jLabelEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(37, 227, -1, -1));

        jTextFieldEmail.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jTextFieldEmail.setForeground(new java.awt.Color(34, 49, 63));
        jTextFieldEmail.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jTextFieldEmail.setNextFocusableComponent(jTextAddress);
        jPanelRegister.add(jTextFieldEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(132, 222, 160, -1));

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanelRegister.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(329, 11, -1, 259));

        jLabePicture.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabePicture.setForeground(new java.awt.Color(255, 255, 255));
        jLabePicture.setText("Foto de perfil");
        jPanelRegister.add(jLabePicture, new org.netbeans.lib.awtextra.AbsoluteConstraints(357, 75, -1, -1));

        jLabelProfile.setBackground(new java.awt.Color(231, 231, 231));
        jLabelProfile.setForeground(new java.awt.Color(240, 240, 240));
        jLabelProfile.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelProfile.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(34, 49, 63)));
        jLabelProfile.setOpaque(true);
        jPanelRegister.add(jLabelProfile, new org.netbeans.lib.awtextra.AbsoluteConstraints(479, 16, 120, 150));

        jButtonBrowse.setBackground(new java.awt.Color(231, 231, 231));
        jButtonBrowse.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jButtonBrowse.setForeground(new java.awt.Color(21, 25, 28));
        jButtonBrowse.setText("Seleccionar");
        jButtonBrowse.setBorder(null);
        jButtonBrowse.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonBrowse.setFocusPainted(false);
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });
        jPanelRegister.add(jButtonBrowse, new org.netbeans.lib.awtextra.AbsoluteConstraints(628, 76, 100, 32));

        jLabelAddress.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelAddress.setForeground(new java.awt.Color(255, 255, 255));
        jLabelAddress.setText("Dirección*");
        jPanelRegister.add(jLabelAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(357, 183, -1, -1));

        jScrollPaneAddress.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPaneAddress.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTextAddress.setColumns(20);
        jTextAddress.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jTextAddress.setRows(1);
        jTextAddress.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jScrollPaneAddress.setViewportView(jTextAddress);

        jPanelRegister.add(jScrollPaneAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(479, 172, 240, 53));

        jButtonRegister.setBackground(new java.awt.Color(231, 231, 231));
        jButtonRegister.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jButtonRegister.setForeground(new java.awt.Color(21, 25, 28));
        jButtonRegister.setText("Matricular");
        jButtonRegister.setBorder(null);
        jButtonRegister.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonRegister.setFocusPainted(false);
        jButtonRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRegisterActionPerformed(evt);
            }
        });
        jPanelRegister.add(jButtonRegister, new org.netbeans.lib.awtextra.AbsoluteConstraints(494, 236, 100, 32));

        jPanelMain.add(jPanelRegister, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 60, 757, 284));

        jPanelLists.setBackground(new java.awt.Color(58, 155, 83));
        jPanelLists.setNextFocusableComponent(jTextFieldId);
        jPanelLists.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelSubjectIdStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabelSubjectIdStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelSubjectIdStatic.setLabelFor(jTextFieldSubjectId);
        jLabelSubjectIdStatic.setText("Código de materia");
        jPanelLists.add(jLabelSubjectIdStatic, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 25, -1, -1));

        jTextFieldSubjectId.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jTextFieldSubjectId.setForeground(new java.awt.Color(34, 49, 63));
        jTextFieldSubjectId.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jTextFieldSubjectId.setNextFocusableComponent(jTextFieldEmail);
        jPanelLists.add(jTextFieldSubjectId, new org.netbeans.lib.awtextra.AbsoluteConstraints(195, 20, 70, -1));

        jButtonFind.setBackground(new java.awt.Color(231, 231, 231));
        jButtonFind.setFont(new java.awt.Font("Trebuchet MS", 0, 12)); // NOI18N
        jButtonFind.setForeground(new java.awt.Color(21, 25, 28));
        jButtonFind.setText("Buscar");
        jButtonFind.setBorder(null);
        jButtonFind.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonFind.setFocusPainted(false);
        jButtonFind.setPreferredSize(new java.awt.Dimension(75, 25));
        jButtonFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFindActionPerformed(evt);
            }
        });
        jPanelLists.add(jButtonFind, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 22, -1, -1));

        jPanelResult.setOpaque(false);
        jPanelResult.setPreferredSize(new java.awt.Dimension(757, 540));
        jPanelResult.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanelSubjectInfo.setOpaque(false);
        jPanelSubjectInfo.setLayout(new java.awt.GridLayout(1, 3, 20, 0));

        jPanelCol1.setOpaque(false);
        jPanelCol1.setLayout(new java.awt.GridLayout(3, 1));

        jPanelName.setOpaque(false);
        jPanelName.setLayout(new java.awt.BorderLayout());

        jLabelNameStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabelNameStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelNameStatic.setText("Nombre: ");
        jPanelName.add(jLabelNameStatic, java.awt.BorderLayout.WEST);
        jLabelNameStatic.getAccessibleContext().setAccessibleName("");

        jLabelSubjectName.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelSubjectName.setForeground(new java.awt.Color(255, 255, 255));
        jPanelName.add(jLabelSubjectName, java.awt.BorderLayout.CENTER);

        jPanelCol1.add(jPanelName);

        jPanelProfessor.setOpaque(false);
        jPanelProfessor.setLayout(new java.awt.BorderLayout());

        jLabelProfessorStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabelProfessorStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelProfessorStatic.setText("Docente: ");
        jPanelProfessor.add(jLabelProfessorStatic, java.awt.BorderLayout.WEST);

        jLabelSubjectProfessor.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelSubjectProfessor.setForeground(new java.awt.Color(255, 255, 255));
        jPanelProfessor.add(jLabelSubjectProfessor, java.awt.BorderLayout.CENTER);

        jPanelCol1.add(jPanelProfessor);

        jPanelCredits.setOpaque(false);
        jPanelCredits.setLayout(new java.awt.BorderLayout());

        jLabelCreditsStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabelCreditsStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelCreditsStatic.setText("Créditos: ");
        jPanelCredits.add(jLabelCreditsStatic, java.awt.BorderLayout.WEST);

        jLabelCredits.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelCredits.setForeground(new java.awt.Color(255, 255, 255));
        jPanelCredits.add(jLabelCredits, java.awt.BorderLayout.CENTER);

        jPanelCol1.add(jPanelCredits);

        jPanelSubjectInfo.add(jPanelCol1);

        jPanelCol2.setOpaque(false);
        jPanelCol2.setLayout(new java.awt.GridLayout(3, 1));

        jPanelCost.setOpaque(false);
        jPanelCost.setLayout(new java.awt.BorderLayout());

        jLabelCostStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabelCostStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelCostStatic.setText("Costo de matrícula: ");
        jPanelCost.add(jLabelCostStatic, java.awt.BorderLayout.WEST);

        jLabelCost.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelCost.setForeground(new java.awt.Color(255, 255, 255));
        jPanelCost.add(jLabelCost, java.awt.BorderLayout.CENTER);

        jPanelCol2.add(jPanelCost);

        jPanelEnrollsCount.setOpaque(false);
        jPanelEnrollsCount.setLayout(new java.awt.BorderLayout());

        jLabelEnrollsStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabelEnrollsStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelEnrollsStatic.setText("Estudiantes matriculados: ");
        jPanelEnrollsCount.add(jLabelEnrollsStatic, java.awt.BorderLayout.WEST);

        jLabelEnrolls.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelEnrolls.setForeground(new java.awt.Color(255, 255, 255));
        jPanelEnrollsCount.add(jLabelEnrolls, java.awt.BorderLayout.CENTER);

        jPanelCol2.add(jPanelEnrollsCount);

        jPanelProfit.setOpaque(false);
        jPanelProfit.setLayout(new java.awt.BorderLayout());

        jLabelProfitStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabelProfitStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelProfitStatic.setText("Total recibido: ");
        jPanelProfit.add(jLabelProfitStatic, java.awt.BorderLayout.WEST);

        jLabelProfit.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelProfit.setForeground(new java.awt.Color(255, 255, 255));
        jPanelProfit.add(jLabelProfit, java.awt.BorderLayout.CENTER);

        jPanelCol2.add(jPanelProfit);

        jPanelSubjectInfo.add(jPanelCol2);

        jPanelResult.add(jPanelSubjectInfo, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 75, 677, 100));

        jScrollPaneResults.setBorder(null);

        jTableSubjectList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Identificación", "Apellidos", "Nombres"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPaneResults.setViewportView(jTableSubjectList);

        jPanelResult.add(jScrollPaneResults, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 200, 757, 340));

        jPanelLists.add(jPanelResult, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jPanelMain.add(jPanelLists, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 60, 757, 540));

        jPanelRequests.setBackground(new java.awt.Color(58, 155, 83));
        jPanelRequests.setNextFocusableComponent(jTextFieldId);
        jPanelRequests.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelAttachment.setBackground(new java.awt.Color(58, 155, 83));
        jLabelAttachment.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/user.png"))); // NOI18N
        jLabelAttachment.setOpaque(true);
        jPanelRequests.add(jLabelAttachment, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 284, 284));

        jScrollPaneRequets.setBackground(new java.awt.Color(58, 155, 83));
        jScrollPaneRequets.setBorder(null);

        jTableRequests.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Apellidos", "Nombres", "E-mail"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPaneRequets.setViewportView(jTableRequests);

        jPanelRequests.add(jScrollPaneRequets, new org.netbeans.lib.awtextra.AbsoluteConstraints(284, 0, 473, 284));

        jPanelMain.add(jPanelRequests, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 60, 757, -1));

        jPanelProgramme.setBackground(new java.awt.Color(58, 155, 83));
        jPanelProgramme.setNextFocusableComponent(jTextFieldId);
        jPanelProgramme.setPreferredSize(new java.awt.Dimension(757, 540));
        jPanelProgramme.setLayout(new java.awt.BorderLayout());

        jPanelMenuBarContainer.setPreferredSize(new java.awt.Dimension(757, 20));
        jPanelMenuBarContainer.setLayout(new java.awt.GridLayout(1, 0));
        jPanelProgramme.add(jPanelMenuBarContainer, java.awt.BorderLayout.PAGE_START);

        jPanelTables.setLayout(new javax.swing.BoxLayout(jPanelTables, javax.swing.BoxLayout.PAGE_AXIS));

        jScrollPaneSubjects.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(58, 155, 83), 1, true), "Asignaturas", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Trebuchet MS", 1, 12))); // NOI18N
        jScrollPaneSubjects.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jTableSubjects.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Nombre", "Docente", "Créditos"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableSubjects.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTableSubjects.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableSubjectsMouseClicked(evt);
            }
        });
        jTableSubjects.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTableSubjectsKeyReleased(evt);
            }
        });
        jScrollPaneSubjects.setViewportView(jTableSubjects);

        jPanelTables.add(jScrollPaneSubjects);

        jScrollPaneProfessors.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(58, 155, 83), 1, true), "Docentes", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Trebuchet MS", 1, 12))); // NOI18N
        jScrollPaneProfessors.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jTableProfessors.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Identificación", "Apellidos", "Nombres"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableProfessors.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTableProfessors.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableProfessorsMouseClicked(evt);
            }
        });
        jTableProfessors.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTableProfessorsKeyPressed(evt);
            }
        });
        jScrollPaneProfessors.setViewportView(jTableProfessors);

        jPanelTables.add(jScrollPaneProfessors);

        jPanelProgramme.add(jPanelTables, java.awt.BorderLayout.CENTER);

        jPanelMain.add(jPanelProgramme, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 60, 757, 540));

        jPanelFinancial.setBackground(new java.awt.Color(58, 155, 83));
        jPanelFinancial.setNextFocusableComponent(jTextFieldId);
        jPanelFinancial.setPreferredSize(new java.awt.Dimension(757, 540));
        jPanelFinancial.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanelFinancialData.setOpaque(false);
        jPanelFinancialData.setPreferredSize(new java.awt.Dimension(657, 100));
        jPanelFinancialData.setLayout(new java.awt.GridLayout(3, 0));

        jPanelTotalEnrolledStudents.setOpaque(false);
        jPanelTotalEnrolledStudents.setLayout(new javax.swing.BoxLayout(jPanelTotalEnrolledStudents, javax.swing.BoxLayout.LINE_AXIS));

        jLabelTotalEnrolledStudentsStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabelTotalEnrolledStudentsStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelTotalEnrolledStudentsStatic.setLabelFor(jTextFieldSubjectId);
        jLabelTotalEnrolledStudentsStatic.setText("Total de estudiantes matriculados: ");
        jPanelTotalEnrolledStudents.add(jLabelTotalEnrolledStudentsStatic);

        jLabelTotalEnrolledStudents.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelTotalEnrolledStudents.setForeground(new java.awt.Color(255, 255, 255));
        jLabelTotalEnrolledStudents.setLabelFor(jTextFieldSubjectId);
        jLabelTotalEnrolledStudents.setToolTipText("");
        jPanelTotalEnrolledStudents.add(jLabelTotalEnrolledStudents);

        jPanelFinancialData.add(jPanelTotalEnrolledStudents);

        jPanelTotalCredits.setOpaque(false);
        jPanelTotalCredits.setLayout(new javax.swing.BoxLayout(jPanelTotalCredits, javax.swing.BoxLayout.LINE_AXIS));

        jLabelTotalCreditsStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabelTotalCreditsStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelTotalCreditsStatic.setLabelFor(jTextFieldSubjectId);
        jLabelTotalCreditsStatic.setText("Total de creditos matriculados: ");
        jPanelTotalCredits.add(jLabelTotalCreditsStatic);

        jLabelTotalCredits.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelTotalCredits.setForeground(new java.awt.Color(255, 255, 255));
        jLabelTotalCredits.setLabelFor(jTextFieldSubjectId);
        jLabelTotalCredits.setToolTipText("");
        jPanelTotalCredits.add(jLabelTotalCredits);

        jPanelFinancialData.add(jPanelTotalCredits);

        jPanelTotalProfit.setOpaque(false);
        jPanelTotalProfit.setLayout(new javax.swing.BoxLayout(jPanelTotalProfit, javax.swing.BoxLayout.LINE_AXIS));

        jLabelTotalProfilStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabelTotalProfilStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelTotalProfilStatic.setLabelFor(jTextFieldSubjectId);
        jLabelTotalProfilStatic.setText("Total recibido: ");
        jPanelTotalProfit.add(jLabelTotalProfilStatic);

        jLabelTotalProfit.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabelTotalProfit.setForeground(new java.awt.Color(255, 255, 255));
        jLabelTotalProfit.setLabelFor(jTextFieldSubjectId);
        jLabelTotalProfit.setToolTipText("");
        jPanelTotalProfit.add(jLabelTotalProfit);

        jPanelFinancialData.add(jPanelTotalProfit);

        jPanelFinancial.add(jPanelFinancialData, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 20, -1, -1));

        jScrollPaneFinancial.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Lista de asignaturas", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Trebuchet MS", 0, 11))); // NOI18N
        jScrollPaneFinancial.setPreferredSize(new java.awt.Dimension(717, 300));

        jTableFinancial.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Materia", "Créditos", "Estudiantes", "Total recibido"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPaneFinancial.setViewportView(jTableFinancial);

        jPanelFinancial.add(jScrollPaneFinancial, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 135, -1, -1));

        jButtonSaveReport.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jButtonSaveReport.setText("Guardar reporte");
        jButtonSaveReport.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonSaveReport.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonSaveReport.setFocusPainted(false);
        jButtonSaveReport.setPreferredSize(new java.awt.Dimension(150, 32));
        jButtonSaveReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveReportActionPerformed(evt);
            }
        });
        jPanelFinancial.add(jButtonSaveReport, new org.netbeans.lib.awtextra.AbsoluteConstraints(304, 470, -1, -1));

        jPanelMain.add(jPanelFinancial, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 60, 757, 540));

        jScrollPaneList.setBorder(null);

        jTableList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Identificación", "Apellidos", "Nombres", "E-mail"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPaneList.setViewportView(jTableList);

        jPanelMain.add(jScrollPaneList, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 344, 757, 256));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRegisterActionPerformed
        if (!validator.validateForm())
            return;

        String id = jTextFieldId.getText();
        String pwd = User.generatePassword();
        String fname = jTextFieldFname.getText();
        String lname = jTextFieldLname.getText();
        String phone = jTextFieldPhone.getText();
        String email = jTextFieldEmail.getText();
        String address = jTextAddress.getText();
        ImageIcon picture = (ImageIcon) jLabelProfile.getIcon();

        Student student = new Student(id, pwd, fname, lname, phone, email, address, picture);
        if (!userManager.add(student)) {
            JOptionPane.showMessageDialog(null, "El usuario ya se encuentra registrado", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        students.add(student);
        jLabelState.setText("El estudiante fue matriculado exitosamente");
        emailManager.send("noresponder@sm.ecosystem.co", student.getEmail(), "Se efectuó su registro en el sistema de matriculas Ecosystem.\nDebe iniciar sesión con su número de documento y la Su contraseña " + pwd + ".");
        validator.clear();
        jLabelProfile.setIcon(null);
        choosenFile = null;

        final DefaultTableModel model = (DefaultTableModel) jTableList.getModel();
        model.addRow(new Object[] {
            student.getUsername(),
            student.getLname(),
            student.getFname(),
            student.getEmail()
        });
    }//GEN-LAST:event_jButtonRegisterActionPerformed

    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        File file = imageChooser.chooseFile();
        if (file == null)
            return;

        jLabelProfile.setIcon(ImageUtil.resizeImage(file.getAbsolutePath(), jLabelProfile));
        choosenFile = file;
    }//GEN-LAST:event_jButtonBrowseActionPerformed

    private void jPanelLogoutOptMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanelLogoutOptMouseClicked
        userManager.logout();
        new SigninFrame();
        this.dispose();
    }//GEN-LAST:event_jPanelLogoutOptMouseClicked

    private void jButtonFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFindActionPerformed
        String id = jTextFieldSubjectId.getText();
        Subject subj = subjectManager.get(id);

        if (subj == null) {
            JOptionPane.showMessageDialog(null, "No se encontró la materia", "Ecosystem", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Professor prof = professorManager.get(subj.getProfessorId());
        jLabelSubjectName.setText(subj.getName());
        jLabelSubjectName.setToolTipText(subj.getName());
        jLabelSubjectProfessor.setText(prof.toString());
        jLabelSubjectProfessor.setToolTipText(prof.toString());
        DefaultTableModel model = (DefaultTableModel) jTableSubjectList.getModel();
        model.setRowCount(0);

        int count = 0;
        for (User u : userManager.list) {
            if (!(u instanceof Student))
                continue;

            Student s = (Student) u;
            AcademicData academic = s.getAcademicData();

            if (!academic.subjects.contains(id))
                continue;

            model.addRow(new Object[] {
                s.getUsername(),
                s.getLname(),
                s.getFname()
            });

            count++;
        }

        double cost = Financial.COST_PER_CREDIT * subj.getCredits();
        jLabelEnrolls.setText(Integer.toString(count));
        jLabelProfit.setText("$" + Double.toString(cost * count));
        jLabelCredits.setText(Integer.toString(subj.getCredits()));
        jLabelCost.setText("$" + Double.toString(cost));
        JOptionPane.showMessageDialog(null, "Búsqueda finalizada, se encontraron " + count + " registros", "Ecosystem", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButtonFindActionPerformed

    private void jMenuItemAddSubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddSubjectActionPerformed
        new AddSubjectDialog(this);
        loadProgramme();
    }//GEN-LAST:event_jMenuItemAddSubjectActionPerformed

    private void jMenuItemAddProfessorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAddProfessorActionPerformed
        new AddProfessorDialog(this);
        loadProgramme();
    }//GEN-LAST:event_jMenuItemAddProfessorActionPerformed

    private void jTableSubjectsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTableSubjectsKeyReleased
        if (evt.getKeyCode() == 127)
            removeSelectedSubjects();
    }//GEN-LAST:event_jTableSubjectsKeyReleased

    private void jMenuItemDeleteSubjectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteSubjectsActionPerformed
        removeSelectedSubjects();
        loadProfessors();
    }//GEN-LAST:event_jMenuItemDeleteSubjectsActionPerformed

    private void jMenuItemDeleteProfessorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDeleteProfessorsActionPerformed
        removeSelectedProfessors();
    }//GEN-LAST:event_jMenuItemDeleteProfessorsActionPerformed

    private void jTableSubjectsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableSubjectsMouseClicked
        if (evt.getClickCount() != 2)
            return;

        String subjectId = (String) jTableSubjects.getValueAt(jTableSubjects.getSelectedRow(), 0);
        new AddSubjectDialog(this, subjectId);
        loadProgramme();
    }//GEN-LAST:event_jTableSubjectsMouseClicked

    private void jTableProfessorsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTableProfessorsMouseClicked
        if (evt.getClickCount() != 2)
            return;

        String professorId = (String) jTableProfessors.getValueAt(jTableProfessors.getSelectedRow(), 0);
        new AddProfessorDialog(this, professorId);
        loadProgramme();
    }//GEN-LAST:event_jTableProfessorsMouseClicked

    private void jTableProfessorsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTableProfessorsKeyPressed
        if (evt.getKeyCode() == 127)
            removeSelectedProfessors();
    }//GEN-LAST:event_jTableProfessorsKeyPressed

    private void jMenuItemSubjectReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSubjectReportActionPerformed
        int index = subjectsSelModel.getMinSelectionIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione una materia para continuar", "Ecosystem", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        subjectsSelModel.setSelectionInterval(index, index);
        jTextFieldSubjectId.setText((String) jTableSubjects.getValueAt(index, 0));
        jButtonFind.doClick();
        menu.toggleMenuItem(1);
    }//GEN-LAST:event_jMenuItemSubjectReportActionPerformed

    private void jButtonSaveReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveReportActionPerformed
        try {
            File file = pdfChooser.chooseFile();
            if (file == null)
                return;

            if (!file.toString().endsWith(".pdf"))
                file = new File(file + ".pdf");

            ReportBuilder.buildFinancialReport(file);
            if (JOptionPane.showConfirmDialog(null, "¿Desea abrir el archivo?", "Ecosystem", JOptionPane.INFORMATION_MESSAGE) != JOptionPane.OK_OPTION)
                return;

            Desktop desktop = Desktop.getDesktop();
            desktop.open(file);
        } catch (IOException ex) {
            Logger.getLogger(StudentFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButtonSaveReportActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new AdminFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonFind;
    private javax.swing.JButton jButtonRegister;
    private javax.swing.JButton jButtonSaveReport;
    private javax.swing.JCheckBoxMenuItem jCheckBoxVerbose;
    private javax.swing.JLabel jLabePicture;
    private javax.swing.JLabel jLabelAddress;
    private javax.swing.JLabel jLabelAttachment;
    private javax.swing.JLabel jLabelCost;
    private javax.swing.JLabel jLabelCostStatic;
    private javax.swing.JLabel jLabelCredits;
    private javax.swing.JLabel jLabelCreditsStatic;
    private javax.swing.JLabel jLabelEmail;
    private javax.swing.JLabel jLabelEnrolls;
    private javax.swing.JLabel jLabelEnrollsStatic;
    private javax.swing.JLabel jLabelFname;
    private javax.swing.JLabel jLabelId;
    private javax.swing.JLabel jLabelLname;
    private javax.swing.JLabel jLabelNameStatic;
    private javax.swing.JLabel jLabelOpt1;
    private javax.swing.JLabel jLabelOpt2;
    private javax.swing.JLabel jLabelOpt3;
    private javax.swing.JLabel jLabelOpt4;
    private javax.swing.JLabel jLabelOpt5;
    private javax.swing.JLabel jLabelOpt6;
    private javax.swing.JLabel jLabelPhone;
    private javax.swing.JLabel jLabelProfessorStatic;
    private javax.swing.JLabel jLabelProfile;
    private javax.swing.JLabel jLabelProfit;
    private javax.swing.JLabel jLabelProfitStatic;
    private javax.swing.JLabel jLabelRequired;
    private javax.swing.JLabel jLabelState;
    private javax.swing.JLabel jLabelSubjectIdStatic;
    private javax.swing.JLabel jLabelSubjectName;
    private javax.swing.JLabel jLabelSubjectProfessor;
    private javax.swing.JLabel jLabelTotalCredits;
    private javax.swing.JLabel jLabelTotalCreditsStatic;
    private javax.swing.JLabel jLabelTotalEnrolledStudents;
    private javax.swing.JLabel jLabelTotalEnrolledStudentsStatic;
    private javax.swing.JLabel jLabelTotalProfilStatic;
    private javax.swing.JLabel jLabelTotalProfit;
    private javax.swing.JMenu jMenuAdd;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuDelete;
    private javax.swing.JMenuItem jMenuItemAddProfessor;
    private javax.swing.JMenuItem jMenuItemAddSubject;
    private javax.swing.JMenuItem jMenuItemDeleteProfessors;
    private javax.swing.JMenuItem jMenuItemDeleteSubjects;
    private javax.swing.JMenuItem jMenuItemSubjectReport;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JMenu jMenuReport;
    private javax.swing.JPanel jPanelBadge;
    private javax.swing.JPanel jPanelCol1;
    private javax.swing.JPanel jPanelCol2;
    private javax.swing.JPanel jPanelCost;
    private javax.swing.JPanel jPanelCredits;
    private javax.swing.JPanel jPanelEnrollsCount;
    private javax.swing.JPanel jPanelFinancial;
    private javax.swing.JPanel jPanelFinancialData;
    private javax.swing.JPanel jPanelFinancialOpt;
    private javax.swing.JPanel jPanelFindOpt;
    private javax.swing.JPanel jPanelLeft;
    private javax.swing.JPanel jPanelLists;
    private javax.swing.JPanel jPanelLogoutOpt;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelMenu;
    private javax.swing.JPanel jPanelMenuBarContainer;
    private javax.swing.JPanel jPanelName;
    private javax.swing.JPanel jPanelProfessor;
    private javax.swing.JPanel jPanelProfit;
    private javax.swing.JPanel jPanelProgramme;
    private javax.swing.JPanel jPanelProgrammeOpt;
    private javax.swing.JPanel jPanelRegister;
    private javax.swing.JPanel jPanelRegisterOpt;
    private javax.swing.JPanel jPanelRequests;
    private javax.swing.JPanel jPanelRequestsOpt;
    private javax.swing.JPanel jPanelResult;
    private javax.swing.JPanel jPanelSubjectInfo;
    private javax.swing.JPanel jPanelTables;
    private javax.swing.JPanel jPanelTotalCredits;
    private javax.swing.JPanel jPanelTotalEnrolledStudents;
    private javax.swing.JPanel jPanelTotalProfit;
    private javax.swing.JScrollPane jScrollPaneAddress;
    private javax.swing.JScrollPane jScrollPaneFinancial;
    private javax.swing.JScrollPane jScrollPaneList;
    private javax.swing.JScrollPane jScrollPaneProfessors;
    private javax.swing.JScrollPane jScrollPaneRequets;
    private javax.swing.JScrollPane jScrollPaneResults;
    private javax.swing.JScrollPane jScrollPaneSubjects;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTableFinancial;
    private javax.swing.JTable jTableList;
    private javax.swing.JTable jTableProfessors;
    private javax.swing.JTable jTableRequests;
    private javax.swing.JTable jTableSubjectList;
    private javax.swing.JTable jTableSubjects;
    private javax.swing.JTextArea jTextAddress;
    private javax.swing.JTextField jTextFieldEmail;
    private javax.swing.JTextField jTextFieldFname;
    private javax.swing.JTextField jTextFieldId;
    private javax.swing.JTextField jTextFieldLname;
    private javax.swing.JTextField jTextFieldPhone;
    private javax.swing.JTextField jTextFieldSubjectId;
    // End of variables declaration//GEN-END:variables
}
