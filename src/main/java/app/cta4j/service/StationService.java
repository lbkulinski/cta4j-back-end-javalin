package app.cta4j.service;

import app.cta4j.jooq.Tables;
import app.cta4j.model.Station;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Inject;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class StationService {
    private final Cache<String, Set<Station>> cache;

    private final DSLContext context;

    @Inject
    public StationService(DSLContext context) {
        this.cache = Caffeine.newBuilder()
                             .expireAfterWrite(1L, TimeUnit.MINUTES)
                             .build();

        this.context = Objects.requireNonNull(context);
    }

    private Set<Station> loadStations() {
        List<Station> stations = this.context.selectFrom(Tables.STATION)
                                             .fetchInto(Station.class);

        return Set.copyOf(stations);
    }

    public Set<Station> getStations() {
        return this.cache.get("stations", key -> this.loadStations());
    }
}
