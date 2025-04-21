package app.cta4j.service;

import app.cta4j.client.StopArrivalClient;
import app.cta4j.exception.ClientException;
import app.cta4j.model.bus.*;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Singleton
public final class StopService {
    private final DynamoDbTable<Route> routes;

    private final DynamoDbTable<RouteDirections> routeDirections;

    private final DynamoDbTable<RouteStops> routeStops;

    private final Cache<String, List<Route>> routeCache;

    private final Cache<String, List<String>> directionCache;

    private record RouteDirection(String routeId, String direction) {
        public RouteDirection {
            Objects.requireNonNull(routeId);

            Objects.requireNonNull(direction);
        }
    }

    private final Cache<RouteDirection, List<Stop>> stopCache;

    private final StopArrivalClient client;

    @Inject
    public StopService(DynamoDbEnhancedClient dynamoDbClient, StopArrivalClient client) {
        Objects.requireNonNull(dynamoDbClient);

        TableSchema<Route> routeSchema = TableSchema.fromImmutableClass(Route.class);

        this.routes = dynamoDbClient.table("routes", routeSchema);

        TableSchema<RouteDirections> routeDirectionsSchema = TableSchema.fromImmutableClass(RouteDirections.class);

        this.routeDirections = dynamoDbClient.table("route_directions", routeDirectionsSchema);

        TableSchema<RouteStops> routeStopsSchema = TableSchema.fromImmutableClass(RouteStops.class);

        this.routeStops = dynamoDbClient.table("route_stops", routeStopsSchema);

        this.routeCache = Caffeine.newBuilder()
                                  .expireAfterWrite(24L, TimeUnit.HOURS)
                                  .build();

        this.directionCache = Caffeine.newBuilder()
                                      .expireAfterWrite(24L, TimeUnit.HOURS)
                                      .build();

        this.stopCache = Caffeine.newBuilder()
                                 .expireAfterWrite(24L, TimeUnit.HOURS)
                                 .build();

        this.client = Objects.requireNonNull(client);
    }

    private List<Route> loadRoutes() {
        List<Route> routes = this.routes.scan()
                                        .items()
                                        .stream()
                                        .toList();

        return List.copyOf(routes);
    }

    public List<Route> getRoutes() {
        return this.routeCache.get("routes", key -> this.loadRoutes());
    }

    private List<String> loadDirections(String routeId) {
        Objects.requireNonNull(routeId);

        Key key = Key.builder()
                     .partitionValue(routeId)
                     .build();

        List<String> directions = this.routeDirections.getItem(key)
                                                      .getDirections();

        return List.copyOf(directions);
    }

    public List<String> getDirections(String routeId) {
        return this.directionCache.get(routeId, this::loadDirections);
    }

    private List<Stop> loadStops(RouteDirection routeDirection) {
        String routeId = routeDirection.routeId();

        String direction = routeDirection.direction();

        Key key = Key.builder()
                     .partitionValue(routeId)
                     .sortValue(direction)
                     .build();

        List<Stop> stops = this.routeStops.getItem(key)
                                          .getStops();

        return List.copyOf(stops);
    }

    public List<Stop> getStops(String routeId, String direction) {
        Objects.requireNonNull(routeId);

        Objects.requireNonNull(direction);

        RouteDirection key = new RouteDirection(routeId, direction);

        return this.stopCache.get(key, this::loadStops);
    }

    public List<StopArrival> getArrivals(String routeId, String stopId) throws ClientException {
        Objects.requireNonNull(routeId);

        Objects.requireNonNull(stopId);

        List<StopArrival> arrivals = this.client.getStopArrivals(routeId, stopId);

        return List.copyOf(arrivals);
    }
}
