package app.cta4j.model.train;

import lombok.Builder;

import java.util.Objects;

@Builder
public record Station(
    String id,

    String name
) {
    public Station {
        Objects.requireNonNull(id);

        Objects.requireNonNull(name);
    }
}
