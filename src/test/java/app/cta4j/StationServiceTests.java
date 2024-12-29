package app.cta4j;

import app.cta4j.client.StationArrivalClient;
import app.cta4j.exception.ResourceNotFoundException;
import app.cta4j.jooq.Tables;
import app.cta4j.jooq.tables.records.StationRecord;
import app.cta4j.model.*;
import app.cta4j.model.train.Line;
import app.cta4j.model.train.Station;
import app.cta4j.model.train.StationArrival;
import app.cta4j.service.StationService;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.assertj.core.api.Assertions;
import org.jooq.DSLContext;
import org.jooq.SelectWhereStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Set;

class StationServiceTests {
    private DSLContext context;

    private StationArrivalClient client;

    private StationService service;

    @BeforeEach
    void setUp() {
        this.context = Mockito.mock(DSLContext.class);

        this.client = Mockito.mock(StationArrivalClient.class);

        this.service = new StationService(this.context, this.client);
    }

    @DisplayName("Test getStations returns cached stations")
    @Test
    void testGetStationsCached() throws NoSuchFieldException, IllegalAccessException {
        Set<Station> expected = Set.of(
            new Station("41320", "Belmont (Red, Brown & Purple lines)"),
            new Station("41160", "Clinton (Green & Pink lines)"),
            new Station("40710", "Chicago (Brown & Purple lines)")
        );

        @SuppressWarnings("unchecked")
        LoadingCache<String, Set<Station>> cache = Mockito.mock(LoadingCache.class);

        Field cacheField = StationService.class.getDeclaredField("cache");

        cacheField.setAccessible(true);

        cacheField.set(this.service, cache);

        Mockito.when(cache.get("stations"))
               .thenReturn(expected);

        Set<Station> actual = this.service.getStations();

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("Test getStations returns stations from database")
    @Test
    void testGetStationsDatabase() {
        Set<Station> expected = Set.of(
            new Station("41320", "Belmont (Red, Brown & Purple lines)"),
            new Station("41160", "Clinton (Green & Pink lines)"),
            new Station("40710", "Chicago (Brown & Purple lines)")
        );

        @SuppressWarnings("unchecked")
        SelectWhereStep<StationRecord> selectWhereStep = Mockito.mock(SelectWhereStep.class);

        Mockito.when(this.context.selectFrom(Tables.STATION))
               .thenReturn(selectWhereStep);

        Mockito.when(selectWhereStep.fetchInto(Station.class))
               .thenReturn(List.copyOf(expected));

        Set<Station> actual = this.service.getStations();

        Assertions.assertThat(actual)
                  .containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("Test getArrivals returns arrivals")
    @Test
    void testGetArrivals() {
        List<StationArrival> expected = List.of(
            new StationArrival("123", Line.RED, "95th/Dan Ryan", "Belmont", Instant.parse("2021-09-01T12:00:00Z"), Instant.parse("2021-09-01T12:05:00Z"), true, false, false),
            new StationArrival("456", Line.BROWN, "Kimball", "Belmont", Instant.parse("2021-09-01T12:10:00Z"), Instant.parse("2021-09-01T12:15:00Z"), false, true, false),
            new StationArrival("789", Line.PURPLE, "Linden", "Belmont", Instant.parse("2021-09-01T12:20:00Z"), Instant.parse("2021-09-01T12:25:00Z"), false, false, true)
        );

        ArrivalBody<StationArrival> body = new ArrivalBody<>(expected);

        ArrivalResponse<StationArrival> response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getStationArrivals("41320"))
               .thenReturn(response);

        Set<StationArrival> actual = this.service.getArrivals("41320");

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }

    @DisplayName("Test getArrivals throws runtime exception with null response")
    @Test
    void testGetArrivalsThrowsExceptionNullResponse() {
        Mockito.when(this.client.getStationArrivals("41320"))
               .thenReturn(null);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals("41320"))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival response is null for station ID 41320");
    }

    @DisplayName("Test getArrivals throws runtime exception with null body")
    @Test
    void testGetArrivalsThrowsExceptionNullBody() {
        ArrivalResponse<StationArrival> response = new ArrivalResponse<>(null);

        Mockito.when(this.client.getStationArrivals("41320"))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals("41320"))
                  .isInstanceOf(RuntimeException.class)
                  .hasMessage("The arrival body is null for station ID 41320");
    }

    @DisplayName("Test getArrivals throws resource not found exception with null arrivals")
    @Test
    void testGetArrivalsNotFound() {
        ArrivalBody<StationArrival> body = new ArrivalBody<>(null);

        ArrivalResponse<StationArrival> response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getStationArrivals("41320"))
               .thenReturn(response);

        Assertions.assertThatThrownBy(() -> this.service.getArrivals("41320"))
                  .isInstanceOf(ResourceNotFoundException.class)
                  .hasMessage("The List of arrivals is null for station ID 41320");
    }

    @DisplayName("Test getArrivals filters N/A arrivals")
    @Test
    void testGetArrivalsNAFilter() {
        List<StationArrival> arrivals = List.of(
            new StationArrival("123", Line.RED, "95th/Dan Ryan", "Belmont", Instant.parse("2021-09-01T12:00:00Z"), Instant.parse("2021-09-01T12:05:00Z"), true, false, false),
            new StationArrival("456", Line.BROWN, "Kimball", "Belmont", Instant.parse("2021-09-01T12:10:00Z"), Instant.parse("2021-09-01T12:15:00Z"), false, true, false),
            new StationArrival("789", Line.N_A, "Linden", "Belmont", Instant.parse("2021-09-01T12:20:00Z"), Instant.parse("2021-09-01T12:25:00Z"), false, false, true)
        );

        ArrivalBody<StationArrival> body = new ArrivalBody<>(arrivals);

        ArrivalResponse<StationArrival> response = new ArrivalResponse<>(body);

        Mockito.when(this.client.getStationArrivals("41320"))
               .thenReturn(response);

        Set<StationArrival> actual = this.service.getArrivals("41320");

        Set<StationArrival> expected = Set.of(
            new StationArrival("123", Line.RED, "95th/Dan Ryan", "Belmont", Instant.parse("2021-09-01T12:00:00Z"), Instant.parse("2021-09-01T12:05:00Z"), true, false, false),
            new StationArrival("456", Line.BROWN, "Kimball", "Belmont", Instant.parse("2021-09-01T12:10:00Z"), Instant.parse("2021-09-01T12:15:00Z"), false, true, false)
        );

        Assertions.assertThat(actual)
                  .hasSameElementsAs(expected);
    }
}
