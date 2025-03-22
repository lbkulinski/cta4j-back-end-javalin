package app.cta4j.service;

import app.cta4j.client.StopArrivalClient;
import app.cta4j.exception.ClientException;
import app.cta4j.model.bus.Direction;
import app.cta4j.model.bus.Route;
import app.cta4j.model.bus.Stop;
import app.cta4j.model.bus.StopArrival;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.javalin.http.NotFoundResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import redis.clients.jedis.UnifiedJedis;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Singleton
public final class StopService {
    private final UnifiedJedis jedis;

    private final ObjectMapper mapper;

    private final Cache<String, Set<Route>> routeCache;

    private final Cache<String, Set<Direction>> directionCache;

    private record RouteDirection(String routeId, String direction) {
        public RouteDirection {
            Objects.requireNonNull(routeId);

            Objects.requireNonNull(direction);
        }
    }

    private final Cache<RouteDirection, Set<Stop>> stopCache;

    private final StopArrivalClient client;

    @Inject
    public StopService(UnifiedJedis jedis, ObjectMapper mapper, StopArrivalClient client) {
        this.jedis = Objects.requireNonNull(jedis);

        this.mapper = Objects.requireNonNull(mapper);

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

    private Set<Route> loadRoutes() {
        String routesJson = this.jedis.get("routes");

        if (routesJson == null) {
            throw new NotFoundResponse("The routes JSON is null");
        }

        TypeReference<List<Route>> type = new TypeReference<>() {};

        List<Route> routes;

        try {
            routes = this.mapper.readValue(routesJson, type);
        } catch (Exception e) {
            String message = e.getMessage();

            throw new RuntimeException(message);
        }

        return Set.copyOf(routes);
    }

    public Set<Route> getRoutes() {
        return this.routeCache.get("routes", key -> this.loadRoutes());
    }

    private Set<Direction> loadDirections(String routeId) {
        String key = "route:%s:directions".formatted(routeId);

        String directionsJson = this.jedis.get(key);

        if (directionsJson == null) {
            throw new NotFoundResponse("The directions JSON is null for route ID %s".formatted(routeId));
        }

        TypeReference<List<Direction>> type = new TypeReference<>() {};

        List<Direction> directions;

        try {
            directions = this.mapper.readValue(directionsJson, type);
        } catch (Exception e) {
            String message = e.getMessage();

            throw new RuntimeException(message);
        }

        return Set.copyOf(directions);
    }

    public Set<Direction> getDirections(String routeId) {
        return this.directionCache.get(routeId, this::loadDirections);
    }

    private Set<Stop> loadStops(RouteDirection key) {
        String routeId = key.routeId();

        String direction = key.direction();

        String keyString = "route:%s:direction:%s:stops".formatted(routeId, direction);

        String stopsJson = this.jedis.get(keyString);

        if (stopsJson == null) {
            throw new NotFoundResponse("""
            The stops JSON is null for route ID %s and direction %s""".formatted(routeId, direction));
        }

        TypeReference<List<Stop>> type = new TypeReference<>() {};

        List<Stop> stops;

        try {
            stops = this.mapper.readValue(stopsJson, type);
        } catch (Exception e) {
            String message = e.getMessage();

            throw new RuntimeException(message);
        }

        return Set.copyOf(stops);
    }

    public Set<Stop> getStops(String routeId, String direction) {
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
