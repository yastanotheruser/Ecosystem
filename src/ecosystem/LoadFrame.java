package ecosystem;

import ecosystem.components.EcosystemFrame;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadFrame extends EcosystemFrame {
    public LoadFrame() {
        super(810, 510);
        initComponents();
        setVisible(true);

        try {
            loadDelay(500L);
        } catch (InterruptedException ex) {
            Logger.getLogger(LoadFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadDelay(long time) throws InterruptedException {
        long millis = time / 100;
        for (int i = 0; i <= 100; i++) {
            Thread.sleep(millis);
            jLabelProgress.setText(Integer.toString(i) + "%");
            jProgressBar.setValue(i);
        }

        new SigninFrame();
        this.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar();
        jProgressBar2 = new javax.swing.JProgressBar();
        jPanelMain = new javax.swing.JPanel();
        jLabelEcosystem = new javax.swing.JLabel();
        jProgressBar = new javax.swing.JProgressBar();
        jLabelProgress = new javax.swing.JLabel();
        jLabelProduct = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanelMain.setBackground(new java.awt.Color(46, 49, 49));
        jPanelMain.setEnabled(false);
        jPanelMain.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelEcosystem.setBackground(new java.awt.Color(44, 130, 201));
        jLabelEcosystem.setFont(new java.awt.Font("Trebuchet MS", 1, 48)); // NOI18N
        jLabelEcosystem.setForeground(new java.awt.Color(44, 201, 130));
        jLabelEcosystem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/passed-exam.png"))); // NOI18N
        jLabelEcosystem.setText("EcoSystem");
        jLabelEcosystem.setToolTipText("");
        jPanelMain.add(jLabelEcosystem, new org.netbeans.lib.awtextra.AbsoluteConstraints(234, 140, -1, -1));

        jProgressBar.setForeground(new java.awt.Color(44, 201, 130));
        jProgressBar.setBorderPainted(false);
        jProgressBar.setFocusable(false);
        jProgressBar.setRequestFocusEnabled(false);
        jProgressBar.setVerifyInputWhenFocusTarget(false);
        jPanelMain.add(jProgressBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, 470, 820, 40));

        jLabelProgress.setFont(new java.awt.Font("Trebuchet MS", 1, 24)); // NOI18N
        jLabelProgress.setForeground(new java.awt.Color(44, 201, 130));
        jLabelProgress.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelProgress.setText("0%");
        jLabelProgress.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanelMain.add(jLabelProgress, new org.netbeans.lib.awtextra.AbsoluteConstraints(405, 430, -1, -1));

        jLabelProduct.setFont(new java.awt.Font("Trebuchet MS", 0, 36)); // NOI18N
        jLabelProduct.setForeground(new java.awt.Color(44, 201, 130));
        jLabelProduct.setText("Sistema Integrado de Matriculas");
        jPanelMain.add(jLabelProduct, new org.netbeans.lib.awtextra.AbsoluteConstraints(146, 248, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMain, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            new LoadFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelEcosystem;
    private javax.swing.JLabel jLabelProduct;
    public javax.swing.JLabel jLabelProgress;
    private javax.swing.JPanel jPanelMain;
    public javax.swing.JProgressBar jProgressBar;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JProgressBar jProgressBar2;
    // End of variables declaration//GEN-END:variables
}
