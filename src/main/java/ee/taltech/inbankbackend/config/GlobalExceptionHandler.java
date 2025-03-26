package ee.taltech.inbankbackend.config;

import ee.taltech.inbankbackend.endpoint.DecisionResponse;
import ee.taltech.inbankbackend.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler (InvalidPersonalCodeException.class)
    public ResponseEntity<DecisionResponse> handleInvalidPersonalCodeException(InvalidPersonalCodeException e) {
        DecisionResponse response = new DecisionResponse(null, null, e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler (InvalidLoanAmountException.class)
    public ResponseEntity<DecisionResponse> handleInvalidLoanAmountException(InvalidLoanAmountException e) {
        DecisionResponse response = new DecisionResponse(null, null, e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidLoanPeriodException.class)
    public ResponseEntity<DecisionResponse> handleInvalidLoanPeriodException(InvalidLoanPeriodException e) {
        DecisionResponse response = new DecisionResponse(null, null, e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoValidLoanException.class)
    public ResponseEntity<DecisionResponse> handleNoValidLoanException(NoValidLoanException e) {
        DecisionResponse response = new DecisionResponse(null, null, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IneligibleAgeException.class)
    public ResponseEntity<DecisionResponse> handleIneligibleAgeException(IneligibleAgeException e) {
        DecisionResponse response = new DecisionResponse(null, null, e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DecisionResponse> handleGenericException(Exception ignored) {
        DecisionResponse response = new DecisionResponse(null, null, "An unexpected error occurred");
        return ResponseEntity.internalServerError().body(response);
    }
}
