package app.cta4j.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.util.List;
import java.util.Objects;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ArrivalBody<T extends Arrival>(@JsonAlias(value = {"eta", "prd"}) List<T> arrivals) {
    public ArrivalBody {
        Objects.requireNonNull(arrivals);
    }
}
