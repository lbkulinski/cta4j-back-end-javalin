package app.cta4j.service;

import app.cta4j.client.StationArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.jooq.Tables;
import app.cta4j.model.*;
import app.cta4j.model.train.Line;
import app.cta4j.model.train.Station;
import app.cta4j.model.train.StationArrival;
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

    private final StationArrivalClient client;

    @Inject
    public StationService(DSLContext context, StationArrivalClient client) {
        this.cache = Caffeine.newBuilder()
                             .expireAfterWrite(24L, TimeUnit.HOURS)
                             .build(key -> this.loadStations());

        this.context = Objects.requireNonNull(context);

        this.client = Objects.requireNonNull(client);
    }

    private Set<Station> loadStations() {
        List<Station> stations = this.context.selectFrom(Tables.STATION)
                                             .fetchInto(Station.class);

        return Set.copyOf(stations);
    }

    public Set<Station> getStations() {
        return this.cache.get("stations");
    }

    public Set<StationArrival> getArrivals(String stationId) {
        ArrivalResponse<StationArrival> response = this.client.getStationArrivals(stationId);

        if (response == null) {
            throw new RuntimeException("The arrival response is null for station ID %s".formatted(stationId));
        }

        ArrivalBody<StationArrival> body = response.body();

        if (body == null) {
            throw new RuntimeException("The arrival body is null for station ID %s".formatted(stationId));
        }

        List<StationArrival> arrivals = body.arrivals();

        if (arrivals == null) {
            throw new ResourceNotFoundException("The List of arrivals is null for station ID %s".formatted(stationId));
        }

        return arrivals.stream()
                       .filter(arrival -> arrival.line() != Line.N_A)
                       .collect(Collectors.toSet());
    }
}
