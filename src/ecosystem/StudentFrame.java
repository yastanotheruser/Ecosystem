package ecosystem;

import static ecosystem.Ecosystem.*;
import ecosystem.academic.*;
import ecosystem.components.*;
import ecosystem.event.MouseClickedListener;
import ecosystem.user.*;
import ecosystem.util.FormValidator;
import ecosystem.util.ImageUtil;
import ecosystem.util.RegexValidator;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.File;
import static java.lang.Integer.min;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;

public class StudentFrame extends EcosystemFrame {
    private ToggleMenu menu;
    private User user;
    private Applicant appl;
    private Student stud;
    private ImageIcon choosenImage = null;

    private JTextComponent[] submitFields = null;
    private FormValidator submitValidator = null;

    private JTable manageTable;
    private DefaultTableModel manageTableModel;
    private ArrayList<Integer> subjectIndices;
    private int subjectCount;
    private int credits;
    private AcademicData academic;

    public StudentFrame() {
        super();
        initComponents();
        jLabelCreditsInfo.setVisible(false);
        initMenu();
        loadLoginData();
        initValidationData();

        if (stud == null) {
            setVisible(true);
            return;
        }

        ImageIcon picture = stud.getPicture();
        if (picture != null) {
            picture = ImageUtil.resizeImage(picture, jLabelPicture);
            jLabelPicture.setIcon(picture);
        }

        initManagement();
        loadPayments();
        setVisible(true);
    }

    private boolean validCredits() {
        return credits >= 4 && credits <= 16;
    }

    private void updateAttachment(ImageIcon img, boolean sent) {
        if (img == null) {
            choosenImage = null;
            jLabelDocument.setIcon(null);
            jButtonSend.setEnabled(false);
        } else {
            choosenImage = img;
            jLabelDocument.setIcon(ImageUtil.resizeImage(choosenImage, jLabelDocument));
            jButtonSend.setEnabled(!sent);
        }
    }

    private void updateAttachment(ImageIcon img) {
        updateAttachment(img, false);
    }

    private void loadLoginData() {
        user = userManager.getCurrentUser();
        if (user instanceof Applicant) {
            menu.disableAllExcept(3);
            menu.toggleMenuItem(3);
            appl = (Applicant) user;
            stud = null;
            jLabelName.setText(appl.fname + ' ' + appl.lname);

            if (appl.enabled) {
                jLabelId.setText(appl.getUsername());
            } else {
                jLabelId.setText("<pendiente>");
                if (appl.attachment != null)
                    updateAttachment(appl.attachment, true);
            }

            jPanelIdentity.setVisible(!appl.enabled);
            jPanelSubmit.setVisible(appl.enabled);
        } else {
            jPanelMenu.remove(jPanelVerifyOpt);
            menu.toggleMenuItem(0);
            stud = (Student) user;
            appl = null;
            jLabelName.setText(stud.getFname() + ' ' + stud.getLname());
            jLabelId.setText(stud.getUsername());
            loadStudentReportData();
        }
    }

    private void loadStudentReportData() {
        if (stud.getState() == AcademicState.SEEMS_THEY_WENT_THROUGH_ALL_THAT_STUFF_ALREADY)
            jPanelMenu.remove(jPanelManageOpt);

        academic = stud.getAcademicData();
        jLabelCredits.setText(String.valueOf(academic.getCredits()));
        DefaultTableModel model1 = (DefaultTableModel) jTableSubjects.getModel();
        DefaultTableModel model2 = (DefaultTableModel) jTableCancelledSubjects.getModel();
        model1.setRowCount(0);
        model2.setRowCount(0);

        for (Transaction tr : academic.transactions) {
            DefaultTableModel target = (tr.type == TransactionType.ENROLLMENT) ? model1 : model2;
            for (String sid : tr.subjects) {
                Subject sub = subjectManager.get(sid);
                Professor prof = professorManager.get(sub.getProfessorId());
                target.addRow(new Object[] { sub.getId(), sub, prof, tr.when });
            }
        }
    }

    private void initTableEvents() {
        MouseClickedListener listener = (MouseEvent me) -> {
            if (me.getButton() != MouseEvent.BUTTON1 || me.getClickCount() != 2)
                return;

            removeSelectedSubjects();
        };

        jTableEnrollSubjects.addMouseListener(listener);
        jTableCancelSubjects.addMouseListener(listener);
    }

    private void initManagement() {
        subjectIndices = new ArrayList<>();
        if (stud.getState() == AcademicState.BEFORE_ENROLL) {
            manageTable = jTableEnrollSubjects;
            manageTableModel = (DefaultTableModel) manageTable.getModel();
            loadEnrollData();
        } else if (stud.getState() == AcademicState.ENROLLED) {
            manageTable = jTableCancelSubjects;
            manageTableModel = (DefaultTableModel) manageTable.getModel();
            loadCancelData();
        }

        initTableEvents();
        updateManagePanel();
    }

    private void loadPayments() {
        if (stud == null)
            return;

        double total = Financial.ENROLLMENT_COST;
        jLabelEnrollmentCost.setText(String.valueOf(Financial.ENROLLMENT_COST));
        jLabelCreditCost.setText(String.valueOf(Financial.COST_PER_CREDIT));
        DefaultTableModel costsModel = (DefaultTableModel) jTableCosts.getModel();

        for (String sid : academic.subjects) {
            Subject s = subjectManager.get(sid);
            double cost = Financial.getSubjectCost(s);
            total += cost;
            costsModel.addRow(new Object[] { s.getName(), s.getCredits(), cost });
        }

        jLabelTotal.setText(String.valueOf(total));
    }

