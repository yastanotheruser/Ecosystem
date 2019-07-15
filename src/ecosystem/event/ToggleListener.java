
package ecosystem.event;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public abstract class ToggleListener implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent me) {}

    @Override
    public void mousePressed(MouseEvent me) {}

    @Override
    public void mouseReleased(MouseEvent me) {}
    
    public abstract void mouseEntered(MouseEvent me);
    public abstract void mouseExited(MouseEvent me);
}
