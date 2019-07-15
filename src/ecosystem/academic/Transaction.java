package ecosystem.academic;

import ecosystem.util.MD5Hash;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;

public class Transaction implements Serializable {
    public String transactionId;
    public String studentId;
    public LocalDateTime when;
    public ArrayList<String> subjects;
    public TransactionType type;

    public Transaction(String studentId, ArrayList<String> subjects, TransactionType type) {
        this.studentId = studentId;
        this.when = LocalDateTime.now();
        this.transactionId = MD5Hash.getHash(studentId + "-" + this.when.toEpochSecond(ZoneOffset.UTC));
        this.subjects = subjects;
        this.type = type;
    }
    
    public Transaction(String studentId, String[] subjects, TransactionType type) {
        this(studentId, new ArrayList<>(Arrays.asList(subjects)), type);
    }
}
