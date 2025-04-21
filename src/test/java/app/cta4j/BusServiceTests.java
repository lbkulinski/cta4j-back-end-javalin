package app.cta4j;

import app.cta4j.client.StopArrivalClient;
import app.cta4j.exception.ClientException;
import app.cta4j.model.ArrivalBody;
import app.cta4j.model.ArrivalResponse;
import app.cta4j.model.bus.StopArrival;
import app.cta4j.model.bus.StopEventType;
import app.cta4j.service.BusService;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;

class BusServiceTests {
    private StopArrivalClient client;

    private BusService service;

    @BeforeEach
    void setUp() {
        this.client = Mockito.mock(StopArrivalClient.class);

        this.service = new BusService(this.client);
    }

    @DisplayName("Test getArrivals returns arrivals")
    @Test
    void testGetArrivals() throws ClientException {
        String id = "1450";

        List<StopArrival> expected = List.of(
            StopArrival.builder()
                       .id(id)
                       .type(StopEventType.ARRIVAL)
                       .stop("Kedzie & Diversey")
                       .route("76")
                       .destination("Nature Museum")
                       .predictionTime(Instant.parse("2025-01-02T18:32:00Z"))
                       .arrivalTime(Instant.parse("2025-01-02T18:34:00Z"))
                       .delayed(false)
                       .build(),

            StopArrival.builder()
                       .id(id)
                       .type(StopEventType.ARRIVAL)
                       .stop("Diversey & Albany")
                       .route("76")
                       .destination("Nature Museum")
                       .predictionTime(Instant.parse("2025-01-02T18:32:00Z"))
                       .arrivalTime(Instant.parse("2025-01-02T18:34:00Z"))
                       .delayed(false)
                       .build(),

            StopArrival.builder()
                       .id(id)
                       .type(StopEventType.ARRIVAL)
                       .stop("Diversey & Sacramento")
                       .route("76")
                       .destination("Nature Museum")
                       .predictionTime(Instant.parse("2025-01-02T18:32:00Z"))
                       .arrivalTime(Instant.parse("2025-01-02T18:35:00Z"))
                       .delayed(false)
                       .build()
        );

        Mockito.when(this.client.getBusArrivals(id))
               .thenReturn(expected);

        List<StopArrival> actual = this.service.getArrivals(id);

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }

    @DisplayName("Test getArrivals throws runtime exception with null response")
    @Test
    void testGetArrivalsThrowsException() throws ClientException {
        String id = "1450";

        Mockito.when(this.client.getBusArrivals(id))
               .thenThrow(ClientException.class);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals(id))
                  .isInstanceOf(InternalServerErrorResponse.class);
    }

    @DisplayName("Test getArrivals throws resource not found exception with null arrivals")
    @Test
    void testGetArrivalsNotFound() throws ClientException {
        String id = "1450";

        ArrivalBody<StopArrival>  body = new ArrivalBody<>(null);

        ArrivalResponse<StopArrival>  response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getBusArrivals(id))
               .thenThrow(NotFoundResponse.class);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals(id))
                  .isInstanceOf(NotFoundResponse.class);
    }
}
