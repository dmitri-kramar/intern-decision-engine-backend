package ee.taltech.inbankbackend.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds the response data of the REST endpoint.
 */
public record DecisionResponse(
        @JsonProperty("loanAmount") Integer loanAmount,
        @JsonProperty("loanPeriod") Integer loanPeriod,
        @JsonProperty("errorMessage") String errorMessage
) {}
