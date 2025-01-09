package app.cta4j.service;

import app.cta4j.client.StationArrivalClient;
import app.cta4j.model.train.StationArrival;
import app.cta4j.model.ArrivalBody;
import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.train.Line;
import com.google.inject.Inject;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;

import java.util.List;
import java.util.Objects;

public final class TrainService {
    private final StationArrivalClient client;

    @Inject
    public TrainService(StationArrivalClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public List<StationArrival> getArrivals(String run) {
        Objects.requireNonNull(run);

        ArrivalResponse<StationArrival> response = this.client.getTrainArrivals(run);

        if (response == null) {
            throw new InternalServerErrorResponse("The arrival response is null for run %s".formatted(run));
        }

        ArrivalBody<StationArrival> body = response.body();

        if (body == null) {
            throw new InternalServerErrorResponse("The arrival body is null for run %s".formatted(run));
        }

        List<StationArrival> arrivals = body.arrivals();

        if (arrivals == null) {
            throw new NotFoundResponse("The List of arrivals is null for run %s".formatted(run));
        }

        return arrivals.stream()
                       .filter(arrival -> arrival.line() != Line.N_A)
                       .toList();
    }
}
