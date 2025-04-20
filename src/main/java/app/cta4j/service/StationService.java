package app.cta4j.service;

import app.cta4j.client.StationArrivalClient;
import app.cta4j.exception.ClientException;
import app.cta4j.model.train.Line;
import app.cta4j.model.train.Station;
import app.cta4j.model.train.StationArrival;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public final class StationService {
    private final DynamoDbTable<Station> stations;

    private final Cache<String, List<Station>> cache;

    private final StationArrivalClient client;

    @Inject
    public StationService(DynamoDbEnhancedClient dynamoDbClient, StationArrivalClient client) {
        Objects.requireNonNull(dynamoDbClient);

        TableSchema<Station> tableSchema = TableSchema.fromImmutableClass(Station.class);

        this.stations = dynamoDbClient.table("stations", tableSchema);

        this.cache = Caffeine.newBuilder()
                             .expireAfterWrite(24L, TimeUnit.HOURS)
                             .build();

        this.client = Objects.requireNonNull(client);
    }

    private List<Station> loadStations() {
        List<Station> stations = this.stations.scan()
                                              .items()
                                              .stream()
                                              .toList();

        return List.copyOf(stations);
    }

    public List<Station> getStations() {
        return this.cache.get("stations", key -> this.loadStations());
    }

    public Set<StationArrival> getArrivals(String stationId) throws ClientException {
        List<StationArrival> arrivals = this.client.getStationArrivals(stationId);

        return arrivals.stream()
                       .filter(arrival -> arrival.line() != Line.N_A)
                       .collect(Collectors.toSet());
    }
}