    private void loadEnrollData() {
        credits = 0;
        for (Subject s : subjectManager.list)
            jComboBoxSubjects.addItem(s.getName());
    }

    private void loadCancelData() {
        credits = academic.getCredits();
        subjectCount = academic.subjects.size();

        for (String sid : academic.subjects) {
            Subject sub = subjectManager.get(sid);
            jComboBoxSubjects.addItem(sub.getName());
        }
    }

    private void initMenu() {
        int count = 0;
        TogglePanel[] options = new TogglePanel[4];

        for (JPanel option : new JPanel[] { jPanelReportOpt, jPanelManageOpt, jPanelPaymentOpt, jPanelVerifyOpt })
            options[count++] = (TogglePanel) option;

        JPanel[] views = new JPanel[] { jPanelReport, jPanelManage, jPanelPayment, jPanelVerify };
        menu = new ToggleMenu(options, views);

        menu.addMenuListener((TogglePanel tp, int index) -> {
            if (index != 1)
                return;

            AcademicState state = stud.getState();
            if (state == AcademicState.BEFORE_ENROLL) {
                jPanelEnroll.setVisible(true);
                jPanelCancel.setVisible(false);
            } else if (state == AcademicState.ENROLLED) {
                jPanelEnroll.setVisible(false);
                jPanelCancel.setVisible(true);
            }

            if (subjectIndices == null)
                initManagement();
        });
    }

    private void initValidationData() {
        jTextFieldPhone.putClientProperty("validator", RegexValidator.PHONE_VALIDATOR);
        jTextAddress.putClientProperty("required", true);

        submitFields = new JTextComponent[] {
            jTextFieldPhone,
            jTextAddress
        };

        submitValidator = new FormValidator(submitFields);
    }

    public void updateEnrollPanel() {
        int count = subjectIndices.size();
        jLabelEnrollCredits.setText(String.valueOf(credits));
        jButtonAddSubj.setEnabled(credits < 16);
        jButtonRemoveSubj.setEnabled(count > 0);
        jButtonEnroll.setEnabled(validCredits());
    }

    public void updateCancelPanel() {
        int count = subjectIndices.size();
        jLabelCancelCredits.setText(String.valueOf(credits));
        jButtonAddSubj.setEnabled(credits >= 4);
        jButtonRemoveSubj.setEnabled(count > 0);
        jButtonCancel.setEnabled(validCredits());
    }

    public void updateManagePanel() {
        if (stud.getState() == AcademicState.BEFORE_ENROLL)
            updateEnrollPanel();
        else if (stud.getState() == AcademicState.ENROLLED)
            updateCancelPanel();

        jLabelCreditsInfo.setVisible(credits < 4 || credits >= 16 && stud.getState() == AcademicState.BEFORE_ENROLL);
        if (credits == 0)
            jLabelCreditsInfo.setText("Debe matricularse como mínimo 4 créditos");
        else if (credits >= 16 && stud.getState() == AcademicState.BEFORE_ENROLL)
            jLabelCreditsInfo.setText("Debe matricularse como máximo 16 créditos");
    }

    public void clearManagement() {
        manageTable = null;
        manageTableModel = null;
        subjectIndices = null;
        loadStudentReportData();
        userManager.update();
        menu.toggleMenuItem(0);
        jComboBoxSubjects.removeAllItems();
    }

    private Subject getSubject(int index) {
        if (stud.getState() == AcademicState.BEFORE_ENROLL)
            return subjectManager.list.get(index);

        String sid = academic.subjects.get(index);
        return subjectManager.get(sid);
    }

    private void addSelectedSubject() {
        int itemIndex = jComboBoxSubjects.getSelectedIndex(), subjIndex = itemIndex;
        if (itemIndex == -1)
            return;

        for (Integer j : subjectIndices) {
            if (j <= subjIndex)
                subjIndex += 1;
        }

        Subject s = getSubject(subjIndex);
        Professor prof = professorManager.get(s.getProfessorId());
        manageTableModel.addRow(new Object[] { s.getId(), s, prof, s.getCredits() });
        
        java.awt.EventQueue.invokeLater(() -> {
            jComboBoxSubjects.removeItemAt(itemIndex);
        });
        
        subjectIndices.add(subjIndex);
        int subjCredits = s.getCredits();
        if (stud.getState() == AcademicState.BEFORE_ENROLL)
            credits += subjCredits;
        else if (stud.getState() == AcademicState.ENROLLED)
            credits -= subjCredits;

        updateManagePanel();
    }

