package ee.taltech.inbankbackend.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.inbankbackend.exceptions.*;
import ee.taltech.inbankbackend.service.DecisionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.hamcrest.Matchers.nullValue;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class holds integration tests for the DecisionEngineController endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class DecisionEngineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DecisionEngine decisionEngine;

    private ObjectMapper objectMapper;

    private DecisionRequest request;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        request = new DecisionRequest("1234", 10, 10);
    }

    /**
     * This method tests the /loan/decision endpoint with valid inputs.
     */
    @Test
    public void givenValidRequest_whenRequestDecision_thenReturnsExpectedResponse() throws Exception {
        DecisionResponse expectedResponse = new DecisionResponse(1000, 12, null);
        when(decisionEngine.calculateApprovedLoan(any(DecisionRequest.class))).thenReturn(expectedResponse);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").value(1000))
                .andExpect(jsonPath("$.loanPeriod").value(12))
                .andExpect(jsonPath("$.errorMessage").isEmpty())
                .andReturn();

        DecisionResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.loanAmount().equals(1000);
        assert response.loanPeriod().equals(12);
        assert response.errorMessage() == null;
    }

    /**
     * This test ensures that if an invalid personal code is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidPersonalCode_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(any(DecisionRequest.class)))
                .thenThrow(new InvalidPersonalCodeException("Invalid personal code"));

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").value(nullValue()))
                .andExpect(jsonPath("$.loanPeriod").value(nullValue()))
                .andExpect(jsonPath("$.errorMessage").value("Invalid personal code"))
                .andReturn();

        DecisionResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.loanAmount() == null;
        assert response.loanPeriod() == null;
        assert response.errorMessage().equals("Invalid personal code");
    }

    /**
     * This test ensures that if an invalid loan amount is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidLoanAmount_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(any(DecisionRequest.class)))
                .thenThrow(new InvalidLoanAmountException("Invalid loan amount"));

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount", nullValue()))
                .andExpect(jsonPath("$.loanPeriod", nullValue()))
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan amount"))
                .andReturn();

        DecisionResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.loanAmount() == null;
        assert response.loanPeriod() == null;
        assert response.errorMessage().equals("Invalid loan amount");
    }

    /**
     * This test ensures that if an invalid loan period is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidLoanPeriod_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(any(DecisionRequest.class)))
                .thenThrow(new InvalidLoanPeriodException("Invalid loan period"));

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount", nullValue()))
                .andExpect(jsonPath("$.loanPeriod", nullValue()))
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan period"))
                .andReturn();

        DecisionResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.loanAmount() == null;
        assert response.loanPeriod() == null;
        assert response.errorMessage().equals("Invalid loan period");
    }


    /**
     * This test ensures that if no valid loan is found, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenNoValidLoan_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(any(DecisionRequest.class)))
                .thenThrow(new NoValidLoanException("No valid loan available"));

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount", nullValue()))
                .andExpect(jsonPath("$.loanPeriod", nullValue()))
                .andExpect(jsonPath("$.errorMessage").value("No valid loan available"))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.loanAmount() == null;
        assert response.loanPeriod() == null;
        assert response.errorMessage().equals("No valid loan available");
    }


    /**
     * This test ensures that if an unexpected error occurs when processing the request, the controller returns
     * an HTTP Internal Server Error (500) response with the appropriate error message in the response body.
     */
    @Test
    public void givenUnexpectedError_whenRequestDecision_thenReturnsInternalServerError() throws Exception {
        when(decisionEngine.calculateApprovedLoan(any(DecisionRequest.class)))
                .thenThrow(new RuntimeException());

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount", nullValue()))
                .andExpect(jsonPath("$.loanPeriod", nullValue()))
                .andExpect(jsonPath("$.errorMessage").value("An unexpected error occurred"))
                .andReturn();

        DecisionResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.loanAmount() == null;
        assert response.loanPeriod() == null;
        assert response.errorMessage().equals("An unexpected error occurred");
    }

    /**
     * This test ensures that if the applicant's age is not eligible,
     * the controller returns an HTTP Bad Request (400) with the appropriate error message.
     */
    @Test
    public void givenIneligibleAge_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(any()))
                .thenThrow(new IneligibleAgeException("Age is ineligible for loan!"));

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount", nullValue()))
                .andExpect(jsonPath("$.loanPeriod", nullValue()))
                .andExpect(jsonPath("$.errorMessage").value("Age is ineligible for loan!"))
                .andReturn();

        DecisionResponse response = objectMapper
                .readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.loanAmount() == null;
        assert response.loanPeriod() == null;
        assert response.errorMessage().equals("Age is ineligible for loan!");
    }
}
