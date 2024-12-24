package app.cta4j.service;

import app.cta4j.client.TrainArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.model.Arrival;
import app.cta4j.model.ArrivalBody;
import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.Line;
import com.google.inject.Inject;

import java.util.List;
import java.util.Objects;

public final class TrainService {
    private final TrainArrivalClient trainArrivalClient;

    @Inject
    public TrainService(TrainArrivalClient trainArrivalClient) {
        this.trainArrivalClient = Objects.requireNonNull(trainArrivalClient);
    }

    public List<Arrival> getArrivals(String run) {
        Objects.requireNonNull(run);

        ArrivalResponse response = this.trainArrivalClient.getTrainArrivals(run);

        if (response == null) {
            throw new RuntimeException("The arrival response is null for run %s".formatted(run));
        }

        ArrivalBody body = response.body();

        if (body == null) {
            throw new RuntimeException("The arrival body is null for run %s".formatted(run));
        }

        List<Arrival> arrivals = body.arrivals();

        if (arrivals == null) {
            throw new ResourceNotFoundException("The List of arrivals is null for run %s".formatted(run));
        }

        return arrivals.stream()
                       .filter(arrival -> arrival.line() != Line.N_A)
                       .toList();
    }
}
