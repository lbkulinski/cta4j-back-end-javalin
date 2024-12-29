package app.cta4j;

import app.cta4j.client.TrainArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.jooq.Tables;
import app.cta4j.model.*;
import app.cta4j.model.train.Line;
import app.cta4j.model.train.Station;
import app.cta4j.model.train.TrainArrival;
import app.cta4j.service.StationService;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.assertj.core.api.Assertions;
import org.jooq.DSLContext;
import org.jooq.SelectWhereStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Set;

class StationServiceTests {
    private DSLContext context;

    private TrainArrivalClient trainArrivalClient;

    private StationService stationService;

    @BeforeEach
    void setUp() {
        this.context = Mockito.mock(DSLContext.class);

        this.trainArrivalClient = Mockito.mock(TrainArrivalClient.class);

        this.stationService = new StationService(this.context, this.trainArrivalClient);
    }

    @Test
    void testGetStations_returns_cached_stations() throws NoSuchFieldException, IllegalAccessException {
        Set<Station> expected = Set.of(
            new Station("41320", "Belmont (Red, Brown & Purple lines)"),
            new Station("41160", "Clinton (Green & Pink lines)"),
            new Station("40710", "Chicago (Brown & Purple lines)")
        );

        @SuppressWarnings("unchecked")
        LoadingCache<String, Set<Station>> cache = Mockito.mock(LoadingCache.class);

        Field cacheField = StationService.class.getDeclaredField("cache");

        cacheField.setAccessible(true);

        cacheField.set(this.stationService, cache);

        Mockito.when(cache.get("stations"))
               .thenReturn(expected);

        Set<Station> actual = this.stationService.getStations();

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void testGetStations_returns_stations_from_database() {
        Set<Station> expected = Set.of(
            new Station("41320", "Belmont (Red, Brown & Purple lines)"),
            new Station("41160", "Clinton (Green & Pink lines)"),
            new Station("40710", "Chicago (Brown & Purple lines)")
        );

        Mockito.when(this.context.selectFrom(Tables.STATION))
               .thenReturn(Mockito.mock(SelectWhereStep.class));

        Mockito.when(this.context.selectFrom(Tables.STATION)
                                 .fetchInto(Station.class))
               .thenReturn(List.copyOf(expected));

        Set<Station> actual = this.stationService.getStations();

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void testGetArrivals_returns_arrivals() {
        List<TrainArrival> expected = List.of(
            new TrainArrival("123", Line.RED, "95th/Dan Ryan", "Belmont", Instant.parse("2021-09-01T12:00:00Z"), Instant.parse("2021-09-01T12:05:00Z"), true, false, false),
            new TrainArrival("456", Line.BROWN, "Kimball", "Belmont", Instant.parse("2021-09-01T12:10:00Z"), Instant.parse("2021-09-01T12:15:00Z"), false, true, false),
            new TrainArrival("789", Line.PURPLE, "Linden", "Belmont", Instant.parse("2021-09-01T12:20:00Z"), Instant.parse("2021-09-01T12:25:00Z"), false, false, true)
        );

        ArrivalBody<TrainArrival> body = new ArrivalBody<>(expected);

        ArrivalResponse<TrainArrival> response = new ArrivalResponse<>(body);

        Mockito.when(this.trainArrivalClient.getStationArrivals("41320"))
               .thenReturn(response);

        Set<TrainArrival> actual = this.stationService.getArrivals("41320");

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void testGetArrivals_throws_runtime_exception_with_null_response() {
        Mockito.when(this.trainArrivalClient.getStationArrivals("41320"))
               .thenReturn(null);

        Assertions.assertThatThrownBy(() -> this.stationService.getArrivals("41320"))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival response is null for station ID 41320");
    }

    @Test
    void testGetArrivals_throws_runtime_exception_with_null_body() {
        ArrivalResponse<TrainArrival> response = new ArrivalResponse<>(null);

        Mockito.when(this.trainArrivalClient.getStationArrivals("41320"))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.stationService.getArrivals("41320"))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival body is null for station ID 41320");
    }

    @Test
    void testGetArrivals_throws_resource_not_found_exception_with_null_arrivals() {
        ArrivalBody<TrainArrival> body = new ArrivalBody<>(null);

        ArrivalResponse<TrainArrival> response = new ArrivalResponse<>(body);

        Mockito.when(this.trainArrivalClient.getStationArrivals("41320"))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.stationService.getArrivals("41320"))
                  .isInstanceOf(ResourceNotFoundException.class)
                  .hasMessage("The List of arrivals is null for station ID 41320");
    }

    @Test
    void testGetArrivals_filters_na_arrivals() {
        List<TrainArrival> arrivals = List.of(
            new TrainArrival("123", Line.RED, "95th/Dan Ryan", "Belmont", Instant.parse("2021-09-01T12:00:00Z"), Instant.parse("2021-09-01T12:05:00Z"), true, false, false),
            new TrainArrival("456", Line.BROWN, "Kimball", "Belmont", Instant.parse("2021-09-01T12:10:00Z"), Instant.parse("2021-09-01T12:15:00Z"), false, true, false),
            new TrainArrival("789", Line.N_A, "Linden", "Belmont", Instant.parse("2021-09-01T12:20:00Z"), Instant.parse("2021-09-01T12:25:00Z"), false, false, true)
        );

        ArrivalBody<TrainArrival> body = new ArrivalBody<>(arrivals);

        ArrivalResponse<TrainArrival> response = new ArrivalResponse<>(body);

        Mockito.when(this.trainArrivalClient.getStationArrivals("41320"))
               .thenReturn(response);

        Set<TrainArrival> actual = this.stationService.getArrivals("41320");

        Set<TrainArrival> expected = Set.of(
            new TrainArrival("123", Line.RED, "95th/Dan Ryan", "Belmont", Instant.parse("2021-09-01T12:00:00Z"), Instant.parse("2021-09-01T12:05:00Z"), true, false, false),
            new TrainArrival("456", Line.BROWN, "Kimball", "Belmont", Instant.parse("2021-09-01T12:10:00Z"), Instant.parse("2021-09-01T12:15:00Z"), false, true, false)
        );

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }
}
