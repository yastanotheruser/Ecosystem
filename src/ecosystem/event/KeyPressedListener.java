package ecosystem.event;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public abstract class KeyPressedListener implements KeyListener {
    @Override
    public void keyTyped(KeyEvent ke) {}
    
    @Override
    public abstract void keyPressed(KeyEvent ke);
    
    @Override
    public void keyReleased(KeyEvent ke) {}
}