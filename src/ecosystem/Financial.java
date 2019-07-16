package ecosystem;

import ecosystem.academic.Subject;

public class Financial {
    public static float ENROLLMENT_COST = 20000;
    public static float COST_PER_CREDIT = 10000;

    public static float getSubjectCost(Subject s) {
        return COST_PER_CREDIT * s.getCredits();
    }
}
