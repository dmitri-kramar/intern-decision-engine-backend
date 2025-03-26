package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.config.DecisionEngineConstants;
import ee.taltech.inbankbackend.endpoint.DecisionRequest;
import ee.taltech.inbankbackend.endpoint.DecisionResponse;
import ee.taltech.inbankbackend.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DecisionEngineTest {

    @InjectMocks
    private DecisionEngine decisionEngine;

    private String debtorPersonalCode;
    private String segment1PersonalCode;
    private String segment2PersonalCode;
    private String segment3PersonalCode;

    @BeforeEach
    void setUp() {
        debtorPersonalCode = "49002010965";
        segment1PersonalCode = "49002010976";
        segment2PersonalCode = "49002010987";
        segment3PersonalCode = "49002010998";
    }

    @Test
    void testDebtorPersonalCode() {
        DecisionRequest request = new DecisionRequest(debtorPersonalCode, 4000, 12);

        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(request));
    }

    @Test
    void testSegment1PersonalCode() {
        DecisionRequest request = new DecisionRequest(segment1PersonalCode, 4000, 12);
        DecisionResponse response = decisionEngine.calculateApprovedLoan(request);

        assertEquals(2000, response.loanAmount());
        assertEquals(20, response.loanPeriod());
    }

    @Test
    void testSegment2PersonalCode() {
        DecisionRequest request = new DecisionRequest(segment2PersonalCode, 4000, 12);
        DecisionResponse response = decisionEngine.calculateApprovedLoan(request);

        assertEquals(3600, response.loanAmount());
        assertEquals(12, response.loanPeriod());
    }

    @Test
    void testSegment3PersonalCode() {
        DecisionRequest request = new DecisionRequest(segment3PersonalCode, 4000, 12);
        DecisionResponse response = decisionEngine.calculateApprovedLoan(request);

        assertEquals(10000, response.loanAmount());
        assertEquals(12, response.loanPeriod());
    }

    @Test
    void testInvalidPersonalCode() {
        DecisionRequest request = new DecisionRequest("12345678901", 4000, 12);
        assertThrows(InvalidPersonalCodeException.class,
                () -> decisionEngine.calculateApprovedLoan(request));
    }

    @Test
    void testInvalidLoanAmount() {
        int validPeriod = 12;

        int tooLowLoanAmount = DecisionEngineConstants.MINIMUM_LOAN_AMOUNT - 1;
        int tooHighLoanAmount = DecisionEngineConstants.MAXIMUM_LOAN_AMOUNT + 1;

        DecisionRequest lowAmountRequest = new DecisionRequest(segment1PersonalCode, tooLowLoanAmount, validPeriod);
        DecisionRequest highAmountRequest = new DecisionRequest(segment1PersonalCode, tooHighLoanAmount, validPeriod);

        assertThrows(InvalidLoanAmountException.class, () -> decisionEngine.calculateApprovedLoan(lowAmountRequest));
        assertThrows(InvalidLoanAmountException.class, () -> decisionEngine.calculateApprovedLoan(highAmountRequest));
    }


    @Test
    void testInvalidLoanPeriod() {
        int validAmount = 4000;

        int tooShortLoanPeriod = DecisionEngineConstants.MINIMUM_LOAN_PERIOD - 1;
        int tooLongLoanPeriod = DecisionEngineConstants.MAXIMUM_LOAN_PERIOD + 1;

        DecisionRequest shortPeriodRequest = new DecisionRequest(segment1PersonalCode, validAmount, tooShortLoanPeriod);
        DecisionRequest longPeriodRequest = new DecisionRequest(segment1PersonalCode, validAmount, tooLongLoanPeriod);

        assertThrows(InvalidLoanPeriodException.class, () -> decisionEngine.calculateApprovedLoan(shortPeriodRequest));
        assertThrows(InvalidLoanPeriodException.class, () -> decisionEngine.calculateApprovedLoan(longPeriodRequest));
    }


    @Test
    void testFindSuitableLoanPeriod() {
        DecisionRequest request = new DecisionRequest(segment2PersonalCode, 2000, 12);
        DecisionResponse response = decisionEngine.calculateApprovedLoan(request);

        assertEquals(3600, response.loanAmount());
        assertEquals(12, response.loanPeriod());
    }


    @Test
    void testNoValidLoanFound() {
        DecisionRequest request = new DecisionRequest(debtorPersonalCode, 10000, 48);
        assertThrows(NoValidLoanException.class,
                () -> decisionEngine.calculateApprovedLoan(request));
    }

    @Test
    void testIneligibleAge() {
        String underagePersonalCode = "61502200230";
        String elderlyPersonalCode = "43912090313";

        DecisionRequest underageRequest = new DecisionRequest(underagePersonalCode, 4000, 12);
        DecisionRequest elderlyRequest = new DecisionRequest(elderlyPersonalCode, 4000, 12);

        assertThrows(IneligibleAgeException.class, () -> decisionEngine.calculateApprovedLoan(underageRequest));
        assertThrows(IneligibleAgeException.class, () -> decisionEngine.calculateApprovedLoan(elderlyRequest));
    }

}

