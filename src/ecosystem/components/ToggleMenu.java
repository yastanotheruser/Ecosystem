package ecosystem.components;

import ecosystem.event.MouseClickedListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JPanel;

public class ToggleMenu {
    private TogglePanel[] menu;
    private JPanel[] views;
    private ArrayList<MenuListener> listeners;

    public ToggleMenu(TogglePanel[] menu, JPanel[] views) {
        this.menu = menu;
        this.views = views;
        listeners = new ArrayList<>();
        initMenu();
    }

    private final MouseClickedListener menuListener = new MouseClickedListener() {
        @Override
        public void mouseClicked(MouseEvent me) {
            TogglePanel self = (TogglePanel) me.getSource();
            if (!self.enabled)
                return;

            int index = toggleMenuItem(self);
            if (index != -1)
                listeners.forEach(l -> l.selectOption(self, index));
        }
    };

    private void initMenu() {
        for (TogglePanel tp : menu)
            tp.addMouseListener(menuListener);
    }

    public void toggleMenuItem(int index) {
        for (int i = 0; i < menu.length; i++)
            menu[i].toggle(i == index);

        for (int i = 0; i < menu.length; i++)
            views[i].setVisible(i == index);
    }

    public int toggleMenuItem(TogglePanel item) {
        for (int i = 0; i < menu.length; i++) {
            if (menu[i] == item) {
                toggleMenuItem(i);
                return i;
            }
        }

        return -1;
    }

    public void setEnabled(int index, boolean enabled) {
        menu[index].setEnabled(enabled);
    }

    public void changeAllExcept(boolean enabled, int[] indices) {
        for (int i = 0; i < menu.length; i++) {
            TogglePanel tp = menu[i];
            boolean exclude = false;

            if (indices != null) {
                for (int j : indices) {
                    if (j == i) {
                        exclude = true;
                        break;
                    }
                }
            }

            if (!exclude)
                tp.setEnabled(enabled);
        }
    }

    public void disableAll() {
        disableAllExcept(null);
    }

    public void disableAllExcept(int[] indices) {
        changeAllExcept(false, indices);
    }

    public void disableAllExcept(int index) {
        disableAllExcept(new int[] { index });
    }

    public void enableAll() {
        enableAllExcept(null);
    }

    public void enableAllExcept(int[] indices) {
        changeAllExcept(true, indices);
    }

    public void enableAllExcept(int index) {
        enableAllExcept(new int[] { index });
    }

    public void addMenuListener(MenuListener listener) {
        this.listeners.add(listener);
    }

    public void removeMenuListener(MenuListener listener) {
        this.listeners.remove(listener);
    }
}
