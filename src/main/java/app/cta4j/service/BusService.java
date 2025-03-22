package app.cta4j.service;

import app.cta4j.client.StopArrivalClient;
import app.cta4j.model.ArrivalBody;
import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.bus.StopArrival;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Objects;

@Singleton
public final class BusService {
    private final StopArrivalClient client;

    @Inject
    public BusService(StopArrivalClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public List<StopArrival> getArrivals(String id) {
        Objects.requireNonNull(id);

        List<StopArrival> arrivals = this.client.getBusArrivals(id);

        return List.copyOf(arrivals);
    }
}
