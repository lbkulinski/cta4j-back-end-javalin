package app.cta4j.service;

import app.cta4j.client.ArrivalClient;
import app.cta4j.jooq.Tables;
import app.cta4j.model.Arrival;
import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.Station;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.inject.Inject;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
        return this.arrivalClient.getArrivals(stationId)
                                 .body()
                                 .arrivals();
    }
}
