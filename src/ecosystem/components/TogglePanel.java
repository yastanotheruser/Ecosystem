package ecosystem.components;

import ecosystem.event.ToggleListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

public class TogglePanel extends JPanel {
    public Color previousColor = null;
    public Color hoverColor= null;
    public boolean active = false, enabled = true;

    public TogglePanel(Color hoverColor) {
        this.hoverColor = hoverColor;
        this.addMouseListener(new ToggleListener() {
            @Override
            public void mouseEntered(MouseEvent me) {
                if (active || !enabled)
                    return;

                final TogglePanel self = (TogglePanel) me.getSource();
                previousColor = self.getBackground();
                self.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent me) {
                if (active || !enabled)
                    return;

                final TogglePanel self = (TogglePanel) me.getSource();
                self.setBackground(previousColor);
            }
        });
    }

    public void toggle(boolean active) {
        if (previousColor == null)
            previousColor = getBackground();

        this.active = active;
        if (active)
            setBackground(hoverColor);
        else
            setBackground(previousColor);
    }

    public void toggle() {
        toggle(!active);
    }

    @Override
    public void setEnabled(boolean enabled) {
        for (Component c : this.getComponents())
            c.setEnabled(enabled);

        this.enabled = enabled;
    }
}
