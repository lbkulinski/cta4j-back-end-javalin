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

    private List<StationArrival> getArrivalTestData() {
        return List.of(
            StationArrival.builder()
                          .run("123")
                          .line(Line.RED)
                          .destination("95th/Dan Ryan")
                          .station("Belmont")
                          .predictionTime(Instant.parse("2021-09-01T12:00:00Z"))
                          .arrivalTime(Instant.parse("2021-09-01T12:05:00Z"))
                          .due(true)
                          .delayed(false)
                          .scheduled(false)
                          .build(),

            StationArrival.builder()
                          .run("456")
                          .line(Line.BROWN)
                          .destination("Kimball")
                          .station("Belmont")
                          .predictionTime(Instant.parse("2021-09-01T12:10:00Z"))
                          .arrivalTime(Instant.parse("2021-09-01T12:15:00Z"))
                          .due(false)
                          .delayed(true)
                          .scheduled(false)
                          .build(),

            StationArrival.builder()
                          .run("789")
                          .line(Line.PURPLE)
                          .destination("Linden")
                          .station("Belmont")
                          .predictionTime(Instant.parse("2021-09-01T12:20:00Z"))
                          .arrivalTime(Instant.parse("2021-09-01T12:25:00Z"))
                          .due(false)
                          .delayed(false)
                          .scheduled(true)
                          .build()
        );
    }

    @DisplayName("Test getArrivals returns arrivals")
    @Test
    void testGetArrivals() {
        List<StationArrival> expected = this.getArrivalTestData();

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

    private List<StationArrival> getArrivalTestDataNA() {
        return List.of(
            StationArrival.builder()
                          .run("123")
                          .line(Line.RED)
                          .destination("95th/Dan Ryan")
                          .station("Belmont")
                          .predictionTime(Instant.parse("2021-09-01T12:00:00Z"))
                          .arrivalTime(Instant.parse("2021-09-01T12:05:00Z"))
                          .due(true)
                          .delayed(false)
                          .scheduled(false)
                          .build(),

            StationArrival.builder()
                          .run("456")
                          .line(Line.BROWN)
                          .destination("Kimball")
                          .station("Belmont")
                          .predictionTime(Instant.parse("2021-09-01T12:10:00Z"))
                          .arrivalTime(Instant.parse("2021-09-01T12:15:00Z"))
                          .due(false)
                          .delayed(true)
                          .scheduled(false)
                          .build(),

            StationArrival.builder()
                          .run("789")
                          .line(Line.N_A)
                          .destination("Linden")
                          .station("Belmont")
                          .predictionTime(Instant.parse("2021-09-01T12:20:00Z"))
                          .arrivalTime(Instant.parse("2021-09-01T12:25:00Z"))
                          .due(false)
                          .delayed(false)
                          .scheduled(true)
                          .build()
        );
    }

    @DisplayName("Test getArrivals filters N/A arrivals")
    @Test
    void testGetArrivalsNAFilter() {
        List<StationArrival> arrivals = this.getArrivalTestDataNA();

        ArrivalBody<StationArrival>  body = new ArrivalBody<>(arrivals);

        ArrivalResponse<StationArrival>  response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getTrainArrivals("417"))
               .thenReturn(response);

        List<StationArrival> actual = this.service.getArrivals("417");

        Set<StationArrival> expected = Set.of(
            StationArrival.builder()
                          .run("123")
                          .line(Line.RED)
                          .destination("95th/Dan Ryan")
                          .station("Belmont")
                          .predictionTime(Instant.parse("2021-09-01T12:00:00Z"))
                          .arrivalTime(Instant.parse("2021-09-01T12:05:00Z"))
                          .due(true)
                          .delayed(false)
                          .scheduled(false)
                          .build(),

            StationArrival.builder()
                          .run("456")
                          .line(Line.BROWN)
                          .destination("Kimball")
                          .station("Belmont")
                          .predictionTime(Instant.parse("2021-09-01T12:10:00Z"))
                          .arrivalTime(Instant.parse("2021-09-01T12:15:00Z"))
                          .due(false)
                          .delayed(true)
                          .scheduled(false)
                          .build()
        );

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }
}
