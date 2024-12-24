package app.cta4j;

import app.cta4j.client.ArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.model.*;
import app.cta4j.service.TrainService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Set;

class TrainServiceTests {
    private ArrivalClient arrivalClient;

    private TrainService trainService;

    @BeforeEach
    void setUp() {
        this.arrivalClient = Mockito.mock(ArrivalClient.class);

        this.trainService = new TrainService(this.arrivalClient);
    }

    @Test
    void testGetArrivals_returns_arrivals() {
        List<Arrival> expected = List.of(
            new Arrival("417", Line.BROWN, "Loop", "Paulina", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:55:09Z"), true, false, false),
            new Arrival("417", Line.BROWN, "Loop", "Southport", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:56:09Z"), false, true, false),
            new Arrival("417", Line.BROWN, "Loop", "Belmont", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:59:09Z"), false, false, true)
        );

        ArrivalBody body = new ArrivalBody(expected);

        ArrivalResponse response = new ArrivalResponse(body);

        Mockito.when(this.arrivalClient.getTrainArrivals("417"))
               .thenReturn(response);

        List<Arrival> actual = this.trainService.getArrivals("417");

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }

    @Test
    void testGetArrivals_throws_runtime_exception_with_null_response() {
        Mockito.when(this.arrivalClient.getTrainArrivals("417"))
               .thenReturn(null);

        Assertions.assertThatThrownBy(() -> this.trainService.getArrivals("417"))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival response is null for run 417");
    }

    @Test
    void testGetArrivals_throws_runtime_exception_with_null_body() {
        ArrivalResponse response = new ArrivalResponse(null);

        Mockito.when(this.arrivalClient.getTrainArrivals("417"))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.trainService.getArrivals("417"))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival body is null for run 417");
    }

    @Test
    void testGetArrivals_throws_resource_not_found_exception_with_null_arrivals() {
        ArrivalBody body = new ArrivalBody(null);

        ArrivalResponse response = new ArrivalResponse(body);

        Mockito.when(this.arrivalClient.getTrainArrivals("417"))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.trainService.getArrivals("417"))
                  .isInstanceOf(ResourceNotFoundException.class)
                  .hasMessage("The List of arrivals is null for run 417");
    }

    @Test
    void testGetArrivals_filters_na_arrivals() {
        List<Arrival> arrivals = List.of(
            new Arrival("417", Line.BROWN, "Loop", "Paulina", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:55:09Z"), true, false, false),
            new Arrival("417", Line.BROWN, "Loop", "Southport", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:56:09Z"), false, true, false),
            new Arrival("417", Line.N_A, "Loop", "Belmont", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:59:09Z"), false, false, true)
        );

        ArrivalBody body = new ArrivalBody(arrivals);

        ArrivalResponse response = new ArrivalResponse(body);

        Mockito.when(this.arrivalClient.getTrainArrivals("417"))
               .thenReturn(response);

        List<Arrival> actual = this.trainService.getArrivals("417");

        Set<Arrival> expected = Set.of(
            new Arrival("417", Line.BROWN, "Loop", "Paulina", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:55:09Z"), true, false, false),
            new Arrival("417", Line.BROWN, "Loop", "Southport", Instant.parse("2024-12-22T21:54:09Z"), Instant.parse("2024-12-22T21:56:09Z"), false, true, false)
        );

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }
}
