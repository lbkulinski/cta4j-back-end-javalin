package app.cta4j;

import app.cta4j.client.StationArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.model.*;
import app.cta4j.model.train.Line;
import app.cta4j.model.train.Station;
import app.cta4j.model.train.StationArrival;
import app.cta4j.service.StationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import redis.clients.jedis.UnifiedJedis;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

class StationServiceTests {
    private UnifiedJedis jedis;

    private ObjectMapper mapper;

    private StationArrivalClient client;

    private StationService service;

    @BeforeEach
    void setUp() {
        this.jedis = Mockito.mock(UnifiedJedis.class);

        this.mapper = new ObjectMapper();

        this.client = Mockito.mock(StationArrivalClient.class);

        this.service = new StationService(this.jedis, this.mapper, this.client);
    }

    @DisplayName("Test getStations returns cached stations")
    @Test
    void testGetStationsCached() throws NoSuchFieldException, IllegalAccessException {
        Set<Station> expected = Set.of(
            Station.builder()
                   .id("41320")
                   .name("Belmont (Red, Brown & Purple lines)")
                   .build(),

            Station.builder()
                   .id("41160")
                   .name("Clinton (Green & Pink lines)")
                   .build(),

            Station.builder()
                   .id("40710")
                   .name("Chicago (Brown & Purple lines)")
                   .build()
        );

        @SuppressWarnings("unchecked")
        Cache<String, Set<Station>> cache = Mockito.mock(Cache.class);

        Field cacheField = StationService.class.getDeclaredField("cache");

        cacheField.setAccessible(true);

        cacheField.set(this.service, cache);

        Mockito.when(cache.get(Mockito.eq("stations"), Mockito.any(Function.class)))
               .thenReturn(expected);

        Set<Station> actual = this.service.getStations();

        System.out.println(actual);

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("Test getStations returns stations from database")
    @Test
    void testGetStationsDatabase() {
        Set<Station> expected = Set.of(
            Station.builder()
                   .id("41320")
                   .name("Belmont (Red, Brown & Purple lines)")
                   .build(),

            Station.builder()
                   .id("41160")
                   .name("Clinton (Green & Pink lines)")
                   .build(),

            Station.builder()
                   .id("40710")
                   .name("Chicago (Brown & Purple lines)")
                   .build()
        );

        String expectedJson;

        try {
            expectedJson = this.mapper.writeValueAsString(expected);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Mockito.when(this.jedis.get("stations"))
               .thenReturn(expectedJson);

        Set<Station> actual = this.service.getStations();

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
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
        String stationId = "41320";

        List<StationArrival> expected = this.getArrivalTestData();

        ArrivalBody<StationArrival> body = new ArrivalBody<>(expected);

        ArrivalResponse<StationArrival> response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getStationArrivals(stationId))
               .thenReturn(response);

        Set<StationArrival> actual = this.service.getArrivals(stationId);

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }

    @DisplayName("Test getArrivals throws runtime exception with null response")
    @Test
    void testGetArrivalsThrowsExceptionNullResponse() {
        String stationId = "41320";

        Mockito.when(this.client.getStationArrivals(stationId))
               .thenReturn(null);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals(stationId))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival response is null for station ID %s".formatted(stationId));
    }

    @DisplayName("Test getArrivals throws runtime exception with null body")
    @Test
    void testGetArrivalsThrowsExceptionNullBody() {
        String stationId = "41320";

        ArrivalResponse<StationArrival> response = new ArrivalResponse<>(null);

        Mockito.when(this.client.getStationArrivals(stationId))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals(stationId))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival body is null for station ID %s".formatted(stationId));
    }

    @DisplayName("Test getArrivals throws resource not found exception with null arrivals")
    @Test
    void testGetArrivalsNotFound() {
        String stationId = "41320";

        ArrivalBody<StationArrival> body = new ArrivalBody<>(null);

        ArrivalResponse<StationArrival> response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getStationArrivals(stationId))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals(stationId))
                  .isInstanceOf(ResourceNotFoundException.class)
                  .hasMessage("The List of arrivals is null for station ID %s".formatted(stationId));
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
        String stationId = "41320";

        List<StationArrival> arrivals = this.getArrivalTestDataNA();

        ArrivalBody<StationArrival> body = new ArrivalBody<>(arrivals);

        ArrivalResponse<StationArrival> response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getStationArrivals(stationId))
               .thenReturn(response);

        Set<StationArrival> actual = this.service.getArrivals(stationId);

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
