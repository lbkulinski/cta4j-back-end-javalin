package app.cta4j.model;

import java.util.Objects;

public record Station(
    int id,

    String name
) {
    public Station {
        Objects.requireNonNull(name);
    }
}
