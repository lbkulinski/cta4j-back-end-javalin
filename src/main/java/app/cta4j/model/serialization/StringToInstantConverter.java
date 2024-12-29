package app.cta4j.model.serialization;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

public final class StringToInstantConverter extends StdConverter<String, Instant> {
    @Override
    public Instant convert(String string) {
        Objects.requireNonNull(string);

        ZoneId chicagoId = ZoneId.of("America/Chicago");

        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyyMMdd HH:mm")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(string, formatter);

                return localDateTime.atZone(chicagoId)
                                    .toInstant();
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("\"%s\" is not a valid date-time".formatted(string));
    }
}
