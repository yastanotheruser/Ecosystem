package ecosystem.mailing;

import ecosystem.util.DataManager;
import java.util.ArrayList;

public class EmailManager extends DataManager<Email> {
    private ArrayList<EmailListener> listeners;

    public EmailManager(String filename) {
        super(filename);
        listeners = new ArrayList<>();
    }

    @Override
    public boolean add(Email email, boolean update) {
        if (!super.add(email, update))
            return false;

        for (EmailListener listener : listeners)
            listener.emailAdded(email);

        return true;
    }

    public boolean send(Email email) {
        return add(email, true);
    }

    public boolean send(String from, String to, String body) {
        return add(new Email(from, to, body), true);
    }

    public void addEmailListener(EmailListener listener) {
        listeners.add(listener);
    }

    public void removeEmailListener(EmailListener listener) {
        listeners.remove(listener);
    }
}
