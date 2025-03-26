package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeParser;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.endpoint.DecisionRequest;
import ee.taltech.inbankbackend.endpoint.DecisionResponse;
import ee.taltech.inbankbackend.exceptions.*;
import org.springframework.stereotype.Service;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan decision includes the maximum amount that can be approved and the earliest possible period,
 * starting from the one requested by the applicant, at which the credit score threshold is met.
 */
@Service
public class DecisionEngine {

    // Used to check for the validity of the presented ID code.
    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();

    // Used to parse data from the presented ID code.
    private final EstonianPersonalCodeParser parser = new EstonianPersonalCodeParser();

    /**
     * Evaluates a loan request and returns the maximum approved amount and the earliest suitable period.
     * If the requested loan period does not result in an acceptable credit score,
     * the system searches for the shortest possible period (greater than or equal to the requested one)
     * that results in an approved credit score. Then, it calculates the maximum amount that can be approved
     * for that period, limited by the system's upper loan boundaries.
     *
     * @param request The loan request containing personal code, amount and period.
     * @return DecisionResponse with approved amount and period.
     * @throws InvalidPersonalCodeException if the personal code is not valid
     * @throws InvalidLoanAmountException if the loan amount is out of allowed range
     * @throws InvalidLoanPeriodException if the loan period is out of allowed range
     * @throws NoValidLoanException if no suitable loan can be approved
     */
    public DecisionResponse calculateApprovedLoan(DecisionRequest request) {
        validateInputs(request);
        validateAge(request.personalCode());

        int creditModifier = getCreditModifier(request.personalCode());
        int approvedPeriod = findEarliestEligiblePeriod(creditModifier, request.loanPeriod());
        int approvedAmount = calculateMaxAmount(creditModifier, approvedPeriod);

        return new DecisionResponse(approvedAmount, approvedPeriod, null);
    }

    /**
     * Finds the shortest possible loan period (starting from the user's requested period)
     * where the credit score meets the threshold, assuming the minimum allowed loan amount.
     *
     * @param creditModifier Credit modifier based on the applicant's personal code.
     * @param startingPeriod The initial period requested by the applicant.
     * @return The first eligible loan period that passes the credit score threshold.
     * @throws NoValidLoanException if no suitable period can be found up to the maximum allowed.
     */
    private int findEarliestEligiblePeriod(int creditModifier, int startingPeriod) {
        for (int period = startingPeriod; period <= DecisionEngineConstants.MAXIMUM_LOAN_PERIOD; period++) {
            double creditScore = calculateCreditScore(creditModifier, DecisionEngineConstants.MINIMUM_LOAN_AMOUNT, period);
            if (creditScore >= DecisionEngineConstants.CREDIT_SCORE_THRESHOLD) {
                return period;
            }
        }
        throw new NoValidLoanException("No valid loan found!");
    }

    /**
     * Calculates the maximum loan amount based on the credit modifier and period.
     * The amount is capped by the system's maximum allowed loan amount.
     *
     * @param creditModifier Credit modifier based on the applicant's personal code.
     * @param period The approved loan period.
     * @return The maximum approved loan amount for the given period.
     */
    private int calculateMaxAmount(int creditModifier, int period) {
        return Math.min(creditModifier * period, DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT);
    }

    /**
     * Calculates the credit score based on the given modifier, amount and period.
     * This score is used to determine whether a loan should be approved.
     *
     * @param creditModifier Credit modifier based on the applicant's personal code.
     * @param loanAmount The loan amount to evaluate.
     * @param loanPeriod The loan period in months.
     * @return A double value representing the calculated credit score.
     */
    @SuppressWarnings("SameParameterValue")
    private double calculateCreditScore(int creditModifier, Integer loanAmount, int loanPeriod) {
        return ((double) creditModifier / loanAmount) * loanPeriod / 10;
    }

    /**
     * Determines the applicant's credit modifier based on the last digit of their personal code.
     * - Digits 4 or 6 → Segment 1 (low modifier)
     * - Digits 3 or 7 → Segment 2 (medium modifier)
     * - Digits 8 or 9 → Segment 3 (high modifier)
     * - Any other digit → Treated as debt (loan not approved)
     *
     * @param personalCode The applicant's personal identification code.
     * @return Credit modifier for the applicant.
     * @throws NoValidLoanException if the applicant is in debt segment.
     */
    private int getCreditModifier(String personalCode) {
        int lastDigit = Integer.parseInt(personalCode.substring(personalCode.length() - 1));

        return switch (lastDigit) {
            case 4, 6 -> DecisionEngineConstants.SEGMENT_1_CREDIT_MODIFIER;
            case 3, 7 -> DecisionEngineConstants.SEGMENT_2_CREDIT_MODIFIER;
            case 8, 9 -> DecisionEngineConstants.SEGMENT_3_CREDIT_MODIFIER;
            default -> throw new NoValidLoanException("No valid loan found!");
        };
    }

    /**
     * Validates the loan request data against business rules.
     *
     * @param request The loan request containing personal code, amount and period.
     * @throws InvalidPersonalCodeException if the personal code is not valid
     * @throws InvalidLoanAmountException if the loan amount is outside 2000–10000 €
     * @throws InvalidLoanPeriodException if the loan period is outside 12–48 months
     */
    private void validateInputs(DecisionRequest request) {
        String personalCode = request.personalCode();
        Integer loanAmount = request.loanAmount();
        Integer loanPeriod = request.loanPeriod();

        if (personalCode == null || !validator.isValid(personalCode)) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }

        if (loanAmount == null ||
                loanAmount < DecisionEngineConstants.MINIMUM_LOAN_AMOUNT ||
                loanAmount > DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT) {
            throw new InvalidLoanAmountException("Invalid loan amount!");
        }

        if (loanPeriod == null ||
                loanPeriod < DecisionEngineConstants.MINIMUM_LOAN_PERIOD ||
                loanPeriod > DecisionEngineConstants.MAXIMUM_LOAN_PERIOD) {
            throw new InvalidLoanPeriodException("Invalid loan period!");
        }
    }

    /**
     * Validates whether the applicant's age is within the eligible range for receiving a loan.
     * The applicant's age is determined from the personal code. The country is identified by the first digit
     * of the code, and each country has its own life expectancy. The maximum allowed age is calculated as:
     * (life expectancy - maximum loan period in years).
     *
     * @param personalCode The applicant's personal identification code.
     * @throws IneligibleAgeException if the applicant's age is not within the allowed range
     * @throws InvalidPersonalCodeException if the personal code is invalid or cannot be parsed
     */
    private void validateAge(String personalCode) {
        try {
            int age = parser.getAge(personalCode).getYears();
            int maxAge = switch (personalCode.charAt(0)) {
                case '4' -> DecisionEngineConstants.LIFE_EXPECTANCY_LATVIA;
                case '5' -> DecisionEngineConstants.LIFE_EXPECTANCY_LITHUANIA;
                default -> DecisionEngineConstants.LIFE_EXPECTANCY_ESTONIA;
            };

            if (age < DecisionEngineConstants.MINIMUM_AGE ||
                    age > maxAge - DecisionEngineConstants.MAXIMUM_LOAN_PERIOD / 12) {
                throw new IneligibleAgeException("Age is not eligible for a loan!");
            }
        } catch (PersonalCodeException e) {
            throw new InvalidPersonalCodeException("Invalid personal ID code!");
        }
    }
}
