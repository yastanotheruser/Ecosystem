package ecosystem.mailing;

import ecosystem.event.MouseClickedListener;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class SimpleMailApp extends JFrame {
    public static EmailManager emailManager = new EmailManager("mailing");
    private static class EmailContentDialog extends JDialog {
        public EmailContentDialog(JFrame parent, Email email) {
            super(parent, true);
            setBounds(100, 100, 300, 200);
            Container container = getContentPane();
            container.setLayout(new GridLayout(1, 1));

            JTextArea jTextAreaEmail = new JTextArea(email.getBody());
            jTextAreaEmail.setEditable(false);
            jTextAreaEmail.setLineWrap(true);
            container.add(new JScrollPane(jTextAreaEmail));
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setVisible(true);
        }
    }

    private DefaultTableModel model;
    private JTable jTableEmailList;
    private final Container container;

    public SimpleMailApp() {
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/ecosystem.png")));
        setBounds(100, 100, 500, 400);
        container = this.getContentPane();
        initComponents();
        initEvents();
        loadData();
        setVisible(true);
    }

    private void initComponents() {
        model = new DefaultTableModel(new Object[][] {}, new Object[] { "Fuente", "Destinatario", "Contenido" }) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };

        jTableEmailList = new JTable(model);
        jTableEmailList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        container.setLayout(new GridLayout(1, 1));
        container.add(new JScrollPane(jTableEmailList));
    }

    private void initEvents() {
        emailManager.addEmailListener((Email email) -> {
            model.insertRow(0, new Object[] { email.getFrom(), email.getTo(), email.getBody() });
        });

        JFrame self = this;
        jTableEmailList.addMouseListener((MouseClickedListener) (MouseEvent me) -> {
            if (me.getButton() != MouseEvent.BUTTON1 || me.getClickCount() != 2)
                return;

            int index = jTableEmailList.getSelectedRow();
            if (index == -1)
                return;

            new EmailContentDialog(this, emailManager.list.get(emailManager.list.size() - index - 1));
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void loadData() {
        for (Email email : emailManager.list)
            model.insertRow(0, new Object[] { email.getFrom(), email.getTo(), email.getBody() });
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            emailManager.getFile().delete();
            emailManager.send("noreply@sm.ecosystem.co", "a@b.cd", "appapapa");

            byte[] bytes = new byte[1000];
            (new Random()).nextBytes(bytes);
            emailManager.send("noreply@sm.ecosystem.co", "p@q.rs", new String(bytes));
            new SimpleMailApp();
        });
    }
}