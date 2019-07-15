package ecosystem.util;

import ecosystem.event.KeyPressedListener;
import ecosystem.event.KeyReleasedListener;
import java.awt.Color;;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.text.JTextComponent;

public class FormValidator {
    private static final Color DEFAULT_BACKGROUND = new Color(255, 255, 255);
    private static final Color DEFAULT_INVALID_BACKGROUND = new Color(200, 200, 200);

    private Color fieldBackground, invalidFieldBackground;
    private JTextComponent[] fields;
    private JButton submitButton;

    public FormValidator(JTextComponent[] fields, Color field, Color invalidField, JButton submitButton) {
        this.fields = fields;
        fieldBackground = field;
        invalidFieldBackground = invalidField;
        initValidationEvents();

        if (submitButton != null) {
            this.submitButton = submitButton;
            initSubmitEvents();
        }
    }

    public FormValidator(JTextComponent[] fields, Color field, Color invalidField) {
        this(fields, field, invalidField, null);
    }

    public FormValidator(JTextComponent[] fields, JButton submitButton) {
        this(fields, DEFAULT_BACKGROUND, DEFAULT_INVALID_BACKGROUND, submitButton);
    }

    public FormValidator(JTextComponent[] fields) {
        this(fields, DEFAULT_BACKGROUND, DEFAULT_INVALID_BACKGROUND, null);
    }

    public boolean validateForm() {
        if (fields == null)
            return false;

        JTextComponent nextTarget = null;
        for (JTextComponent jtc : fields) {
            if (!isValidField(jtc) && nextTarget == null)
                nextTarget = jtc;
        }

        if (nextTarget != null)
            nextTarget.requestFocus();

        return nextTarget == null;
    }

    private void initValidationEvents() {
        for (JTextComponent jtc : fields) {
            jtc.addFocusListener(focusValidationListener);
            jtc.addKeyListener(keyValidationListener);
        }
    }

    private void initSubmitEvents() {
        for (JTextComponent jtc : fields) {
            jtc.addKeyListener(new KeyPressedListener() {
                @Override
                public void keyPressed(KeyEvent ke) {
                    if (ke.getKeyCode() == 10)
                        submitButton.doClick();
                }
            });
        }
    }

    public boolean isValidField(JTextComponent jtc) {
        final RegexValidator validator = (RegexValidator) jtc.getClientProperty("validator");
        Object value = jtc.getClientProperty("required");
        boolean required = (value == null) ? false : Boolean.TRUE.equals(value);
        boolean validity = true;

        if (validator == null)
            validity = (!required || jtc.getText().length() > 0);
        else if (!required && jtc.getText().isEmpty())
            validity = true;
        else
            validity = validator.validate(jtc);

        jtc.setBackground(validity ? fieldBackground : invalidFieldBackground);
        return validity;
    }

    private void validationHandler(ComponentEvent e) {
        isValidField((JTextComponent) e.getSource());
    }

    public void focus(JTextComponent jtc) {
        jtc.setBackground(invalidFieldBackground);
        jtc.requestFocus();
    }

    public void clear() {
        for (JTextComponent jtc : fields)
            jtc.setText("");
    }

    private final FocusListener focusValidationListener = new FocusListener() {
        @Override
        public void focusGained(FocusEvent fe) {}

        @Override
        public void focusLost(FocusEvent fe) {
            validationHandler(fe);
        }
    };

    private final KeyListener keyValidationListener = new KeyReleasedListener() {
        @Override
        public void keyReleased(KeyEvent ke) {
            validationHandler(ke);
        }
    };
}