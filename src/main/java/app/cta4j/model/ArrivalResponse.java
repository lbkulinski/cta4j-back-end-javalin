package app.cta4j.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public record ArrivalResponse(@JsonAlias("ctatt") ArrivalBody body) {
}
