package app.cta4j.service;

import app.cta4j.client.StopArrivalClient;
import app.cta4j.exception.ClientException;
import app.cta4j.model.bus.StopArrival;
import io.javalin.http.InternalServerErrorResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Singleton
public final class BusService {
    private final StopArrivalClient client;

    private static final Logger LOGGER;

    static {
        LOGGER = LoggerFactory.getLogger(BusService.class);
    }

    @Inject
    public BusService(StopArrivalClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public List<StopArrival> getArrivals(String id) {
        Objects.requireNonNull(id);

        List<StopArrival> arrivals;

        try {
            arrivals = this.client.getBusArrivals(id);
        } catch (ClientException e) {
            String message = "Failed to get arrivals for ID %s".formatted(id);

            BusService.LOGGER.error(message, e);

            throw new InternalServerErrorResponse();
        }

        return List.copyOf(arrivals);
    }
}
