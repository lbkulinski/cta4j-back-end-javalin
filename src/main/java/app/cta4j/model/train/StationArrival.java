package app.cta4j.model.train;

import app.cta4j.model.Arrival;
import app.cta4j.model.serialization.StringToBooleanConverter;
import app.cta4j.model.serialization.StringToInstantConverter;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;

import java.time.Instant;
import java.util.Objects;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record StationArrival(
    @JsonAlias("rn")
    String run,

    @JsonAlias("rt")
    Line line,

    @JsonAlias("destNm")
    String destination,

    @JsonAlias("staNm")
    String station,

    @JsonAlias("prdt")
    @JsonDeserialize(converter = StringToInstantConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant predictionTime,

    @JsonAlias("arrT")
    @JsonDeserialize(converter = StringToInstantConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant arrivalTime,

    @JsonAlias("isApp")
    @JsonDeserialize(converter = StringToBooleanConverter.class)
    boolean due,

    @JsonAlias("isSch")
    @JsonDeserialize(converter = StringToBooleanConverter.class)
    boolean scheduled,

    @JsonAlias("isDly")
    @JsonDeserialize(converter = StringToBooleanConverter.class)
    boolean delayed
) implements Arrival {
    public StationArrival {
        Objects.requireNonNull(run);

        Objects.requireNonNull(line);

        Objects.requireNonNull(destination);

        Objects.requireNonNull(station);

        Objects.requireNonNull(predictionTime);

        Objects.requireNonNull(arrivalTime);
    }
}
