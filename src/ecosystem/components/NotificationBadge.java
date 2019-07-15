package ecosystem.components;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JComponent;

public class NotificationBadge extends JComponent {
    private String text;

    public NotificationBadge(String text, Rectangle rect) {
        this.text = text;
        if (rect != null)
            this.setBounds(rect);
    }

    public NotificationBadge(String text) {
        this(text, null);
    }

    public NotificationBadge(String text, Container container, int width, int height) {
        this(text, new Rectangle((container.getWidth() - width) / 2, (container.getHeight() - height) / 2, width, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle bounds = getBounds();
        g.setColor(Color.RED);
        g.fillOval(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);

        Font font = this.getFont();
        FontMetrics fm = g.getFontMetrics(font);
        int sw = fm.stringWidth(text), sh = fm.getMaxAscent();
        g.setFont(font);
        g.drawString(text, (bounds.width - sw) / 2, (bounds.height + sh) / 2);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.repaint();
    }
}
