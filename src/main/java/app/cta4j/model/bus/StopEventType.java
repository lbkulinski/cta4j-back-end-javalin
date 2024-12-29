package app.cta4j.model.bus;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public enum StopEventType {
    ARRIVAL,

    DEPARTURE;

    @JsonCreator
    public static StopEventType ofString(String string) {
        Objects.requireNonNull(string);

        return switch (string) {
            case "A" -> StopEventType.ARRIVAL;
            case "D" -> StopEventType.DEPARTURE;
            default -> {
                String message = "\"%s\" is not a valid type".formatted(string);

                throw new IllegalArgumentException(message);
            }
        };
    }
}
