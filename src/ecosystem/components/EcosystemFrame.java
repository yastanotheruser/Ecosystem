package ecosystem.components;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import org.netbeans.lib.awtextra.AbsoluteConstraints;
import org.netbeans.lib.awtextra.AbsoluteLayout;

public class EcosystemFrame extends JFrame {
    private static final int DEFAULT_WIDTH = 900;
    private static final int DEFAULT_HEIGHT = 600;

    public final JFrame self = this;
    private int initialX, initialY;
    private int mouseStartX, mouseStartY;
    private int width, height;

    public EcosystemFrame(int width, int height) {
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/ecosystem.png")));
        this.width = width;
        this.height = height;

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x0 = (dim.width - width) / 2, y0 = (dim.height - height) / 2;
        int x1 = x0 + width, y1 = y0 + height;

        setBounds(x0, y0, x1, y1);
        initComponents();
        initEvents();
        setTitle("Ecosystem");
        getContentPane().requestFocusInWindow();
    }

    public EcosystemFrame() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    private void initComponents() {
        container = this.getContentPane();
        buttonMinimize = new ESButton();
        buttonClose = new ESButton();

        setUndecorated(true);

        container.setBackground(new Color(34, 63, 49));
        container.setPreferredSize(new Dimension(width, height));
        container.setLayout(new AbsoluteLayout());

        buttonMinimize.setIcon(new ImageIcon(getClass().getResource("/images/minimize.png")));
        buttonMinimize.setRolloverIcon(new ImageIcon(getClass().getResource("/images/minimize-active.png")));
        buttonMinimize.setToolTipText("Minimizar");

        buttonMinimize.addActionListener((ActionEvent evt) -> {
            buttonMinimizeActionPerformed(evt);
        });

        container.add(buttonMinimize, new AbsoluteConstraints(width - 85, 5, 40, 40));

        buttonClose.setIcon(new ImageIcon(getClass().getResource("/images/exit.png")));
        buttonClose.setRolloverIcon(new ImageIcon(getClass().getResource("/images/exit-active.png")));
        buttonClose.setToolTipText("Cerrar");

        buttonClose.addActionListener((ActionEvent evt) -> {
            buttonCloseActionPerformed(evt);
        });

        container.add(buttonClose, new AbsoluteConstraints(width - 45, 5, 40, 40));
        pack();
    }

    private void initEvents() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent evt) {
                formMouseDragged(evt);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                formMousePressed(evt);
                getContentPane().requestFocusInWindow();
            }
            @Override
            public void mouseReleased(MouseEvent evt) {
                formMouseReleased(evt);
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void formMousePressed(MouseEvent evt) {
        final Point position = getLocation();
        initialX = position.x;
        initialY = position.y;
        mouseStartX = evt.getXOnScreen();
        mouseStartY = evt.getYOnScreen();
    }

    private void formMouseReleased(MouseEvent evt) {
        initialX = initialY = -1;
        mouseStartX = mouseStartY = -1;
    }

    private void formMouseDragged(MouseEvent evt) {
        if (initialX != -1 && initialY != -1) {
            final int x = initialX - mouseStartX + evt.getXOnScreen();
            final int y = initialY - mouseStartY + evt.getYOnScreen();
            setLocation(x, y);
        }
    }

    private ESButton buttonClose;
    private ESButton buttonMinimize;
    private Container container;

    private void buttonMinimizeActionPerformed(ActionEvent evt) {
        this.setState(JFrame.ICONIFIED);
    }

    private void buttonCloseActionPerformed(ActionEvent evt) {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
}
    