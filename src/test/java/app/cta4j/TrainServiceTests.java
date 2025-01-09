package app.cta4j;

import app.cta4j.client.StationArrivalClient;
import app.cta4j.model.*;
import app.cta4j.model.train.Line;
import app.cta4j.model.train.StationArrival;
import app.cta4j.service.TrainService;
import io.javalin.http.NotFoundResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Set;

class TrainServiceTests {
    private StationArrivalClient client;

    private TrainService service;

    @BeforeEach
    void setUp() {
        this.client = Mockito.mock(StationArrivalClient.class);

        this.service = new TrainService(this.client);
    }

    @DisplayName("Test getArrivals returns arrivals")
    @Test
    void testGetArrivals() {
        String run = "417";

        List<StationArrival> expected = List.of(
            StationArrival.builder()
                          .run(run)
                          .line(Line.BROWN)
                          .destination("Kimball")
                          .station("Belmont")
                          .predictionTime(Instant.parse("2025-01-02T18:50:53Z"))
                          .arrivalTime(Instant.parse("2025-01-02T18:51:53Z"))
                          .due(true)
                          .delayed(false)
                          .scheduled(false)
                          .build(),

            StationArrival.builder()
                          .run(run)
                          .line(Line.BROWN)
                          .destination("Kimball")
                          .station("Southport")
                          .predictionTime(Instant.parse("2025-01-02T18:50:53Z"))
                          .arrivalTime(Instant.parse("2025-01-02T18:55:53Z"))
                          .due(false)
                          .delayed(true)
                          .scheduled(false)
                          .build(),

            StationArrival.builder()
                          .run(run)
                          .line(Line.BROWN)
                          .destination("Kimball")
                          .station("Paulina")
                          .predictionTime(Instant.parse("2025-01-02T18:50:53Z"))
                          .arrivalTime(Instant.parse("2025-01-02T18:56:53Z"))
                          .due(false)
                          .delayed(false)
                          .scheduled(true)
                          .build()
        );

        ArrivalBody<StationArrival> body = new ArrivalBody<>(expected);

        ArrivalResponse<StationArrival>  response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getTrainArrivals(run))
               .thenReturn(response);

        List<StationArrival> actual = this.service.getArrivals(run);

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }

    @DisplayName("Test getArrivals throws runtime exception with null response")
    @Test
    void testGetArrivalsThrowsExceptionNullResponse() {
        String run = "417";

        Mockito.when(this.client.getTrainArrivals(run))
               .thenReturn(null);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals(run))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival response is null for run %s".formatted(run));
    }

    @DisplayName("Test getArrivals throws runtime exception with null body")
    @Test
    void testGetArrivalsThrowsExceptionNullBody() {
        String run = "417";

        ArrivalResponse<StationArrival>  response = new ArrivalResponse<>(null);

        Mockito.when(this.client.getTrainArrivals(run))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals(run))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival body is null for run %s".formatted(run));
    }

    @DisplayName("Test getArrivals throws resource not found exception with null arrivals")
    @Test
    void testGetArrivalsNotFound() {
        String run = "417";

        ArrivalBody<StationArrival>  body = new ArrivalBody<>(null);

        ArrivalResponse<StationArrival>  response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getTrainArrivals(run))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals(run))
                  .isInstanceOf(NotFoundResponse.class)
                  .hasMessage("The List of arrivals is null for run %s".formatted(run));
    }

    @DisplayName("Test getArrivals filters N/A arrivals")
    @Test
    void testGetArrivalsNAFilter() {
        String run = "417";

        List<StationArrival> arrivals = List.of(
            StationArrival.builder()
                          .run(run)
                          .line(Line.BROWN)
                          .destination("Kimball")
                          .station("Belmont")
                          .predictionTime(Instant.parse("2025-01-02T18:50:53Z"))
                          .arrivalTime(Instant.parse("2025-01-02T18:51:53Z"))
                          .due(true)
                          .delayed(false)
                          .scheduled(false)
                          .build(),

            StationArrival.builder()
                          .run(run)
                          .line(Line.BROWN)
                          .destination("Kimball")
                          .station("Southport")
                          .predictionTime(Instant.parse("2025-01-02T18:50:53Z"))
                          .arrivalTime(Instant.parse("2025-01-02T18:55:53Z"))
                          .due(false)
                          .delayed(true)
                          .scheduled(false)
                          .build(),

            StationArrival.builder()
                          .run(run)
                          .line(Line.N_A)
                          .destination("Kimball")
                          .station("Paulina")
                          .predictionTime(Instant.parse("2025-01-02T18:50:53Z"))
                          .arrivalTime(Instant.parse("2025-01-02T18:56:53Z"))
                          .due(false)
                          .delayed(false)
                          .scheduled(true)
                          .build()
        );

        ArrivalBody<StationArrival>  body = new ArrivalBody<>(arrivals);

        ArrivalResponse<StationArrival>  response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getTrainArrivals(run))
               .thenReturn(response);

        List<StationArrival> actual = this.service.getArrivals(run);

        Set<StationArrival> expected = Set.of(
            StationArrival.builder()
                          .run(run)
                          .line(Line.BROWN)
                          .destination("Kimball")
                          .station("Belmont")
                          .predictionTime(Instant.parse("2025-01-02T18:50:53Z"))
                          .arrivalTime(Instant.parse("2025-01-02T18:51:53Z"))
                          .due(true)
                          .delayed(false)
                          .scheduled(false)
                          .build(),

            StationArrival.builder()
                          .run(run)
                          .line(Line.BROWN)
                          .destination("Kimball")
                          .station("Southport")
                          .predictionTime(Instant.parse("2025-01-02T18:50:53Z"))
                          .arrivalTime(Instant.parse("2025-01-02T18:55:53Z"))
                          .due(false)
                          .delayed(true)
                          .scheduled(false)
                          .build()
        );

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }
}
