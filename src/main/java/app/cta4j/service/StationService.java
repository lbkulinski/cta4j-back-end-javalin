package app.cta4j.service;

import app.cta4j.client.StationArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.model.*;
import app.cta4j.model.train.Line;
import app.cta4j.model.train.Station;
import app.cta4j.model.train.StationArrival;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.inject.Inject;
import redis.clients.jedis.UnifiedJedis;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class StationService {
    private final UnifiedJedis jedis;

    private final ObjectMapper mapper;

    private final LoadingCache<String, Set<Station>> cache;

    private final StationArrivalClient client;

    @Inject
    public StationService(UnifiedJedis jedis, ObjectMapper mapper, StationArrivalClient client) {
        this.jedis = Objects.requireNonNull(jedis);

        this.mapper = Objects.requireNonNull(mapper);

        this.cache = Caffeine.newBuilder()
                             .expireAfterWrite(24L, TimeUnit.HOURS)
                             .build(key -> this.loadStations());

        this.client = Objects.requireNonNull(client);
    }

    private Set<Station> loadStations() {
        String stationsJson = this.jedis.get("stations");

        if (stationsJson == null) {
            throw new ResourceNotFoundException("The stations JSON is null");
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
