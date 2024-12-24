package app.cta4j.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

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