    private void removeSelectedSubjects() {
        while (manageTable.getSelectedRows().length > 0) {
            int i = manageTable.getSelectedRows()[0];
            Integer si = subjectIndices.remove(i);

            if (si == null)
                continue;

            Subject s = getSubject(si);
            int subjCredits = s.getCredits();

            if (stud.getState() == AcademicState.BEFORE_ENROLL)
                credits -= subjCredits;
            else if (stud.getState() == AcademicState.ENROLLED)
                credits += subjCredits;

            manageTableModel.removeRow(i);
            java.awt.EventQueue.invokeLater(() -> {
                jComboBoxSubjects.insertItemAt(s.getName(), min(si, jComboBoxSubjects.getItemCount()));
            });
        }

        updateManagePanel();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelMain = new javax.swing.JPanel();
        jPanelLeft = new javax.swing.JPanel();
        jLabelTrademark = new javax.swing.JLabel();
        jLabelPicture = new javax.swing.JLabel();
        jPanelMenu = new javax.swing.JPanel();
        jPanelReportOpt = new TogglePanel(new Color(44, 93, 59));
        jLabelOpt1 = new javax.swing.JLabel();
        jPanelManageOpt = new TogglePanel(new Color(44, 93, 59));
        jLabelOpt2 = new javax.swing.JLabel();
        jPanelPaymentOpt = new TogglePanel(new Color(44, 93, 59));
        jLabelOpt3 = new javax.swing.JLabel();
        jPanelVerifyOpt = new TogglePanel(new Color(44, 93, 59));
        jLabelOpt4 = new javax.swing.JLabel();
        jPanelLogoutOpt1 = new TogglePanel(new Color(44, 93, 59));
        jLabelOpt5 = new javax.swing.JLabel();
        jPanelUserInfo = new javax.swing.JPanel();
        jLabelNameStatic = new javax.swing.JLabel();
        jLabelName = new javax.swing.JLabel();
        jLabelIdStatic = new javax.swing.JLabel();
        jLabelId = new javax.swing.JLabel();
        jPanelReport = new javax.swing.JPanel();
        jLabelCreditsStatic = new javax.swing.JLabel();
        jLabelCredits = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableSubjects = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTableCancelledSubjects = new javax.swing.JTable();
        jPanelManage = new javax.swing.JPanel();
        jButtonAddSubj = new javax.swing.JButton();
        jButtonRemoveSubj = new javax.swing.JButton();
        jLabelCreditsInfo = new javax.swing.JLabel();
        jComboBoxSubjects = new javax.swing.JComboBox<>();
        jPanelEnroll = new javax.swing.JPanel();
        jLabelEnrollCreditsStatic = new javax.swing.JLabel();
        jLabelEnrollCredits = new javax.swing.JLabel();
        jLabelSelect = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableEnrollSubjects = new javax.swing.JTable();
        jButtonEnroll = new javax.swing.JButton();
        jPanelCancel = new javax.swing.JPanel();
        jLabelCancelCreditsStatic = new javax.swing.JLabel();
        jLabelCancelCredits = new javax.swing.JLabel();
        jLabelSelect1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTableCancelSubjects = new javax.swing.JTable();
        jButtonCancel = new javax.swing.JButton();
        jPanelPayment = new javax.swing.JPanel();
        jPanelPricing = new javax.swing.JPanel();
        jPanelEnrollmentCost = new javax.swing.JPanel();
        jLabelEnrollmentCostStatic = new javax.swing.JLabel();
        jLabelEnrollmentCost = new javax.swing.JLabel();
        jPanelCreditCost = new javax.swing.JPanel();
        jLabelCreditCostStatic = new javax.swing.JLabel();
        jLabelCreditCost = new javax.swing.JLabel();
        jPanelCostToPay = new javax.swing.JPanel();
        jLabelTotalStatic = new javax.swing.JLabel();
        jLabelTotal = new javax.swing.JLabel();
        jScrollPaneCosts = new javax.swing.JScrollPane();
        jTableCosts = new javax.swing.JTable();
        jPanelVerify = new javax.swing.JPanel();
        jPanelIdentity = new javax.swing.JPanel();
        jLabelVerifyInfo = new javax.swing.JLabel();
        jButtonBrowse = new javax.swing.JButton();
        jButtonSend = new javax.swing.JButton();
        jLabelDocument = new javax.swing.JLabel();
        jPanelSubmit = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabelRequired = new javax.swing.JLabel();
        jLabelPhone = new javax.swing.JLabel();
        jTextFieldPhone = new javax.swing.JTextField();
        jLabelAddress = new javax.swing.JLabel();
        jScrollPaneAddress = new javax.swing.JScrollPane();
        jTextAddress = new javax.swing.JTextArea();
        jLabePicture = new javax.swing.JLabel();
        jLabelProfile = new javax.swing.JLabel();
        jButtonBrowseProfile = new javax.swing.JButton();
        jButtonRegister = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanelMain.setBackground(new java.awt.Color(34, 63, 49));
        jPanelMain.setPreferredSize(new java.awt.Dimension(900, 600));
        jPanelMain.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanelLeft.setBackground(new java.awt.Color(34, 63, 49));
        jPanelLeft.setPreferredSize(new java.awt.Dimension(230, 600));
        jPanelLeft.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelTrademark.setBackground(new java.awt.Color(34, 63, 49));
        jLabelTrademark.setFont(new java.awt.Font("Trebuchet MS", 1, 18)); // NOI18N
        jLabelTrademark.setForeground(new java.awt.Color(44, 201, 130));
        jLabelTrademark.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTrademark.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/ecosystem-small.png"))); // NOI18N
        jLabelTrademark.setText("EcoSystem");
        jLabelTrademark.setIconTextGap(8);
        jPanelLeft.add(jLabelTrademark, new org.netbeans.lib.awtextra.AbsoluteConstraints(52, 25, -1, -1));

        jLabelPicture.setForeground(new java.awt.Color(240, 240, 240));
        jLabelPicture.setPreferredSize(new java.awt.Dimension(230, 230));
        jPanelLeft.add(jLabelPicture, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 330, -1, -1));

        jPanelMenu.setBackground(new java.awt.Color(34, 63, 49));
        jPanelMenu.setPreferredSize(new java.awt.Dimension(230, 250));
        jPanelMenu.setLayout(new java.awt.GridLayout(5, 0));

