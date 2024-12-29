package app.cta4j.service;

import app.cta4j.client.StopArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.jooq.Tables;
import app.cta4j.model.ArrivalBody;
import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.bus.Direction;
import app.cta4j.model.bus.Route;
import app.cta4j.model.bus.Stop;
import app.cta4j.model.bus.StopArrival;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class StopService {
    private final LoadingCache<String, Set<Route>> routeCache;

    private final Cache<String, Set<Direction>> directionCache;

    private record RouteDirection(String routeId, String direction) {
        public RouteDirection {
            Objects.requireNonNull(routeId);

            Objects.requireNonNull(direction);
        }
    }

    private final Cache<RouteDirection, Set<Stop>> stopCache;

    private final DSLContext context;

    private final StopArrivalClient client;

    @Inject
    public StopService(DSLContext context, StopArrivalClient client) {
        this.routeCache = Caffeine.newBuilder()
                                  .expireAfterWrite(24L, TimeUnit.HOURS)
                                  .build(key -> this.loadRoutes());

        this.directionCache = Caffeine.newBuilder()
                                      .expireAfterWrite(24L, TimeUnit.HOURS)
                                      .build();

        this.stopCache = Caffeine.newBuilder()
                                 .expireAfterWrite(24L, TimeUnit.HOURS)
                                 .build();

        this.context = Objects.requireNonNull(context);

        this.client = Objects.requireNonNull(client);
    }

    private Set<Route> loadRoutes() {
        List<Route> routes = this.context.selectFrom(Tables.ROUTE)
                                         .fetchInto(Route.class);

        return Set.copyOf(routes);
    }

    public Set<Route> getRoutes() {
        return this.routeCache.get("routes");
    }

    private Set<Direction> loadDirections(String routeId) {
        List<Direction> directions = this.context.select(DSL.upper(Tables.DIRECTION.NAME))
                                                 .from(Tables.DIRECTION)
                                                 .join(Tables.ROUTE_DIRECTION)
                                                 .on(Tables.DIRECTION.ID.eq(Tables.ROUTE_DIRECTION.DIRECTION_ID))
                                                 .where(Tables.ROUTE_DIRECTION.ROUTE_ID.eq(routeId))
                                                 .fetchInto(Direction.class);

        return Set.copyOf(directions);
    }

    public Set<Direction> getDirections(String routeId) {
        return this.directionCache.get(routeId, this::loadDirections);
    }

    private Set<Stop> loadStops(RouteDirection key) {
        String routeId = key.routeId();

        String direction = key.direction();

        List<Stop> stops = this.context.select(Tables.STOP.ID, Tables.STOP.NAME)
                                       .from(Tables.STOP)
                                       .join(Tables.ROUTE_STOP)
                                       .on(Tables.STOP.ID.eq(Tables.ROUTE_STOP.STOP_ID))
                                       .join(Tables.DIRECTION)
                                       .on(Tables.ROUTE_STOP.DIRECTION_ID.eq(Tables.DIRECTION.ID))
                                       .where(Tables.ROUTE_STOP.ROUTE_ID.eq(routeId))
                                       .and(Tables.DIRECTION.NAME.eq(direction))
                                       .fetchInto(Stop.class);

        return Set.copyOf(stops);
    }

    public Set<Stop> getStops(String routeId, String direction) {
        RouteDirection key = new RouteDirection(routeId, direction);

        return this.stopCache.get(key, this::loadStops);
    }

    public Set<StopArrival> getArrivals(String routeId, String stopId) {
        ArrivalResponse<StopArrival> response = this.client.getStopArrivals(routeId, stopId);

        if (response == null) {
            throw new RuntimeException("""
            The arrival response is null for route ID %s and stop ID %s""".formatted(routeId, stopId));
        }

        ArrivalBody<StopArrival> body = response.body();

        if (body == null) {
            throw new RuntimeException("""
            The arrival body is null for route ID %s and stop ID %s""".formatted(routeId, stopId));
        }

        List<StopArrival> arrivals = body.arrivals();

        if (arrivals == null) {
            throw new ResourceNotFoundException("""
            The List of arrivals is null for route ID %s and stop ID %s""".formatted(routeId, stopId));
        }

        return Set.copyOf(arrivals);
    }
}
