package ecosystem.util;

import java.awt.Component;
import java.awt.Image;
import javax.swing.ImageIcon;

public class ImageUtil {
    public static ImageIcon resizeImage(ImageIcon imgIcon, Component container) {
        int w = container.getWidth(), h = container.getHeight();
        if (container != null) {
            int w0 = imgIcon.getIconWidth(), h0 = imgIcon.getIconHeight();
            double wq = ((double) w) / w0, hq = ((double) h) / h0;

            if (wq < hq)
                h = (int) (wq * h0);
            else
                w = (int) (hq * w0);
        }

        Image img = imgIcon.getImage().getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    public static ImageIcon resizeImage(String path, Component container) {
        return resizeImage(new ImageIcon(path), container);
    }
}
