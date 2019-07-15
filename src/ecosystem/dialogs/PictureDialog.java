package ecosystem.dialogs;

import ecosystem.components.EcosystemDialog;
import java.awt.Dimension;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JFrame;
import org.netbeans.lib.awtextra.AbsoluteConstraints;

public class PictureDialog extends EcosystemDialog {
    private final JLabel jLabelImageContainer;

    public PictureDialog(JFrame parent, Icon image) {
        super(parent, true, image.getIconWidth(), image.getIconHeight());
        jLabelImageContainer = new JLabel();
        jLabelImageContainer.setPreferredSize(new Dimension(width, height));
        jLabelImageContainer.setIcon(image);
        this.getContentPane().add(jLabelImageContainer, new AbsoluteConstraints(0, 0));
        setVisible(true);
    }
    
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            URL testImageURL = PictureDialog.class.getResource("/images/doggo.jpg");
            new PictureDialog(null, new ImageIcon(testImageURL));
        });
    }
}
