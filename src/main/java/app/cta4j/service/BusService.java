package app.cta4j.service;

import app.cta4j.client.StopArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.model.ArrivalBody;
import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.bus.StopArrival;
import com.google.inject.Inject;

import java.util.List;
import java.util.Objects;

public final class BusService {
    private final StopArrivalClient client;

    @Inject
    public BusService(StopArrivalClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public List<StopArrival> getArrivals(String id) {
        Objects.requireNonNull(id);

        ArrivalResponse<StopArrival> response = this.client.getBusArrivals(id);

        if (response == null) {
            throw new RuntimeException("The arrival response is null for ID %s".formatted(id));
        }

        ArrivalBody<StopArrival> body = response.body();

        if (body == null) {
            throw new RuntimeException("The arrival body is null for ID %s".formatted(id));
        }

        List<StopArrival> arrivals = body.arrivals();

        if (arrivals == null) {
            throw new ResourceNotFoundException("The List of arrivals is null for ID %s".formatted(id));
        }

        return List.copyOf(arrivals);
    }
}
