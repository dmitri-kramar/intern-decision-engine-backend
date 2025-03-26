package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when requested loan period is invalid.
 */
public class InvalidLoanPeriodException extends RuntimeException {

    public InvalidLoanPeriodException(String message) {
        super(message);
    }
}
