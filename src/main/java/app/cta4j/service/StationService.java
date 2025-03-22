package app.cta4j.service;

import app.cta4j.client.StationArrivalClient;
import app.cta4j.exception.ClientException;
import app.cta4j.model.train.Line;
import app.cta4j.model.train.Station;
import app.cta4j.model.train.StationArrival;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.stream.Collectors;

@Singleton
public final class StationService {
    private final UnifiedJedis jedis;

    private final ObjectMapper mapper;

    private final Cache<String, Set<Station>> cache;

    private final StationArrivalClient client;

    @Inject
    public StationService(UnifiedJedis jedis, ObjectMapper mapper, StationArrivalClient client) {
        this.jedis = Objects.requireNonNull(jedis);

        this.mapper = Objects.requireNonNull(mapper);

        this.cache = Caffeine.newBuilder()
                             .expireAfterWrite(24L, TimeUnit.HOURS)
                             .build();

        this.client = Objects.requireNonNull(client);
    }

    private Set<Station> loadStations() {
        String stationsJson = this.jedis.get("stations");

        if (stationsJson == null) {
            throw new NotFoundResponse("The stations JSON is null");
        }

        TypeReference<List<Station>> type = new TypeReference<>() {};

        List<Station> stations;

        try {
            stations = this.mapper.readValue(stationsJson, type);
        } catch (JsonProcessingException e) {
            String message = e.getMessage();

            throw new RuntimeException(message);
        }

        return Set.copyOf(stations);
    }

    public Set<Station> getStations() {
        return this.cache.get("stations", key -> this.loadStations());
    }

    public Set<StationArrival> getArrivals(String stationId) throws ClientException {
        List<StationArrival> arrivals = this.client.getStationArrivals(stationId);

        return arrivals.stream()
                       .filter(arrival -> arrival.line() != Line.N_A)
                       .collect(Collectors.toSet());
    }
}
