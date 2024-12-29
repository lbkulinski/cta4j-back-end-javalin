package app.cta4j.model.bus;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Direction {
    NORTHBOUND,

    SOUTHBOUND,

    EASTBOUND,

    WESTBOUND;

    @JsonValue
    public String toFormattedString() {
        String firstLetter = this.name()
                                 .substring(0, 1);

        String restOfName = this.name()
                                .substring(1)
                                .toLowerCase();

        return firstLetter + restOfName;
    }
}
