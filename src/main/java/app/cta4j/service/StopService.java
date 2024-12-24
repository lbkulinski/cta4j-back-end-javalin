package app.cta4j.service;

import app.cta4j.client.BusArrivalClient;
import app.cta4j.jooq.Tables;
import app.cta4j.model.Route;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.inject.Inject;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class StopService {
    private final LoadingCache<String, Set<Route>> routeCache;

    private final DSLContext context;

    private final BusArrivalClient busArrivalClient;

    @Inject
    public StopService(DSLContext context, BusArrivalClient busArrivalClient) {
        this.routeCache = Caffeine.newBuilder()
                                  .expireAfterWrite(24L, TimeUnit.HOURS)
                                  .build(key -> this.loadRoutes());

        this.context = Objects.requireNonNull(context);

        this.busArrivalClient = Objects.requireNonNull(busArrivalClient);
    }

    private Set<Route> loadRoutes() {
        List<Route> routes = this.context.selectFrom(Tables.ROUTE)
                                         .fetchInto(Route.class);

        return Set.copyOf(routes);
    }

    public Set<Route> getRoutes() {
        return this.routeCache.get("routes");
    }
}
