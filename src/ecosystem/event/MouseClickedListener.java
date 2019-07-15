package ecosystem.event;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public interface MouseClickedListener extends MouseListener {
    @Override
    public void mouseClicked(MouseEvent me);
    
    @Override
    public default void mousePressed(MouseEvent me) {}

    @Override
    public default void mouseReleased(MouseEvent me) {}
    
    @Override
    public default void mouseEntered(MouseEvent me) {}
    
    @Override
    public default void mouseExited(MouseEvent me) {}
}