        jPanelReportOpt.setBackground(new java.awt.Color(44, 73, 59));
        jPanelReportOpt.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        jPanelReportOpt.setLayout(new java.awt.GridLayout(1, 0));

        jLabelOpt1.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelOpt1.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOpt1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelOpt1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/report-card.png"))); // NOI18N
        jLabelOpt1.setText("Reporte de matrícula");
        jLabelOpt1.setRequestFocusEnabled(false);
        jPanelReportOpt.add(jLabelOpt1);
        jLabelOpt1.getAccessibleContext().setAccessibleName("");

        jPanelMenu.add(jPanelReportOpt);

        jPanelManageOpt.setBackground(new java.awt.Color(44, 73, 59));
        jPanelManageOpt.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        jPanelManageOpt.setLayout(new java.awt.GridLayout(1, 0));

        jLabelOpt2.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelOpt2.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOpt2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelOpt2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/manage.png"))); // NOI18N
        jLabelOpt2.setText("Gestión de matrícula");
        jLabelOpt2.setRequestFocusEnabled(false);
        jPanelManageOpt.add(jLabelOpt2);
        jLabelOpt2.getAccessibleContext().setAccessibleName("");

        jPanelMenu.add(jPanelManageOpt);

        jPanelPaymentOpt.setBackground(new java.awt.Color(44, 73, 59));
        jPanelPaymentOpt.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        jPanelPaymentOpt.setLayout(new java.awt.GridLayout(1, 0));

        jLabelOpt3.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelOpt3.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOpt3.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelOpt3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/payments.png"))); // NOI18N
        jLabelOpt3.setText("Reporte de pago");
        jLabelOpt3.setRequestFocusEnabled(false);
        jPanelPaymentOpt.add(jLabelOpt3);
        jLabelOpt3.getAccessibleContext().setAccessibleName("");

        jPanelMenu.add(jPanelPaymentOpt);

        jPanelVerifyOpt.setBackground(new java.awt.Color(44, 73, 59));
        jPanelVerifyOpt.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        jPanelVerifyOpt.setLayout(new java.awt.GridLayout(1, 0));

        jLabelOpt4.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelOpt4.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOpt4.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelOpt4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/user-icon.png"))); // NOI18N
        jLabelOpt4.setText("Verificación");
        jLabelOpt4.setRequestFocusEnabled(false);
        jPanelVerifyOpt.add(jLabelOpt4);
        jLabelOpt4.getAccessibleContext().setAccessibleName("");

        jPanelMenu.add(jPanelVerifyOpt);

