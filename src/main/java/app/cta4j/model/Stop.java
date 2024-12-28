package app.cta4j.model;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.Objects;

public record Stop(
    @JsonAlias("stpid")
    int id,

    @JsonAlias("stpnm")
    String name
) {
    public Stop {
        Objects.requireNonNull(name);
    }
}
