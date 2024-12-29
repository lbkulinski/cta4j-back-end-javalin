package app.cta4j.model.train;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public enum Line {
    RED,

    BLUE,

    BROWN,

    GREEN,

    ORANGE,

    PURPLE,

    PINK,

    YELLOW,

    N_A;

    @JsonCreator
    public static Line parseString(String string) {
        Objects.requireNonNull(string);

        string = string.toUpperCase();

        return switch (string) {
            case "RED", "RED LINE" -> Line.RED;
            case "BLUE", "BLUE LINE" -> Line.BLUE;
            case "BRN", "BROWN LINE" -> Line.BROWN;
            case "G", "GREEN LINE" -> Line.GREEN;
            case "ORG", "ORANGE LINE" -> Line.ORANGE;
            case "P", "PURPLE LINE" -> Line.PURPLE;
            case "PINK", "PINK LINE" -> Line.PINK;
            case "Y", "YELLOW LINE" -> Line.YELLOW;
            case "N/A" -> Line.N_A;
            default -> {
                String message = "A line with the name \"%s\" does not exist".formatted(string);

                throw new IllegalArgumentException(message);
            }
        };
    }
}