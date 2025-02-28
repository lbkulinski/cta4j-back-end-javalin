package app.cta4j.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;

import java.util.Objects;

@Builder
public record ArrivalResponse<T extends Arrival>(
    @JsonAlias(value = {"ctatt", "bustime-response"})
    ArrivalBody<T> body
) {
}
