package ee.taltech.inbankbackend.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds the request data of the REST endpoint
 */
public record DecisionRequest(
        @JsonProperty("personalCode") String personalCode,
        @JsonProperty("loanAmount") Integer loanAmount,
        @JsonProperty("loanPeriod") Integer loanPeriod
) {}
