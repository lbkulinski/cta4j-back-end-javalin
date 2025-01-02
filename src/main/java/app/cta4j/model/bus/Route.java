package app.cta4j.model.bus;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.util.Objects;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record Route(
    @JsonAlias("rt")
    String id,

    @JsonAlias("rtnm")
    String name
) {
    public Route {
        Objects.requireNonNull(id);

        Objects.requireNonNull(name);
    }
}
