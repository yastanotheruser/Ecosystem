package ecosystem.util;

import java.util.regex.Pattern;
import javax.swing.text.JTextComponent;

public class RegexValidator {
    public final static RegexValidator
    NUMBER_VALIDATOR = new RegexValidator("^\\d+$"),
    ALPHA_VALIDATOR = new RegexValidator("^[a-zñáéíóú\\- ]+$", Pattern.CASE_INSENSITIVE),
    PHONE_VALIDATOR = new RegexValidator("^\\+?\\d+$"),
    EMAIL_VALIDATOR = new RegexValidator("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}", Pattern.CASE_INSENSITIVE);

    private Pattern pattern;

    public RegexValidator(String expr, int flags) {
        pattern = Pattern.compile(expr, flags);
    }

    public RegexValidator(String expr) {
        pattern = Pattern.compile(expr);
    }

    public boolean validate(JTextComponent jtc) {
        return pattern.matcher(jtc.getText()).matches();
    }
}
