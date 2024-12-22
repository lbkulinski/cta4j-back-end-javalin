package app.cta4j.model;

import java.util.Objects;

public record Station(
    String id,

    String name
) {
    public Station {
        Objects.requireNonNull(id);

        Objects.requireNonNull(name);
    }
}
