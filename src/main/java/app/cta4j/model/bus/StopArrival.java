package app.cta4j.model.bus;

import app.cta4j.model.Arrival;
import app.cta4j.model.serialization.StringToInstantConverter;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;

import java.time.Instant;
import java.util.Objects;

@Builder
public record StopArrival(
    @JsonAlias("vid")
    String id,

    @JsonAlias("typ")
    StopEventType type,

    @JsonAlias("stpnm")
    String stop,

    @JsonAlias("rt")
    String route,

    @JsonAlias("des")
    String destination,

    @JsonAlias("tmstmp")
    @JsonDeserialize(converter = StringToInstantConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant predictionTime,

    @JsonAlias("prdtm")
    @JsonDeserialize(converter = StringToInstantConverter.class)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    Instant arrivalTime,

    @JsonAlias("dly")
    boolean delayed
) implements Arrival {
    public StopArrival {
        Objects.requireNonNull(id);

        Objects.requireNonNull(type);

        Objects.requireNonNull(stop);

        Objects.requireNonNull(route);

        Objects.requireNonNull(destination);

        Objects.requireNonNull(predictionTime);

        Objects.requireNonNull(arrivalTime);
    }
}
