package ecosystem;

import ecosystem.academic.Subject;

public class Financial {
    public static double ENROLLMENT_COST = 20000;
    public static double COST_PER_CREDIT = 35000;

    public static double getSubjectCost(Subject s) {
        return COST_PER_CREDIT * s.getCredits();
    }
}
