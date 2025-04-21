package app.cta4j.service;

import app.cta4j.client.StationArrivalClient;
import app.cta4j.exception.ClientException;
import app.cta4j.model.train.StationArrival;
import app.cta4j.model.train.Line;
import io.javalin.http.InternalServerErrorResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Singleton
public final class TrainService {
    private final StationArrivalClient client;

    private static final Logger LOGGER;

    static {
        LOGGER = LoggerFactory.getLogger(TrainService.class);
    }

    @Inject
    public TrainService(StationArrivalClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public List<StationArrival> getArrivals(String run) {
        Objects.requireNonNull(run);

        List<StationArrival> arrivals;

        try {
            arrivals = this.client.getTrainArrivals(run);
        } catch (ClientException e) {
            String message = "Failed to get arrivals for run %s".formatted(run);

            TrainService.LOGGER.error(message, e);

            throw new InternalServerErrorResponse();
        }

        return arrivals.stream()
                       .filter(arrival -> arrival.line() != Line.N_A)
                       .toList();
    }
}
