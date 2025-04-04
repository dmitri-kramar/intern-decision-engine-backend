package ee.taltech.inbankbackend.config;

/**
 * Holds all necessary constants for the decision engine.
 */
public class DecisionEngineConstants {
    public static final int MINIMUM_LOAN_AMOUNT = 2000;
    public static final int MAXIMUM_LOAN_AMOUNT = 10000;
    public static final int MINIMUM_LOAN_PERIOD = 12;
    public static final int MAXIMUM_LOAN_PERIOD = 48;
    public static final int SEGMENT_1_CREDIT_MODIFIER = 100;
    public static final int SEGMENT_2_CREDIT_MODIFIER = 300;
    public static final int SEGMENT_3_CREDIT_MODIFIER = 1000;
    public static final double CREDIT_SCORE_THRESHOLD = 0.1;

    public static final int MINIMUM_AGE = 18;
    public static final int LIFE_EXPECTANCY_ESTONIA = 84;
    public static final int LIFE_EXPECTANCY_LATVIA = 82;
    public static final int LIFE_EXPECTANCY_LITHUANIA = 86;
}
