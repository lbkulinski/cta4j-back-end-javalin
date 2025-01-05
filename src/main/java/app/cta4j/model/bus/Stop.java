package app.cta4j.model.bus;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Objects;

@Builder
public record Stop(
    @JsonAlias("stpid")
    String id,

    @JsonAlias("stpnm")
    String name,

    @JsonAlias("lat")
    BigDecimal latitude,

    @JsonAlias("lon")
    BigDecimal longitude
) {
    public Stop {
        Objects.requireNonNull(id);

        Objects.requireNonNull(name);

        Objects.requireNonNull(latitude);

        Objects.requireNonNull(longitude);
    }
}