        jPanelLogoutOpt1.setBackground(new java.awt.Color(44, 73, 59));
        jPanelLogoutOpt1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
        jPanelLogoutOpt1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanelLogoutOpt1MouseClicked(evt);
            }
        });
        jPanelLogoutOpt1.setLayout(new java.awt.GridLayout(1, 0));

        jLabelOpt5.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelOpt5.setForeground(new java.awt.Color(255, 255, 255));
        jLabelOpt5.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabelOpt5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/back-white.png"))); // NOI18N
        jLabelOpt5.setText("Cerrar sesión");
        jLabelOpt5.setRequestFocusEnabled(false);
        jPanelLogoutOpt1.add(jLabelOpt5);

        jPanelMenu.add(jPanelLogoutOpt1);

        jPanelLeft.add(jPanelMenu, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 80, -1, -1));

        jPanelMain.add(jPanelLeft, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jPanelUserInfo.setBackground(new java.awt.Color(34, 63, 49));
        jPanelUserInfo.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelNameStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelNameStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelNameStatic.setText("Estudiante:");
        jPanelUserInfo.add(jLabelNameStatic, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 18, -1, -1));

        jLabelName.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelName.setForeground(new java.awt.Color(255, 255, 255));
        jPanelUserInfo.add(jLabelName, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 18, -1, -1));

        jLabelIdStatic.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelIdStatic.setForeground(new java.awt.Color(255, 255, 255));
        jLabelIdStatic.setText("Identificación:");
        jPanelUserInfo.add(jLabelIdStatic, new org.netbeans.lib.awtextra.AbsoluteConstraints(25, 43, -1, -1));

        jLabelId.setFont(new java.awt.Font("Trebuchet MS", 1, 14)); // NOI18N
        jLabelId.setForeground(new java.awt.Color(255, 255, 255));
        jPanelUserInfo.add(jLabelId, new org.netbeans.lib.awtextra.AbsoluteConstraints(126, 43, -1, -1));

        jPanelMain.add(jPanelUserInfo, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 0, 670, 80));

        jPanelReport.setBackground(new java.awt.Color(58, 155, 83));
        jPanelReport.setForeground(new java.awt.Color(240, 240, 240));
        jPanelReport.setPreferredSize(new java.awt.Dimension(670, 520));
        jPanelReport.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelCreditsStatic.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelCreditsStatic.setForeground(new java.awt.Color(240, 240, 240));
        jLabelCreditsStatic.setText("Total de créditos matriculados:");
        jPanelReport.add(jLabelCreditsStatic, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, -1, -1));

        jLabelCredits.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelCredits.setForeground(new java.awt.Color(240, 240, 240));
        jPanelReport.add(jLabelCredits, new org.netbeans.lib.awtextra.AbsoluteConstraints(261, 30, -1, -1));

        jScrollPane1.setBackground(new java.awt.Color(58, 155, 83));
        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(240, 240, 240), 2, true), "Lista de materias matriculadas", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Trebuchet MS", 1, 12), new java.awt.Color(240, 240, 240))); // NOI18N
        jScrollPane1.setPreferredSize(new java.awt.Dimension(670, 222));

        jTableSubjects.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Nombre", "Docente", "Fecha"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTableSubjects);

        jPanelReport.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 75, -1, -1));

        jScrollPane4.setBackground(new java.awt.Color(58, 155, 83));
        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(240, 240, 240), 2, true), "Lista de materias canceladas", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Trebuchet MS", 1, 12), new java.awt.Color(240, 240, 240))); // NOI18N
        jScrollPane4.setPreferredSize(new java.awt.Dimension(670, 222));

        jTableCancelledSubjects.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Código", "Nombre", "Docente", "Fecha"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(jTableCancelledSubjects);

        jPanelReport.add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 298, -1, -1));

        jPanelMain.add(jPanelReport, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 80, -1, -1));

        jPanelManage.setPreferredSize(new java.awt.Dimension(670, 520));
        jPanelManage.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButtonAddSubj.setBackground(new java.awt.Color(231, 231, 231));
        jButtonAddSubj.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jButtonAddSubj.setForeground(new java.awt.Color(21, 25, 28));
        jButtonAddSubj.setText("Añadir");
        jButtonAddSubj.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonAddSubj.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonAddSubj.setFocusPainted(false);
        jButtonAddSubj.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddSubjActionPerformed(evt);
            }
        });
        jPanelManage.add(jButtonAddSubj, new org.netbeans.lib.awtextra.AbsoluteConstraints(215, 350, 100, 32));

        jButtonRemoveSubj.setBackground(new java.awt.Color(231, 231, 231));
        jButtonRemoveSubj.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jButtonRemoveSubj.setForeground(new java.awt.Color(21, 25, 28));
        jButtonRemoveSubj.setText("Eliminar");
        jButtonRemoveSubj.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonRemoveSubj.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonRemoveSubj.setEnabled(false);
        jButtonRemoveSubj.setFocusPainted(false);
        jButtonRemoveSubj.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveSubjActionPerformed(evt);
            }
        });
        jPanelManage.add(jButtonRemoveSubj, new org.netbeans.lib.awtextra.AbsoluteConstraints(355, 350, 100, 32));

        jLabelCreditsInfo.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jLabelCreditsInfo.setForeground(new java.awt.Color(240, 240, 240));
        jLabelCreditsInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 240, 240)), javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5)), "Información", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Trebuchet MS", 0, 11), new java.awt.Color(240, 240, 240))); // NOI18N
        jPanelManage.add(jLabelCreditsInfo, new org.netbeans.lib.awtextra.AbsoluteConstraints(365, 20, -1, -1));

        jComboBoxSubjects.setPreferredSize(new java.awt.Dimension(300, 20));
        jPanelManage.add(jComboBoxSubjects, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 80, 420, -1));

        jPanelEnroll.setBackground(new java.awt.Color(58, 155, 83));
        jPanelEnroll.setPreferredSize(new java.awt.Dimension(670, 520));
        jPanelEnroll.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelEnrollCreditsStatic.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelEnrollCreditsStatic.setForeground(new java.awt.Color(240, 240, 240));
        jLabelEnrollCreditsStatic.setText("Total de créditos a matricular:");
        jPanelEnroll.add(jLabelEnrollCreditsStatic, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, -1, -1));

        jLabelEnrollCredits.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelEnrollCredits.setForeground(new java.awt.Color(240, 240, 240));
        jPanelEnroll.add(jLabelEnrollCredits, new org.netbeans.lib.awtextra.AbsoluteConstraints(256, 30, -1, -1));

        jLabelSelect.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelSelect.setForeground(new java.awt.Color(240, 240, 240));
        jLabelSelect.setText("Seleccione una materia");
        jPanelEnroll.add(jLabelSelect, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, -1, -1));

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Materias a matricular", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Trebuchet MS", 1, 12))); // NOI18N
        jScrollPane2.setPreferredSize(new java.awt.Dimension(670, 200));

        jTableEnrollSubjects.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jTableEnrollSubjects);

        jPanelEnroll.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 120, -1, -1));

        jButtonEnroll.setBackground(new java.awt.Color(231, 231, 231));
        jButtonEnroll.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jButtonEnroll.setForeground(new java.awt.Color(21, 25, 28));
        jButtonEnroll.setText("Matricular");
        jButtonEnroll.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonEnroll.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonEnroll.setEnabled(false);
        jButtonEnroll.setFocusPainted(false);
        jButtonEnroll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEnrollActionPerformed(evt);
            }
        });
        jPanelEnroll.add(jButtonEnroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(285, 400, 100, 32));

        jPanelManage.add(jPanelEnroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jPanelCancel.setBackground(new java.awt.Color(58, 155, 83));
        jPanelCancel.setPreferredSize(new java.awt.Dimension(670, 520));
        jPanelCancel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelCancelCreditsStatic.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelCancelCreditsStatic.setForeground(new java.awt.Color(240, 240, 240));
        jLabelCancelCreditsStatic.setText("Total de créditos restantes:");
        jPanelCancel.add(jLabelCancelCreditsStatic, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, -1, -1));

        jLabelCancelCredits.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelCancelCredits.setForeground(new java.awt.Color(240, 240, 240));
        jPanelCancel.add(jLabelCancelCredits, new org.netbeans.lib.awtextra.AbsoluteConstraints(235, 30, -1, -1));

        jLabelSelect1.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelSelect1.setForeground(new java.awt.Color(240, 240, 240));
        jLabelSelect1.setText("Seleccione una materia");
        jPanelCancel.add(jLabelSelect1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 80, -1, -1));

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Materias a cancelar", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Trebuchet MS", 1, 12))); // NOI18N
        jScrollPane3.setPreferredSize(new java.awt.Dimension(670, 200));

        jTableCancelSubjects.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(jTableCancelSubjects);

        jPanelCancel.add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 120, -1, -1));

        jButtonCancel.setBackground(new java.awt.Color(231, 231, 231));
        jButtonCancel.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jButtonCancel.setForeground(new java.awt.Color(21, 25, 28));
        jButtonCancel.setText("Cancelación");
        jButtonCancel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jButtonCancel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonCancel.setEnabled(false);
        jButtonCancel.setFocusPainted(false);
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanelCancel.add(jButtonCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(285, 400, 100, 32));

        jPanelManage.add(jPanelCancel, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jPanelMain.add(jPanelManage, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 80, -1, -1));

        jPanelPayment.setPreferredSize(new java.awt.Dimension(670, 520));
        jPanelPayment.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanelPricing.setPreferredSize(new java.awt.Dimension(610, 100));
        jPanelPricing.setLayout(new java.awt.GridLayout(3, 0, 0, 10));

        jPanelEnrollmentCost.setLayout(new javax.swing.BoxLayout(jPanelEnrollmentCost, javax.swing.BoxLayout.LINE_AXIS));

        jLabelEnrollmentCostStatic.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        jLabelEnrollmentCostStatic.setText("Costo de matrícula por ingreso: $");
        jPanelEnrollmentCost.add(jLabelEnrollmentCostStatic);

        jLabelEnrollmentCost.setFont(new java.awt.Font("Consolas", 1, 18)); // NOI18N
        jPanelEnrollmentCost.add(jLabelEnrollmentCost);

        jPanelPricing.add(jPanelEnrollmentCost);

        jPanelCreditCost.setLayout(new javax.swing.BoxLayout(jPanelCreditCost, javax.swing.BoxLayout.LINE_AXIS));

        jLabelCreditCostStatic.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        jLabelCreditCostStatic.setText("Costo de matrícula por crédito: $");
        jPanelCreditCost.add(jLabelCreditCostStatic);
        jLabelCreditCostStatic.getAccessibleContext().setAccessibleName("");

        jLabelCreditCost.setFont(new java.awt.Font("Consolas", 1, 18)); // NOI18N
        jPanelCreditCost.add(jLabelCreditCost);

        jPanelPricing.add(jPanelCreditCost);

        jPanelCostToPay.setLayout(new javax.swing.BoxLayout(jPanelCostToPay, javax.swing.BoxLayout.LINE_AXIS));

        jLabelTotalStatic.setFont(new java.awt.Font("Consolas", 0, 18)); // NOI18N
        jLabelTotalStatic.setText("Costo total a pagar: $");
        jPanelCostToPay.add(jLabelTotalStatic);

        jLabelTotal.setFont(new java.awt.Font("Consolas", 1, 18)); // NOI18N
        jPanelCostToPay.add(jLabelTotal);

        jPanelPricing.add(jPanelCostToPay);

        jPanelPayment.add(jPanelPricing, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, -1, -1));

        jScrollPaneCosts.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(200, 200, 200), 2, true), null), "Valor a pagar por materias", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Trebuchet MS", 0, 11))); // NOI18N
        jScrollPaneCosts.setPreferredSize(new java.awt.Dimension(610, 320));

        jTableCosts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Materia", "Créditos", "Costo"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPaneCosts.setViewportView(jTableCosts);

        jPanelPayment.add(jScrollPaneCosts, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, -1, -1));

        jPanelMain.add(jPanelPayment, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 80, -1, -1));

        jPanelVerify.setPreferredSize(new java.awt.Dimension(670, 520));
        jPanelVerify.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanelIdentity.setBackground(new java.awt.Color(58, 155, 83));
        jPanelIdentity.setPreferredSize(new java.awt.Dimension(670, 520));
        jPanelIdentity.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelVerifyInfo.setFont(new java.awt.Font("Trebuchet MS", 1, 15)); // NOI18N
        jLabelVerifyInfo.setForeground(new java.awt.Color(240, 240, 240));
        jLabelVerifyInfo.setText("Adjunte su documento de identificación");
        jPanelIdentity.add(jLabelVerifyInfo, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 50, -1, -1));

        jButtonBrowse.setBackground(new java.awt.Color(231, 231, 231));
        jButtonBrowse.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jButtonBrowse.setForeground(new java.awt.Color(21, 25, 28));
        jButtonBrowse.setText("Seleccionar");
        jButtonBrowse.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jButtonBrowse.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonBrowse.setFocusPainted(false);
        jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseActionPerformed(evt);
            }
        });
        jPanelIdentity.add(jButtonBrowse, new org.netbeans.lib.awtextra.AbsoluteConstraints(357, 42, 100, 32));

        jButtonSend.setBackground(new java.awt.Color(231, 231, 231));
        jButtonSend.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jButtonSend.setForeground(new java.awt.Color(21, 25, 28));
        jButtonSend.setText("Enviar");
        jButtonSend.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jButtonSend.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonSend.setEnabled(false);
        jButtonSend.setFocusPainted(false);
        jButtonSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSendActionPerformed(evt);
            }
        });
        jPanelIdentity.add(jButtonSend, new org.netbeans.lib.awtextra.AbsoluteConstraints(477, 42, 100, 32));

        jLabelDocument.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(34, 49, 63)));
        jLabelDocument.setOpaque(true);
        jPanelIdentity.add(jLabelDocument, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 110, 290, 360));

        jPanelVerify.add(jPanelIdentity, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jPanelSubmit.setPreferredSize(new java.awt.Dimension(670, 520));
        jPanelSubmit.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Trebuchet MS", 1, 16)); // NOI18N
        jLabel1.setText("Su registro fue aceptado, complete los siguientes campos para terminar.");
        jPanelSubmit.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 30, -1, -1));

        jLabelRequired.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelRequired.setText("*: Requerido");
        jPanelSubmit.add(jLabelRequired, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 80, -1, -1));

        jLabelPhone.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelPhone.setText("Teléfono");
        jPanelSubmit.add(jLabelPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 130, -1, -1));

        jTextFieldPhone.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jTextFieldPhone.setForeground(new java.awt.Color(34, 49, 63));
        jTextFieldPhone.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanelSubmit.add(jTextFieldPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(132, 125, 160, -1));

        jLabelAddress.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabelAddress.setText("Dirección*");
        jPanelSubmit.add(jLabelAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 190, -1, -1));

        jScrollPaneAddress.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPaneAddress.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTextAddress.setColumns(20);
        jTextAddress.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jTextAddress.setRows(1);
        jTextAddress.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jScrollPaneAddress.setViewportView(jTextAddress);

        jPanelSubmit.add(jScrollPaneAddress, new org.netbeans.lib.awtextra.AbsoluteConstraints(132, 180, 240, 53));

        jLabePicture.setFont(new java.awt.Font("Trebuchet MS", 0, 16)); // NOI18N
        jLabePicture.setText("Foto de perfil");
        jPanelSubmit.add(jLabePicture, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 310, -1, -1));

        jLabelProfile.setBackground(new java.awt.Color(231, 231, 231));
        jLabelProfile.setForeground(new java.awt.Color(240, 240, 240));
        jLabelProfile.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(34, 49, 63)));
        jLabelProfile.setOpaque(true);
        jPanelSubmit.add(jLabelProfile, new org.netbeans.lib.awtextra.AbsoluteConstraints(165, 260, 120, 150));

        jButtonBrowseProfile.setBackground(new java.awt.Color(200, 200, 200));
        jButtonBrowseProfile.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jButtonBrowseProfile.setForeground(new java.awt.Color(21, 25, 28));
        jButtonBrowseProfile.setText("Seleccionar");
        jButtonBrowseProfile.setBorder(null);
        jButtonBrowseProfile.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonBrowseProfile.setFocusPainted(false);
        jButtonBrowseProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowseProfileActionPerformed(evt);
            }
        });
        jPanelSubmit.add(jButtonBrowseProfile, new org.netbeans.lib.awtextra.AbsoluteConstraints(320, 305, 100, 32));

        jButtonRegister.setBackground(new java.awt.Color(200, 200, 200));
        jButtonRegister.setFont(new java.awt.Font("Trebuchet MS", 0, 14)); // NOI18N
        jButtonRegister.setForeground(new java.awt.Color(21, 25, 28));
        jButtonRegister.setText("Terminar");
        jButtonRegister.setBorder(null);
        jButtonRegister.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButtonRegister.setFocusPainted(false);
        jButtonRegister.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRegisterActionPerformed(evt);
            }
        });
        jPanelSubmit.add(jButtonRegister, new org.netbeans.lib.awtextra.AbsoluteConstraints(285, 450, 100, 32));

        jPanelVerify.add(jPanelSubmit, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        jPanelMain.add(jPanelVerify, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 80, -1, -1));

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


    private void jPanelLogoutOpt1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanelLogoutOpt1MouseClicked
        userManager.logout();
        new SigninFrame();
        this.dispose();
    }//GEN-LAST:event_jPanelLogoutOpt1MouseClicked

    private void jButtonSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSendActionPerformed
        appl.attachment = choosenImage;
        userManager.update();
        JOptionPane.showMessageDialog(null, "Su documento fue enviado, en cuanto su solicitud sea respondida será notificado por correo electrónico", "Ecosystem", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButtonSendActionPerformed

    private void jButtonBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseActionPerformed
        File file = imageChooser.chooseFile();
        if (file == null)
            return;

        updateAttachment(new ImageIcon(file.getAbsolutePath()));
    }//GEN-LAST:event_jButtonBrowseActionPerformed

    private void jButtonBrowseProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowseProfileActionPerformed
        File file = imageChooser.chooseFile();
        if (file == null)
            return;

        choosenImage = ImageUtil.resizeImage(file.getAbsolutePath(), jLabelProfile);
        jLabelProfile.setIcon(choosenImage);
    }//GEN-LAST:event_jButtonBrowseProfileActionPerformed

    private void jButtonRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRegisterActionPerformed
        if (!submitValidator.validateForm())
            return;

        Student newUser = new Student(appl.getUsername(), appl.getPasswordHash(), true, appl.fname, appl.lname,
                                      jTextFieldPhone.getText(), appl.email, jTextAddress.getText(), choosenImage);
        userManager.delete(appl);

        if (!userManager.add(newUser))
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error. Póngase en contacto con el administrador", "Error", JOptionPane.ERROR_MESSAGE);
        else
            JOptionPane.showMessageDialog(null, "Registro finalizado satisfactoriamente");

        userManager.logout();
        new SigninFrame();
        this.dispose();
    }//GEN-LAST:event_jButtonRegisterActionPerformed

    private void jButtonAddSubjActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddSubjActionPerformed
        addSelectedSubject();
    }//GEN-LAST:event_jButtonAddSubjActionPerformed

    private void jButtonRemoveSubjActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveSubjActionPerformed
        removeSelectedSubjects();
    }//GEN-LAST:event_jButtonRemoveSubjActionPerformed

    private void jButtonEnrollActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEnrollActionPerformed
        if (JOptionPane.showConfirmDialog(null, "¿Desea continuar? No será posible volver a realizar el proceso de matrícula una vez completado",
                                          "Ecosystem", JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION)
            return;

        ArrayList<String> added = new ArrayList<>();
        for (Integer i : subjectIndices)
            added.add(subjectManager.list.get(i).getId());

        academic.execTransaction(new Transaction(stud.getUsername(), added, TransactionType.ENROLLMENT));
        stud.setState(AcademicState.ENROLLED);
        clearManagement();
    }//GEN-LAST:event_jButtonEnrollActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        if (JOptionPane.showConfirmDialog(null, "¿Desea continuar? El proceso de cancelación puede realizarse solamente una vez",
                                          "Ecosystem", JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION)
            return;

        ArrayList<String> cancelled = new ArrayList<>();
        for (Integer i : subjectIndices)
            cancelled.add(academic.subjects.get(i));

        academic.execTransaction(new Transaction(stud.getUsername(), cancelled, TransactionType.CANCELLATION));
        stud.setState(AcademicState.SEEMS_THEY_WENT_THROUGH_ALL_THAT_STUFF_ALREADY);
        clearManagement();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new StudentFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddSubj;
    private javax.swing.JButton jButtonBrowse;
    private javax.swing.JButton jButtonBrowseProfile;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonEnroll;
    private javax.swing.JButton jButtonRegister;
    private javax.swing.JButton jButtonRemoveSubj;
    private javax.swing.JButton jButtonSend;
    private javax.swing.JComboBox<String> jComboBoxSubjects;
    private javax.swing.JLabel jLabePicture;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelAddress;
    private javax.swing.JLabel jLabelCancelCredits;
    private javax.swing.JLabel jLabelCancelCreditsStatic;
    private javax.swing.JLabel jLabelCreditCost;
    private javax.swing.JLabel jLabelCreditCostStatic;
    private javax.swing.JLabel jLabelCredits;
    private javax.swing.JLabel jLabelCreditsInfo;
    private javax.swing.JLabel jLabelCreditsStatic;
    private javax.swing.JLabel jLabelDocument;
    private javax.swing.JLabel jLabelEnrollCredits;
    private javax.swing.JLabel jLabelEnrollCreditsStatic;
    private javax.swing.JLabel jLabelEnrollmentCost;
    private javax.swing.JLabel jLabelEnrollmentCostStatic;
    private javax.swing.JLabel jLabelId;
    private javax.swing.JLabel jLabelIdStatic;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelNameStatic;
    private javax.swing.JLabel jLabelOpt1;
    private javax.swing.JLabel jLabelOpt2;
    private javax.swing.JLabel jLabelOpt3;
    private javax.swing.JLabel jLabelOpt4;
    private javax.swing.JLabel jLabelOpt5;
    private javax.swing.JLabel jLabelPhone;
    private javax.swing.JLabel jLabelPicture;
    private javax.swing.JLabel jLabelProfile;
    private javax.swing.JLabel jLabelRequired;
    private javax.swing.JLabel jLabelSelect;
    private javax.swing.JLabel jLabelSelect1;
    private javax.swing.JLabel jLabelTotal;
    private javax.swing.JLabel jLabelTotalStatic;
    private javax.swing.JLabel jLabelTrademark;
    private javax.swing.JLabel jLabelVerifyInfo;
    private javax.swing.JPanel jPanelCancel;
    private javax.swing.JPanel jPanelCostToPay;
    private javax.swing.JPanel jPanelCreditCost;
    private javax.swing.JPanel jPanelEnroll;
    private javax.swing.JPanel jPanelEnrollmentCost;
    private javax.swing.JPanel jPanelIdentity;
    private javax.swing.JPanel jPanelLeft;
    private javax.swing.JPanel jPanelLogoutOpt1;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelManage;
    private javax.swing.JPanel jPanelManageOpt;
    private javax.swing.JPanel jPanelMenu;
    private javax.swing.JPanel jPanelPayment;
    private javax.swing.JPanel jPanelPaymentOpt;
    private javax.swing.JPanel jPanelPricing;
    private javax.swing.JPanel jPanelReport;
    private javax.swing.JPanel jPanelReportOpt;
    private javax.swing.JPanel jPanelSubmit;
    private javax.swing.JPanel jPanelUserInfo;
    private javax.swing.JPanel jPanelVerify;
    private javax.swing.JPanel jPanelVerifyOpt;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPaneAddress;
    private javax.swing.JScrollPane jScrollPaneCosts;
    private javax.swing.JTable jTableCancelSubjects;
    private javax.swing.JTable jTableCancelledSubjects;
    private javax.swing.JTable jTableCosts;
    private javax.swing.JTable jTableEnrollSubjects;
    private javax.swing.JTable jTableSubjects;
    private javax.swing.JTextArea jTextAddress;
    private javax.swing.JTextField jTextFieldPhone;
    // End of variables declaration//GEN-END:variables
}
