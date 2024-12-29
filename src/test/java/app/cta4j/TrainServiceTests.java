package app.cta4j;

import app.cta4j.client.StationArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.model.*;
import app.cta4j.model.train.Line;
import app.cta4j.model.train.StationArrival;
import app.cta4j.service.TrainService;
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
        List<StationArrival> expected = List.of(
            new StationArrival("417", Line.BROWN, "Loop", "Paulina", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:55:09Z"), true, false, false),
            new StationArrival("417", Line.BROWN, "Loop", "Southport", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:56:09Z"), false, true, false),
            new StationArrival("417", Line.BROWN, "Loop", "Belmont", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:59:09Z"), false, false, true)
        );

        ArrivalBody<StationArrival> body = new ArrivalBody<>(expected);

        ArrivalResponse<StationArrival>  response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getTrainArrivals("417"))
               .thenReturn(response);

        List<StationArrival> actual = this.service.getArrivals("417");

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }

    @DisplayName("Test getArrivals throws runtime exception with null response")
    @Test
    void testGetArrivalsThrowsExceptionNullResponse() {
        Mockito.when(this.client.getTrainArrivals("417"))
               .thenReturn(null);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals("417"))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival response is null for run 417");
    }

    @DisplayName("Test getArrivals throws runtime exception with null body")
    @Test
    void testGetArrivalsThrowsExceptionNullBody() {
        ArrivalResponse<StationArrival>  response = new ArrivalResponse<>(null);

        Mockito.when(this.client.getTrainArrivals("417"))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals("417"))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival body is null for run 417");
    }

    @DisplayName("Test getArrivals throws resource not found exception with null arrivals")
    @Test
    void testGetArrivalsNotFound() {
        ArrivalBody<StationArrival>  body = new ArrivalBody<>(null);

        ArrivalResponse<StationArrival>  response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getTrainArrivals("417"))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals("417"))
                  .isInstanceOf(ResourceNotFoundException.class)
                  .hasMessage("The List of arrivals is null for run 417");
    }

    @DisplayName("Test getArrivals filters N/A arrivals")
    @Test
    void testGetArrivalsNAFilter() {
        List<StationArrival> arrivals = List.of(
            new StationArrival("417", Line.BROWN, "Loop", "Paulina", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:55:09Z"), true, false, false),
            new StationArrival("417", Line.BROWN, "Loop", "Southport", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:56:09Z"), false, true, false),
            new StationArrival("417", Line.N_A, "Loop", "Belmont", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:59:09Z"), false, false, true)
        );

        ArrivalBody<StationArrival>  body = new ArrivalBody<>(arrivals);

        ArrivalResponse<StationArrival>  response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getTrainArrivals("417"))
               .thenReturn(response);

        List<StationArrival> actual = this.service.getArrivals("417");

        Set<StationArrival> expected = Set.of(
            new StationArrival("417", Line.BROWN, "Loop", "Paulina", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:55:09Z"), true, false, false),
            new StationArrival("417", Line.BROWN, "Loop", "Southport", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:56:09Z"), false, true, false)
        );

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }
}
