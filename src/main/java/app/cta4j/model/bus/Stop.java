package app.cta4j.model.bus;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.Objects;

public record Stop(
    @JsonAlias("stpid")
    String id,

    @JsonAlias("stpnm")
    String name
) {
    public Stop {
        Objects.requireNonNull(name);
    }
}
