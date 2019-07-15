package ecosystem.mailing;

import ecosystem.util.Unique;
import java.util.Random;

public class Email extends Unique {
    private static final Random random = new Random();

    private static String generateRandomId() {
        byte[] randomBytes = new byte[64];
        random.nextBytes(randomBytes);
        return new String(randomBytes);
    }

    private final String from;
    private final String to;
    private final String body;

    public Email(String from, String to, String body) {
        super(generateRandomId());
        this.from = from;
        this.to = to;
        this.body = body;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getBody() {
        return body;
    }
}
