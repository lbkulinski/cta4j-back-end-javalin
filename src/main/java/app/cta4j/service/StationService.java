package app.cta4j.service;

import app.cta4j.client.ArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.jooq.Tables;
import app.cta4j.model.*;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.inject.Inject;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class StationService {
    private final LoadingCache<String, Set<Station>> cache;

    private final DSLContext context;

    private final ArrivalClient arrivalClient;

    @Inject
    public StationService(DSLContext context, ArrivalClient arrivalClient) {
        this.cache = Caffeine.newBuilder()
                             .expireAfterWrite(24L, TimeUnit.HOURS)
                             .build(key -> this.loadStations());

        this.context = Objects.requireNonNull(context);

        this.arrivalClient = Objects.requireNonNull(arrivalClient);
    }

    private Set<Station> loadStations() {
        List<Station> stations = this.context.selectFrom(Tables.STATION)
                                             .fetchInto(Station.class);

        return Set.copyOf(stations);
    }

    public Set<Station> getStations() {
        return this.cache.get("stations");
    }

    public Set<Arrival> getArrivals(String stationId) {
        ArrivalResponse response = this.arrivalClient.getArrivals(stationId);

        if (response == null) {
            throw new RuntimeException("The train response is null for station ID %s".formatted(stationId));
        }

        ArrivalBody body = response.body();

        if (body == null) {
            throw new RuntimeException("The train body is null for station ID %s".formatted(stationId));
        }

        Set<Arrival> arrivals = body.arrivals();

        if (arrivals == null) {
            throw new ResourceNotFoundException("The Set of trains is null for station ID %s".formatted(stationId));
        }

        return arrivals.stream()
                       .filter(arrival -> arrival.line() != Line.N_A)
                       .collect(Collectors.toSet());
    }
}
