package ee.taltech.inbankbackend.exceptions;

/**
 * Thrown when applicant's age is outside the allowed range.
 */
public class IneligibleAgeException extends RuntimeException {

    public IneligibleAgeException(String message) {
        super(message);
    }
}
