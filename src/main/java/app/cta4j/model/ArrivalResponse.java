package app.cta4j.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ArrivalResponse<T extends Arrival>(
    @JsonAlias(value = {"ctatt", "bustime-response"})
    ArrivalBody<T> body
) {
}
