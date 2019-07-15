package ecosystem;

import ecosystem.components.NotificationBadge;
import java.awt.GridLayout;
import java.awt.Rectangle;
import javax.swing.JFrame;

public class TestFrame extends JFrame {
    public TestFrame() {
        super("comida");
        this.setBounds(new Rectangle(200, 200, 200, 200));
        getContentPane().setLayout(new GridLayout(1, 1));
        getContentPane().add(new NotificationBadge("comida"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new TestFrame();
        });
    }
}
